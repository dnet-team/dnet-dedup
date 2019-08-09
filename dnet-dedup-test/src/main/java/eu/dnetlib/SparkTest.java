//package eu.dnetlib;
//
//import eu.dnetlib.graph.GraphProcessor;
//import eu.dnetlib.pace.config.DedupConfig;
//import eu.dnetlib.pace.model.MapDocument;
//import eu.dnetlib.pace.utils.PaceUtils;
//import eu.dnetlib.reporter.SparkBlockProcessor;
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
//import java.io.IOException;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//public class SparkTest {
//
//    public static void main(String[] args) throws IOException {
//
//        final String inputSpacePath = args[0];
//        final String dedupConfigPath = args[1];
//        final String groupsPath = args[2] + "_groups";
//        final String outputPath = args[2] + "_output";
//
//        final SparkSession spark = SparkSession
//                .builder()
//                .appName("Deduplication")
//                .master("yarn")
//                .getOrCreate();
//
//        final JavaSparkContext context = new JavaSparkContext(spark.sparkContext());
//
//        final JavaRDD<String> dataRDD = Utility.loadDataFromHDFS(inputSpacePath, context);
//
//        final DedupConfig config = Utility.loadConfigFromHDFS(dedupConfigPath);
//
//        Map<String, LongAccumulator> accumulators = Utility.constructAccumulator(config, context.sc());
//
//        //create vertexes of the graph: <ID, MapDocument>
//        JavaPairRDD<String, MapDocument> mapDocs = dataRDD.mapToPair(it -> {
//            MapDocument mapDocument = PaceUtils.asMapDocument(config, it);
//            return new Tuple2<>(mapDocument.getIdentifier(), mapDocument);
//        });
//        RDD<Tuple2<Object, MapDocument>> vertexes = mapDocs.mapToPair(t -> new Tuple2<Object, MapDocument>( (long) t._1().hashCode(), t._2())).rdd();
//
//        //group documents basing on clustering
//        JavaPairRDD<String, Iterable<MapDocument>> blocks = mapDocs.reduceByKey((a, b) -> a)    //the reduce is just to be sure that we haven't document with same id
//                //Clustering: from <id, doc> to List<groupkey,doc>
//                .flatMapToPair(a -> {
//                    final MapDocument currentDocument = a._2();
//
//                    return Utility.getGroupingKeys(config, currentDocument).stream()
//                            .map(it -> new Tuple2<>(it, currentDocument)).collect(Collectors.toList()).iterator();
//                }).groupByKey();
//
//        Utility.deleteIfExists(groupsPath);
//        blocks.map(group -> new DocumentsBlock(group._1(), group._2())).saveAsTextFile(groupsPath);
//
//        //create relations by comparing only elements in the same group
//        final JavaPairRDD<String, String> relationRDD = blocks.flatMapToPair(it -> {
//            final SparkReporter reporter = new SparkReporter(accumulators);
//            new SparkBlockProcessor(config).process(it._1(), it._2(), reporter, accumulators);
//            return reporter.getRelations().iterator();
//        });
//
//        final RDD<Edge<String>> edgeRdd = relationRDD.map(it -> new Edge<>(it._1().hashCode(),it._2().hashCode(), "similarTo")).rdd();
//
//        JavaRDD<ConnectedComponent> ccs = GraphProcessor.findCCs(vertexes, edgeRdd, 20).toJavaRDD();
//
//        //save connected components on textfile
//        Utility.deleteIfExists(outputPath);
//        ccs.saveAsTextFile(outputPath);
//
//        final JavaRDD<ConnectedComponent> connectedComponents = ccs.filter(cc -> cc.getDocs().size()>1);
//        final JavaRDD<ConnectedComponent> nonDeduplicated = ccs.filter(cc -> cc.getDocs().size()==1);
//
//        System.out.println("Non duplicates: " + nonDeduplicated.count());
//        System.out.println("Duplicates: " + connectedComponents.flatMap(cc -> cc.getDocs().iterator()).count());
//        System.out.println("Connected Components: " + connectedComponents.count());
//        accumulators.forEach((name, acc) -> System.out.println(name + " -> " + acc.value()));
//
//    }
//
//}