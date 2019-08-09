//package eu.dnetlib.reporter;
//import com.google.common.collect.Lists;
//import eu.dnetlib.pace.clustering.NGramUtils;
//import eu.dnetlib.pace.config.DedupConfig;
//import eu.dnetlib.pace.config.WfConfig;
//import eu.dnetlib.pace.distance.PaceDocumentDistance;
//import eu.dnetlib.pace.distance.eval.ScoreResult;
//import eu.dnetlib.pace.model.Field;
//import eu.dnetlib.pace.model.MapDocument;
//import eu.dnetlib.pace.model.MapDocumentComparator;
//import org.apache.commons.lang.StringUtils;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.apache.spark.util.LongAccumulator;
//import scala.Tuple2;
//
//import java.util.*;
//
//public class SparkBlockProcessor2 {
//
//    private static final Log log = LogFactory.getLog(SparkBlockProcessor2.class);
//
//    private DedupConfig dedupConf;
//
//    public SparkBlockProcessor2(DedupConfig dedupConf) {
//        this.dedupConf = dedupConf;
//    }
//
//    public boolean isSimilar(Tuple2<MapDocument, MapDocument> t, SparkReporter context, Map<String, LongAccumulator> accumulators) {
//
//        final PaceDocumentDistance algo = new PaceDocumentDistance();
//
//        final ScoreResult sr = similarity(algo, t._1(), t._2());
//
//        final double d = sr.getScore();
//
//        if (d >= dedupConf.getWf().getThreshold()) {
//            context.incrementCounter(dedupConf.getWf().getEntityType(), "dedupSimilarity (x2)", 1, accumulators);
//            return true;
//        } else {
//            context.incrementCounter(dedupConf.getWf().getEntityType(), "d < " + dedupConf.getWf().getThreshold(), 1, accumulators);
//            return false;
//        }
//    }
//
//    public Iterator<Tuple2<MapDocument, MapDocument>> process(final String key, final Iterable<MapDocument> documents, final SparkReporter context, Map<String, LongAccumulator> accumulators)  {
//
//        final Queue<MapDocument> q = prepare(documents);
//
//        if (q.size() > 1) {
//            return process(simplifyQueue(q, key, context, accumulators), context, accumulators);
//
//        } else {
//            context.incrementCounter(dedupConf.getWf().getEntityType(), "records per hash key = 1", 1, accumulators);
//            return new ArrayList<Tuple2<MapDocument,MapDocument>>().iterator();
//        }
//    }
//
//    private Queue<MapDocument> prepare(final Iterable<MapDocument> documents) {
//        final Queue<MapDocument> queue = new PriorityQueue<>(100, new MapDocumentComparator(dedupConf.getWf().getOrderField()));
//
//        final Set<String> seen = new HashSet<String>();
//        final int queueMaxSize = dedupConf.getWf().getQueueMaxSize();
//
//        documents.forEach(doc -> {
//            if (queue.size() <= queueMaxSize) {
//                final String id = doc.getIdentifier();
//
//                if (!seen.contains(id)) {
//                    seen.add(id);
//                    queue.add(doc);
//                }
//            }
//        });
//
//        return queue;
//    }
//
//    private Queue<MapDocument> simplifyQueue(final Queue<MapDocument> queue, final String ngram, final SparkReporter context, Map<String, LongAccumulator> accumulators) {
//        final Queue<MapDocument> q = new LinkedList<>();
//
//        String fieldRef = "";
//        final List<MapDocument> tempResults = Lists.newArrayList();
//
//        while (!queue.isEmpty()) {
//            final MapDocument result = queue.remove();
//
//            final String orderFieldName = dedupConf.getWf().getOrderField();
//            final Field orderFieldValue = result.values(orderFieldName);
//            if (!orderFieldValue.isEmpty()) {
//                final String field = NGramUtils.cleanupForOrdering(orderFieldValue.stringValue());
//                if (field.equals(fieldRef)) {
//                    tempResults.add(result);
//                } else {
//                    populateSimplifiedQueue(q, tempResults, context, fieldRef, ngram, accumulators);
//                    tempResults.clear();
//                    tempResults.add(result);
//                    fieldRef = field;
//                }
//            } else {
//                context.incrementCounter(dedupConf.getWf().getEntityType(), "missing " + dedupConf.getWf().getOrderField(), 1, accumulators);
//            }
//        }
//        populateSimplifiedQueue(q, tempResults, context, fieldRef, ngram, accumulators);
//
//        return q;
//    }
//
//    private void populateSimplifiedQueue(final Queue<MapDocument> q,
//                                         final List<MapDocument> tempResults,
//                                         final SparkReporter context,
//                                         final String fieldRef,
//                                         final String ngram,
//                                         Map<String, LongAccumulator> accumulators) {
//        WfConfig wf = dedupConf.getWf();
//        if (tempResults.size() < wf.getGroupMaxSize()) {
//            q.addAll(tempResults);
//        } else {
//            context.incrementCounter(wf.getEntityType(), String.format("Skipped records for count(%s) >= %s", wf.getOrderField(), wf.getGroupMaxSize()), tempResults.size(), accumulators);
////            log.info("Skipped field: " + fieldRef + " - size: " + tempResults.size() + " - ngram: " + ngram);
//        }
//    }
//
//    private Iterator<Tuple2<MapDocument, MapDocument>> process(final Queue<MapDocument> queue, final SparkReporter context, Map<String, LongAccumulator> accumulators)  {
//
//        final PaceDocumentDistance algo = new PaceDocumentDistance();
//
//        List<Tuple2<MapDocument, MapDocument>> ret = new ArrayList<>();
//
//        while (!queue.isEmpty()) {
//
//            final MapDocument pivot = queue.remove();
//            final String idPivot = pivot.getIdentifier();
//
//            WfConfig wf = dedupConf.getWf();
//            final Field fieldsPivot = pivot.values(wf.getOrderField());
//            final String fieldPivot = (fieldsPivot == null) || fieldsPivot.isEmpty() ? null : fieldsPivot.stringValue();
//
//            if (fieldPivot != null) {
//                // System.out.println(idPivot + " --> " + fieldPivot);
//
//                int i = 0;
//                for (final MapDocument curr : queue) {
//                    final String idCurr = curr.getIdentifier();
//
//                    if (mustSkip(idCurr)) {
//
//                        context.incrementCounter(wf.getEntityType(), "skip list", 1, accumulators);
//
//                        break;
//                    }
//
//                    if (i > wf.getSlidingWindowSize()) {
//                        break;
//                    }
//
//                    final Field fieldsCurr = curr.values(wf.getOrderField());
//                    final String fieldCurr = (fieldsCurr == null) || fieldsCurr.isEmpty() ? null : fieldsCurr.stringValue();
//
//                    if (!idCurr.equals(idPivot) && (fieldCurr != null)) {
//
//                        if (pivot.getIdentifier().compareTo(curr.getIdentifier())<0){
//                            ret.add(new Tuple2<>(pivot, curr));
//                        } else {
//                            ret.add(new Tuple2<>(curr, pivot));
//                        }
//                        i++;
//                    }
//                }
//            }
//        }
//
//        return ret.iterator();
//    }
//
//    private ScoreResult similarity(final PaceDocumentDistance algo, final MapDocument a, final MapDocument b) {
//        try {
//            return algo.between(a, b, dedupConf);
//        } catch(Throwable e) {
//            log.error(String.format("\nA: %s\n----------------------\nB: %s", a, b), e);
//            throw new IllegalArgumentException(e);
//        }
//    }
//
//    private boolean mustSkip(final String idPivot) {
//        return dedupConf.getWf().getSkipList().contains(getNsPrefix(idPivot));
//    }
//
//    private String getNsPrefix(final String id) {
//        return StringUtils.substringBetween(id, "|", "::");
//    }
//
//}
