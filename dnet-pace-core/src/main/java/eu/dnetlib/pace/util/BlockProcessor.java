package eu.dnetlib.pace.util;

import com.google.common.collect.Lists;
import eu.dnetlib.pace.clustering.NGramUtils;
import eu.dnetlib.pace.config.DedupConfig;
import eu.dnetlib.pace.config.WfConfig;
import eu.dnetlib.pace.distance.PaceDocumentDistance;
import eu.dnetlib.pace.distance.eval.ScoreResult;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.MapDocument;
import eu.dnetlib.pace.model.MapDocumentComparator;
import eu.dnetlib.pace.model.TreeNodeDef;
import eu.dnetlib.pace.tree.support.MatchType;
import org.apache.commons.lang.StringUtils;
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

    public void process(final String key, final Iterable<MapDocument> documents, final Reporter context)  {

        final Queue<MapDocument> q = prepare(documents);

        if (q.size() > 1) {
//            log.info("reducing key: '" + key + "' records: " + q.size());
            //process(q, context);

            //process the decision tree if it is specified, otherwise go with conditions and distance algos
            if (!dedupConf.getPace().getDecisionTree().isEmpty()){
                processPersons(q, context);
            }
            else {
                process(simplifyQueue(q, key, context), context);
            }
        } else {
            context.incrementCounter(dedupConf.getWf().getEntityType(), "records per hash key = 1", 1);
        }
    }

    private void processPersons(final Queue<MapDocument> queue, final Reporter context) {

        while (!queue.isEmpty()) {

            final MapDocument pivot = queue.remove(); //take first element of the queue
            final String idPivot = pivot.getIdentifier();

            //compare the first element with all the others
            for (final MapDocument curr : queue) {
                final String idCurr = curr.getIdentifier();

                //check if pivot and current element are similar by processing the tree
                if (navigateTree(pivot, curr))
                    writeSimilarity(context, idPivot, idCurr);
            }


        }
    }

    private boolean navigateTree(final MapDocument doc1, final MapDocument doc2){

        final Map<String, TreeNodeDef> decisionTree = dedupConf.getPace().getDecisionTree();

        String current = "start";

        while (!current.equals(MatchType.NO_MATCH.toString()) && !current.equals(MatchType.ORCID_MATCH.toString()) && !current.equals(MatchType.TOPICS_MATCH.toString()) && !current.equals(MatchType.COAUTHORS_MATCH.toString())) {

            TreeNodeDef currentNode = decisionTree.get(current);
            //throw an exception if the node doesn't exist
            if (currentNode == null)
                throw new PaceException("The Tree Node doesn't exist: " + current);

            int compare = currentNode.treeNode().compare(doc1.getFieldMap().get(currentNode.getField()), doc2.getFieldMap().get(currentNode.getField()));

            current = (compare==0)?currentNode.getUndefined():(compare==-1)?currentNode.getNegative():currentNode.getPositive();
        }

        if (!current.equals(MatchType.NO_MATCH.toString()))
            return true;
        else
            return false;
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

        final PaceDocumentDistance algo = new PaceDocumentDistance();

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
//                        log.info(sr.toString()+"SCORE "+ sr.getScore());
                        emitOutput(sr, idPivot, idCurr, context);
                        i++;
                    }
                }
            }
        }
    }

    private void emitOutput(final ScoreResult sr, final String idPivot, final String idCurr, final Reporter context)  {
        final double d = sr.getScore();

        if (d >= dedupConf.getWf().getThreshold()) {

            writeSimilarity(context, idPivot, idCurr);
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

    private void writeSimilarity(final Reporter context, final String from, final String to)  {
        final String type = dedupConf.getWf().getEntityType();

        context.emit(type, from, to);
        context.emit(type, to, from);
    }

}
