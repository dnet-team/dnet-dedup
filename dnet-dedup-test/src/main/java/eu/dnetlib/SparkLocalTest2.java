//package eu.dnetlib;
//
//import com.google.common.collect.Lists;
//import eu.dnetlib.graph.GraphProcessor;
//import eu.dnetlib.pace.config.DedupConfig;
//import eu.dnetlib.pace.model.MapDocument;
//import eu.dnetlib.pace.utils.PaceUtils;
//import eu.dnetlib.reporter.SparkBlockProcessor2;
//import eu.dnetlib.reporter.SparkReporter;
//import org.apache.spark.api.java.JavaPairRDD;
//import org.apache.spark.api.java.JavaRDD;
//import org.apache.spark.api.java.JavaSparkContext;
//import org.apache.spark.graphx.Edge;
//import org.apache.spark.rdd.RDD;
//import org.apache.spark.sql.SparkSession;
//import org.apache.spark.util.LongAccumulator;
//import scala.Tuple2;
//
//import java.math.BigInteger;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//public class SparkLocalTest2 {
//
//    public static void main(String[] args) {
//
//        double startTime = System.currentTimeMillis();
//
//        final SparkSession spark = SparkSession
//                .builder()
//                .appName("Deduplication")
//                .master("local[*]")
//                .getOrCreate();
//
//        final JavaSparkContext context = new JavaSparkContext(spark.sparkContext());
//
//        final URL dataset = SparkLocalTest2.class.getResource("/eu/dnetlib/pace/softwares.huge.json");
//        JavaRDD<String> dataRDD = context.textFile(dataset.getPath());
//
//        //read the configuration from the classpath
//        final DedupConfig config = DedupConfig.load(Utility.readFromClasspath("/eu/dnetlib/pace/software.pace.conf", SparkLocalTest2.class));
//
//        Map<String, LongAccumulator> accumulators = Utility.constructAccumulator(config, context.sc());
//
//        //create vertexes of the graph: <ID, MapDocument>
//        JavaPairRDD<String, MapDocument> mapDocs = dataRDD.mapToPair(it -> {
//            MapDocument mapDocument = PaceUtils.asMapDocument(config, it);
//            return new Tuple2<>(mapDocument.getIdentifier(), mapDocument);
//        });
//
////        System.out.println("mapDocs = " + mapDocs.count());
//
//        RDD<Tuple2<Object, MapDocument>> vertexes = mapDocs.mapToPair(t -> new Tuple2<Object, MapDocument>( (long) t._1().hashCode(), t._2())).rdd();
//
//        //create relations between documents
//        JavaRDD<Block> blocks = mapDocs.reduceByKey((a, b) -> a)    //the reduce is just to be sure that we haven't document with same id
//                //Clustering: from <id, doc> to List<groupkey,doc>
//                .flatMapToPair(a -> {
//                    final MapDocument currentDocument = a._2();
//
//                    return Utility.getGroupingKeys(config, currentDocument).stream()
//                            .map(it -> new Tuple2<>(it, currentDocument)).collect(Collectors.toList()).iterator();
//                }).groupByKey().map(b -> new Block(b._1(), b._2())).filter(b -> b.getElements().size()>1);
//
////        //BLOCK PURGING
////        blocks = BlockUtils.blockPurging2(blocks);
//////        blockPurging(blocks);
////
//////        //BLOCK FILTERING
////        blocks = BlockUtils.blockFiltering(blocks);
//
//        JavaPairRDD<Tuple2<MapDocument, MapDocument>, Integer> edge = blocks.flatMap(it -> {
//            final SparkReporter reporter = new SparkReporter(accumulators);
//            return new SparkBlockProcessor2(config).process(it.getKey(), it.getElements(), reporter, accumulators);
//        }).mapToPair(candidate -> new Tuple2<>(candidate, 1))
//                .reduceByKey((a, b) -> a + b);
//
//        final JavaPairRDD<String, String> relationRDD = edge.filter(e -> {
//            final SparkReporter reporter = new SparkReporter(accumulators);
//            return new SparkBlockProcessor2(config).isSimilar(e._1(), reporter, accumulators);
//        }).mapToPair(t -> new Tuple2<>(t._1()._1().getIdentifier(), t._1()._2().getIdentifier()));
//
//        System.out.println("relationRDD = " + relationRDD.count());
//
//        final RDD<Edge<String>> edgeRdd = relationRDD.map(it -> new Edge<>(it._1().hashCode(),it._2().hashCode(), "similarTo")).rdd();
//
//        JavaRDD<ConnectedComponent> ccs = GraphProcessor.findCCs(vertexes, edgeRdd, 20).toJavaRDD();
//
//        System.out.println("total time = " + (System.currentTimeMillis()-startTime));
//
//        printStatistics(ccs);
//        accumulators.forEach((name, acc) -> System.out.println(name + " -> " + acc.value()));
//
//    }
//
//    public static void printStatistics(JavaRDD<ConnectedComponent> ccs){
//        final JavaRDD<ConnectedComponent> connectedComponents = ccs.filter(cc -> cc.getDocs().size()>1);
//        final JavaRDD<ConnectedComponent> nonDeduplicated = ccs.filter(cc -> cc.getDocs().size()==1);
//
////        //print deduped
////        connectedComponents.foreach(cc -> {
////            System.out.println(cc);
////        });
////        //print nondeduped
////        nonDeduplicated.foreach(cc -> {
////            System.out.println(cc);
////        });
//
//        System.out.println("Non duplicates: " + nonDeduplicated.count());
//        System.out.println("Duplicates: " + connectedComponents.flatMap(cc -> cc.getDocs().iterator()).count());
//        System.out.println("Connected Components: " + connectedComponents.count());
//
//    }
//
//}