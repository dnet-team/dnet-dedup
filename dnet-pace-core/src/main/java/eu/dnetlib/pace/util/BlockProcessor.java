package eu.dnetlib.pace.util;

import com.google.common.collect.Lists;
import eu.dnetlib.pace.clustering.NGramUtils;
import eu.dnetlib.pace.config.DedupConfig;
import eu.dnetlib.pace.config.WfConfig;
import eu.dnetlib.pace.tree.support.TreeProcessor;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.MapDocument;
import eu.dnetlib.pace.model.MapDocumentComparator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

public class BlockProcessor {

    public static final List<String> accumulators= new ArrayList<>();

    private static final Log log = LogFactory.getLog(BlockProcessor.class);

    private DedupConfig dedupConf;

    public static void constructAccumulator( final DedupConfig dedupConf) {
        accumulators.add(String.format("%s::%s",dedupConf.getWf().getEntityType(), "records per hash key = 1"));
        accumulators.add(String.format("%s::%s",dedupConf.getWf().getEntityType(), "missing " + dedupConf.getWf().getOrderField()));
        accumulators.add(String.format("%s::%s",dedupConf.getWf().getEntityType(), String.format("Skipped records for count(%s) >= %s", dedupConf.getWf().getOrderField(), dedupConf.getWf().getGroupMaxSize())));
        accumulators.add(String.format("%s::%s",dedupConf.getWf().getEntityType(), "skip list"));
        accumulators.add(String.format("%s::%s",dedupConf.getWf().getEntityType(), "dedupSimilarity (x2)"));
        accumulators.add(String.format("%s::%s",dedupConf.getWf().getEntityType(), "d < " + dedupConf.getWf().getThreshold()));
    }

    public BlockProcessor(DedupConfig dedupConf) {
        this.dedupConf = dedupConf;
    }

    public void processSortedBlock(final String key, final List<MapDocument> documents, final Reporter context)  {
        if (documents.size() > 1) {
//            log.info("reducing key: '" + key + "' records: " + q.size());
            process(prepare(documents), context);

        } else {
            context.incrementCounter(dedupConf.getWf().getEntityType(), "records per hash key = 1", 1);
        }
    }

    public void process(final String key, final Iterable<MapDocument> documents, final Reporter context)  {

        final Queue<MapDocument> q = prepare(documents);

        if (q.size() > 1) {
//            log.info("reducing key: '" + key + "' records: " + q.size());
            process(simplifyQueue(q, key, context), context);

        } else {
            context.incrementCounter(dedupConf.getWf().getEntityType(), "records per hash key = 1", 1);
        }
    }

    private Queue<MapDocument> prepare(final Iterable<MapDocument> documents) {
        final Queue<MapDocument> queue = new PriorityQueue<>(100, new MapDocumentComparator(dedupConf.getWf().getOrderField()));

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
        final Queue<MapDocument> q = new LinkedList<>();

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
//            log.info("Skipped field: " + fieldRef + " - size: " + tempResults.size() + " - ngram: " + ngram);
        }
    }

    private void process(final Queue<MapDocument> queue, final Reporter context)  {

        while (!queue.isEmpty()) {

            final MapDocument pivot = queue.remove();
            final String idPivot = pivot.getIdentifier();

            WfConfig wf = dedupConf.getWf();
            final Field fieldsPivot = pivot.values(wf.getOrderField());
            final String fieldPivot = (fieldsPivot == null) || fieldsPivot.isEmpty() ? "" : fieldsPivot.stringValue();

            if (fieldPivot != null) {
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

                        final TreeProcessor treeProcessor = new TreeProcessor(dedupConf);

                        emitOutput(treeProcessor.compare(pivot, curr), idPivot, idCurr, context);

                    }
                }
            }
        }
    }

    private void emitOutput(final boolean result, final String idPivot, final String idCurr, final Reporter context)  {

        if (result) {
            writeSimilarity(context, idPivot, idCurr);
            context.incrementCounter(dedupConf.getWf().getEntityType(), "dedupSimilarity (x2)", 1);
        } else {
            context.incrementCounter(dedupConf.getWf().getEntityType(), "d < " + dedupConf.getWf().getThreshold(), 1);
        }
    }

    private boolean mustSkip(final String idPivot) {
        return dedupConf.getWf().getSkipList().contains(getNsPrefix(idPivot));
    }

    private String getNsPrefix(final String id) {
        return StringUtils.substringBetween(id, "|", "::");
    }

    private void writeSimilarity(final Reporter context, final String from, final String to)  {
        final String type = dedupConf.getWf().getEntityType();

        context.emit(type, from, to);
        context.emit(type, to, from);
    }

}
