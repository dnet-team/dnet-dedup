package eu.dnetlib;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Hashing;
import eu.dnetlib.graph.GraphProcessor;
import eu.dnetlib.pace.config.DedupConfig;
import eu.dnetlib.pace.config.WfConfig;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.MapDocument;
import eu.dnetlib.pace.model.MapDocumentComparator;
import eu.dnetlib.pace.tree.JsonListMatch;
import eu.dnetlib.pace.tree.LevensteinTitle;
import eu.dnetlib.pace.tree.SizeMatch;
import eu.dnetlib.pace.tree.TitleVersionMatch;
import eu.dnetlib.pace.tree.support.TreeProcessor;
import eu.dnetlib.pace.util.BlockProcessor;
import eu.dnetlib.pace.util.MapDocumentUtil;
import eu.dnetlib.pace.util.Reporter;
import eu.dnetlib.pace.utils.Utility;
import eu.dnetlib.reporter.SparkReporter;
import eu.dnetlib.support.Block;
import eu.dnetlib.support.ConnectedComponent;
import eu.dnetlib.support.Relation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.graphx.Edge;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.util.LongAccumulator;
import scala.Serializable;
import scala.Tuple2;
import scala.math.Ordering;

import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Deduper implements Serializable {

    private static final Log log = LogFactory.getLog(Deduper.class);

    public static JavaPairRDD<String, Block> createSortedBlocks(
            JavaPairRDD<String, MapDocument> mapDocs, DedupConfig config) {
        final String of = config.getWf().getOrderField();
        final int maxQueueSize = config.getWf().getGroupMaxSize();

        return mapDocs
                // the reduce is just to be sure that we haven't document with same id
                .reduceByKey((a, b) -> a)
                .map(Tuple2::_2)
                // Clustering: from <id, doc> to List<groupkey,doc>
                .flatMap(
                        a -> Utility
                                .getGroupingKeys(config, a)
                                .stream()
                                .map(it -> Block.from(it, a))
                                .collect(Collectors.toList())
                                .iterator())
                .mapToPair(block -> new Tuple2<>(block.getKey(), block))
                .reduceByKey((b1, b2) -> Block.from(b1, b2, of, maxQueueSize));
    }

    public static Iterator<Tuple2<String, String>> ccToMergeRel(ConnectedComponent cc, DedupConfig dedupConf) {
        return cc
                .getDocs()
                .stream()
                .flatMap(
                        id -> {
                            List<Tuple2<String, String>> tmp = new ArrayList<>();
                            tmp.add(new Tuple2<>(cc.getCcId(), id));
                            return tmp.stream();
                        })
                .iterator();
    }

    public static long hash(final String id) {
        return Hashing.murmur3_128().hashString(id, Charset.defaultCharset()).asLong();
    }

    public static ConnectedComponent entityMerger(String key, Iterator<String> values) {

        ConnectedComponent cc = new ConnectedComponent();
        cc.setCcId(key);
        cc.setDocs(StreamSupport.stream(Spliterators.spliteratorUnknownSize(values, Spliterator.ORDERED), false)
                .collect(Collectors.toCollection(HashSet::new)));
        return cc;
    }

    public static JavaRDD<Relation> computeRelations(
            JavaSparkContext context, JavaPairRDD<String, Block> blocks, DedupConfig config) {
        Map<String, LongAccumulator> accumulators = Utility.constructAccumulator(config, context.sc());

        return blocks
                .flatMapToPair(
                        it -> {
                            final SparkReporter reporter = new SparkReporter(accumulators);
                            new BlockProcessor(config)
                                    .processSortedBlock(it._1(), it._2().getDocuments(), reporter);
                            return reporter.getRelations().iterator();
                        })
                .mapToPair(it -> new Tuple2<>(it._1() + it._2(), new Relation(it._1(), it._2(), "simRel")))
                .reduceByKey((a, b) -> a)
                .map(Tuple2::_2);
    }

    public static Queue<MapDocument> prepareQueue(final Iterable<MapDocument> documents, DedupConfig config) {
        final Queue<MapDocument> queue = new PriorityQueue<>(100, new MapDocumentComparator(config.getWf().getOrderField()));

        final Set<String> seen = new HashSet<String>();
        final int queueMaxSize = config.getWf().getQueueMaxSize();

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

    public static JavaRDD<Relation> computePublicationRelations(
            JavaSparkContext context, JavaPairRDD<String, Block> blocks, DedupConfig config) {

        return blocks.
                flatMapToPair((PairFlatMapFunction<Tuple2<String, Block>, String, String>)
                        it -> {
                            List<Tuple2<String,String>> relations = new ArrayList<>();

                            if (it._2().getDocuments().size()>1) {

                                Queue<MapDocument> queue = prepareQueue(it._2().getDocuments(), config);

                                while (!queue.isEmpty()) {

                                        final MapDocument pivot = queue.remove();
                                        final String idPivot = pivot.getIdentifier();

                                        WfConfig wf = config.getWf();
                                        final Field fieldsPivot = pivot.values(wf.getOrderField());
                                        final String fieldPivot = (fieldsPivot == null) || fieldsPivot.isEmpty() ? "" : fieldsPivot.stringValue();

                                        if (fieldPivot != null) {
                                            int i = 0;
                                            for (final MapDocument curr : queue) {
                                                final String idCurr = curr.getIdentifier();

                                                if (config.getWf().getSkipList().contains(StringUtils.substringBetween(idCurr, "|", "::"))) {
                                                    break;
                                                }

                                                if (i > wf.getSlidingWindowSize()) {
                                                    break;
                                                }

                                                final Field fieldsCurr = curr.values(wf.getOrderField());
                                                final String fieldCurr = (fieldsCurr == null) || fieldsCurr.isEmpty() ? null : fieldsCurr.stringValue();

                                                if (!idCurr.equals(idPivot) && (fieldCurr != null)) {

                                                    double score = 0.0;
                                                    Map<String, String> params = new HashMap<>();
                                                    params.put("jpath_value", "$.value");
                                                    params.put("jpath_classid", "$.qualifier.classid");
                                                    JsonListMatch jsonListMatch = new JsonListMatch(params);
                                                    double result = jsonListMatch.compare(pivot.getFieldMap().get("pid"), curr.getFieldMap().get("pid"), config);
                                                    if (result > 0.5) //if the result of the comparison is greater than the threshold
                                                        score += 10.0;  //high score because it should match when the first condition is satisfied
                                                    else
                                                        score += 0.0;

                                                    TitleVersionMatch titleVersionMatch = new TitleVersionMatch(params);
                                                    double result1 = titleVersionMatch.compare(pivot.getFieldMap().get("title"), curr.getFieldMap().get("title"), config);
                                                    SizeMatch sizeMatch = new SizeMatch(params);
                                                    double result2 = sizeMatch.compare(pivot.getFieldMap().get("authors"), curr.getFieldMap().get("authors"), config);
                                                    if ((result1 == 1.0 && result2 == 1.0) || (result1 == -1.0 && result2 == 1.0) || (result1 == 1.0 && result2 == -1.0) || (result1 == -1.0 && result2 == -1.0))
                                                        score += 0.0;
                                                    else
                                                        score -= 1.0;

                                                    LevensteinTitle levensteinTitle = new LevensteinTitle(params);
                                                    double result3 = levensteinTitle.compare(pivot.getFieldMap().get("title"), curr.getFieldMap().get("title"), config);
                                                    score += result3;

                                                    if (score >= 0.99) {
                                                        relations.add(new Tuple2<>(idPivot, idCurr));
                                                        relations.add(new Tuple2<>(idCurr, idPivot));
                                                    }

                                                }
                                            }
                                        }
                                    }
                                }

                            return relations.iterator();
                        })
                .mapToPair(it -> new Tuple2<>(it._1() + it._2(), new Relation(it._1(), it._2(), "simRel")))
                .reduceByKey((a,b) -> a)
                .map(Tuple2::_2);
    }

    public static boolean comparePublications(MapDocument a, MapDocument b, DedupConfig config){

        double score = 0.0;
        Map<String, String> params = new HashMap<>();
        params.put("jpath_value", "$.value");
        params.put("jpath_classid", "$.qualifier.classid");
        JsonListMatch jsonListMatch = new JsonListMatch(params);
        double result = jsonListMatch.compare(a.getFieldMap().get("pid"), b.getFieldMap().get("pid"), config);
        if (result > 0.5) //if the result of the comparison is greater than the threshold
            score += 1.0;
        else
            score += 0.0;

        TitleVersionMatch titleVersionMatch = new TitleVersionMatch(params);
        double result1 = titleVersionMatch.compare(a.getFieldMap().get("title"), b.getFieldMap().get("title"), config);
        SizeMatch sizeMatch = new SizeMatch(params);
        double result2 = sizeMatch.compare(a.getFieldMap().get("authors"), b.getFieldMap().get("authors"), config);
        if ((result1 == 1.0 && result2 == 1.0) || (result1 == -1.0 && result2 == 1.0) || (result1 == 1.0 && result2 == -1.0) || (result1 == -1.0 && result2 == -1.0))
            score += 0.0;
        else
            score -= 1.0;

        LevensteinTitle levensteinTitle = new LevensteinTitle(params);
        double result3 = levensteinTitle.compare(a.getFieldMap().get("title"), b.getFieldMap().get("title"), config);
        score += result3;

        return score >= 0.99;

    }

    public static void createSimRels(DedupConfig dedupConf, SparkSession spark, String entitiesPath, String simRelsPath){

        JavaSparkContext sc = JavaSparkContext.fromSparkContext(spark.sparkContext());

        JavaPairRDD<String, MapDocument> mapDocuments = sc
                .textFile(entitiesPath)
                .mapToPair(
                        (PairFunction<String, String, MapDocument>) s -> {
                            MapDocument d = MapDocumentUtil.asMapDocumentWithJPath(dedupConf, s);
                            return new Tuple2<>(d.getIdentifier(), d);
                        });

        // create blocks for deduplication
        JavaPairRDD<String, Block> blocks = Deduper.createSortedBlocks(mapDocuments, dedupConf);

        // create relations by comparing only elements in the same group
        JavaRDD<Relation> relations = Deduper.computeRelations(sc, blocks, dedupConf);

        // save the simrel in the workingdir
        spark
                .createDataset(relations.rdd(), Encoders.bean(Relation.class))
                .write()
                .mode(SaveMode.Overwrite)
                .save(simRelsPath);
    }

    public static void createMergeRels(DedupConfig dedupConf, String entitiesPath, String mergeRelsPath, String simRelsPath, SparkSession spark){

        final int maxIterations = dedupConf.getWf().getMaxIterations();

        JavaSparkContext sc = JavaSparkContext.fromSparkContext(spark.sparkContext());

        final JavaPairRDD<Object, String> vertexes = sc
                .textFile(entitiesPath)
                .map(s -> MapDocumentUtil.getJPathString(dedupConf.getWf().getIdPath(), s))
                .mapToPair((PairFunction<String, Object, String>) s -> new Tuple2<>(hash(s), s));

        final RDD<Edge<String>> edgeRdd = spark
                .read()
                .load(simRelsPath)
                .as(Encoders.bean(Relation.class))
                .javaRDD()
                .map(Relation::toEdgeRdd)
                .rdd();

        JavaRDD<ConnectedComponent> ccs = GraphProcessor
                .findCCs(vertexes.rdd(), edgeRdd, maxIterations)
                .toJavaRDD();

        JavaRDD<Relation> mergeRel = ccs
                .filter(k -> k.getDocs().size() > 1)
                .flatMap(cc -> ccToMergeRel(cc, dedupConf))
                .map(it -> new Relation(it._1(), it._2(), "mergeRel"));

        final Dataset<Relation> mergeRels = spark
                .createDataset(
                        mergeRel.rdd(),
                        Encoders.bean(Relation.class));

        mergeRels.write().mode(SaveMode.Overwrite).parquet(mergeRelsPath);
    }

    public static void createDedupEntity(DedupConfig dedupConf, String mergeRelsPath, String entitiesPath, SparkSession spark, String dedupEntityPath){

        JavaPairRDD<String, String> entities = spark
                .read()
                .textFile(entitiesPath)
                .map((MapFunction<String, Tuple2<String, String>>) it ->
                                new Tuple2<>(MapDocumentUtil.getJPathString(dedupConf.getWf().getIdPath(), it), it),
                        Encoders.tuple(Encoders.STRING(), Encoders.STRING()))
                .toJavaRDD()
                .mapToPair(t -> t);

        // <source, target>: source is the dedup_id, target is the id of the mergedIn
        JavaPairRDD<String, Relation> mergeRels = spark
                .read()
                .load(mergeRelsPath)
                .as(Encoders.bean(Relation.class))
                .toJavaRDD()
                .mapToPair(r -> new Tuple2<>(r.getTarget(), r));

        JavaRDD<ConnectedComponent> dedupEntities = mergeRels.join(entities)
                .mapToPair(t -> new Tuple2<>(t._2()._1().getSource(), t._2()._2()))
                .groupByKey()
                .map(t-> entityMerger(t._1(), t._2().iterator()));

        dedupEntities.saveAsTextFile(dedupEntityPath);
    }

}