package eu.dnetlib;

import com.google.common.collect.Lists;
import eu.dnetlib.pace.clustering.NGramUtils;
import eu.dnetlib.pace.config.DedupConfig;
import eu.dnetlib.pace.config.WfConfig;
import eu.dnetlib.pace.distance.PaceDocumentDistance;
import eu.dnetlib.pace.distance.eval.ScoreResult;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.MapDocument;
import eu.dnetlib.pace.model.MapDocumentComparator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import scala.Tuple2;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

public class BlockProcessor {

    private static final Log log = LogFactory.getLog(BlockProcessor.class);

    private DedupConfig dedupConf;

    public BlockProcessor(DedupConfig dedupConf) {
        this.dedupConf = dedupConf;
    }

    public List<Tuple2<String, String>> process(final String key, final Stream<MapDocument> documents, final Reporter context) throws IOException, InterruptedException {

        final Queue<MapDocument> q = prepare(documents);

        if (q.size() > 1) {
            log.info("reducing key: '" + key + "' records: " + q.size());
            //process(q, context);
            return process(simplifyQueue(q, key, context), context);
        } else {
            context.incrementCounter(dedupConf.getWf().getEntityType(), "records per hash key = 1", 1);
            return new ArrayList<>();
        }
    }

    private Queue<MapDocument> prepare(final Stream<MapDocument> documents) {
        final Queue<MapDocument> queue = new PriorityQueue<MapDocument>(100, new MapDocumentComparator(dedupConf.getWf().getOrderField()));

        final Set<String> seen = new HashSet<String>();
        final int queueMaxSize = dedupConf.getWf().getQueueMaxSize();

        documents.forEach(doc -> {
            if (queue.size() <= queueMaxSize) {
                final String id = doc.getIdentifier();

                if (!seen.contains(id)) {
                    seen.add(id);
                    queue.add(doc);
                }
            }
        });

        return queue;
    }

    private Queue<MapDocument> simplifyQueue(final Queue<MapDocument> queue, final String ngram, final Reporter context) {
        final Queue<MapDocument> q = new LinkedList<MapDocument>();

        String fieldRef = "";
        final List<MapDocument> tempResults = Lists.newArrayList();

        while (!queue.isEmpty()) {
            final MapDocument result = queue.remove();

            final String orderFieldName = dedupConf.getWf().getOrderField();
            final Field orderFieldValue = result.values(orderFieldName);
            if (!orderFieldValue.isEmpty()) {
                final String field = NGramUtils.cleanupForOrdering(orderFieldValue.stringValue());
                if (field.equals(fieldRef)) {
                    tempResults.add(result);
                } else {
                    populateSimplifiedQueue(q, tempResults, context, fieldRef, ngram);
                    tempResults.clear();
                    tempResults.add(result);
                    fieldRef = field;
                }
            } else {
                context.incrementCounter(dedupConf.getWf().getEntityType(), "missing " + dedupConf.getWf().getOrderField(), 1);
            }
        }
        populateSimplifiedQueue(q, tempResults, context, fieldRef, ngram);

        return q;
    }

    private void populateSimplifiedQueue(final Queue<MapDocument> q,
                                         final List<MapDocument> tempResults,
                                         final Reporter context,
                                         final String fieldRef,
                                         final String ngram) {
        WfConfig wf = dedupConf.getWf();
        if (tempResults.size() < wf.getGroupMaxSize()) {
            q.addAll(tempResults);
        } else {
            context.incrementCounter(wf.getEntityType(), String.format("Skipped records for count(%s) >= %s", wf.getOrderField(), wf.getGroupMaxSize()), tempResults.size());
            log.info("Skipped field: " + fieldRef + " - size: " + tempResults.size() + " - ngram: " + ngram);
        }
    }

    private List<Tuple2<String, String>> process(final Queue<MapDocument> queue, final Reporter context) throws IOException, InterruptedException {

        final PaceDocumentDistance algo = new PaceDocumentDistance();
        List<Tuple2<String, String>> resultEmit = new ArrayList<>();

        while (!queue.isEmpty()) {

            final MapDocument pivot = queue.remove();
            final String idPivot = pivot.getIdentifier();

            WfConfig wf = dedupConf.getWf();
            final Field fieldsPivot = pivot.values(wf.getOrderField());
            final String fieldPivot = (fieldsPivot == null) || fieldsPivot.isEmpty() ? null : fieldsPivot.stringValue();

            if (fieldPivot != null) {
                // System.out.println(idPivot + " --> " + fieldPivot);

                int i = 0;
                for (final MapDocument curr : queue) {
                    final String idCurr = curr.getIdentifier();

                    if (mustSkip(idCurr)) {

                        context.incrementCounter(wf.getEntityType(), "skip list", 1);

                        break;
                    }

                    if (i > wf.getSlidingWindowSize()) {
                        break;
                    }

                    final Field fieldsCurr = curr.values(wf.getOrderField());
                    final String fieldCurr = (fieldsCurr == null) || fieldsCurr.isEmpty() ? null : fieldsCurr.stringValue();

                    if (!idCurr.equals(idPivot) && (fieldCurr != null)) {

                        final ScoreResult sr = similarity(algo, pivot, curr);
                        emitOutput(sr, idPivot, idCurr,context, resultEmit);
                        i++;
                    }
                }
            }
        }
        return resultEmit;
    }

    private void emitOutput(final ScoreResult sr, final String idPivot, final String idCurr, final Reporter context,List<Tuple2<String, String>> emitResult) throws IOException, InterruptedException {
        final double d = sr.getScore();

        if (d >= dedupConf.getWf().getThreshold()) {

            writeSimilarity(idPivot, idCurr,  emitResult);
            context.incrementCounter(dedupConf.getWf().getEntityType(), "dedupSimilarity (x2)", 1);
        } else {
            context.incrementCounter(dedupConf.getWf().getEntityType(), "d < " + dedupConf.getWf().getThreshold(), 1);
        }
    }

    private ScoreResult similarity(final PaceDocumentDistance algo, final MapDocument a, final MapDocument b) {
        try {
            return algo.between(a, b, dedupConf);
        } catch(Throwable e) {
            log.error(String.format("\nA: %s\n----------------------\nB: %s", a, b), e);
            throw new IllegalArgumentException(e);
        }
    }

    private boolean mustSkip(final String idPivot) {
        return dedupConf.getWf().getSkipList().contains(getNsPrefix(idPivot));
    }

    private String getNsPrefix(final String id) {
        return StringUtils.substringBetween(id, "|", "::");
    }

    private void writeSimilarity( final String from, final String to, List<Tuple2<String, String>> emitResult){
        emitResult.add(new Tuple2<>(from, to));
        emitResult.add(new Tuple2<>( to, from));
    }

}
