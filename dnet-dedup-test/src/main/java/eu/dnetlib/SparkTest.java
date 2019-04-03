package eu.dnetlib;

import eu.dnetlib.graph.GraphProcessor;
import eu.dnetlib.pace.config.DedupConfig;
import eu.dnetlib.pace.model.MapDocument;
import eu.dnetlib.pace.util.BlockProcessor;
import eu.dnetlib.pace.utils.PaceUtils;
import eu.dnetlib.reporter.SparkCounter;
import eu.dnetlib.reporter.SparkReporter;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.graphx.Edge;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.SparkSession;
import scala.Tuple2;

import java.io.IOException;
import java.util.stream.Collectors;

public class SparkTest {
    public static SparkCounter counter ;

    public static void main(String[] args) throws IOException {

        final SparkSession spark = SparkSession
                .builder()
                .appName("Deduplication")
                .master("yarn")
                .getOrCreate();

        final JavaSparkContext context = new JavaSparkContext(spark.sparkContext());

        final JavaRDD<String> dataRDD = Utility.loadDataFromHDFS(args[0], context);

        counter = new SparkCounter(context);

        final DedupConfig config = Utility.loadConfigFromHDFS(args[1]);

        BlockProcessor.constructAccumulator(config);
        BlockProcessor.accumulators.forEach(acc -> {

            final String[] values = acc.split("::");
            counter.incrementCounter(values[0], values[1], 0);

        });

        //create vertexes of the graph: <ID, MapDocument>
        JavaPairRDD<String, MapDocument> mapDocs = dataRDD.mapToPair(it -> {
            MapDocument mapDocument = PaceUtils.asMapDocument(config, it);
            return new Tuple2<>(mapDocument.getIdentifier(), mapDocument);
        });
        RDD<Tuple2<Object, MapDocument>> vertexes = mapDocs.mapToPair(t -> new Tuple2<Object, MapDocument>( (long) t._1().hashCode(), t._2())).rdd();

        //create relations between documents
        JavaPairRDD<String, Iterable<MapDocument>> blocks = mapDocs.reduceByKey((a, b) -> a)    //the reduce is just to be sure that we haven't document with same id
                //Clustering: from <id, doc> to List<groupkey,doc>
                .flatMapToPair(a -> {
                    final MapDocument currentDocument = a._2();

                    return Utility.getGroupingKeys(config, currentDocument).stream()
                            .map(it -> new Tuple2<>(it, currentDocument)).collect(Collectors.toList()).iterator();
                }).groupByKey();//group documents basing on the key

        //print blocks
        blocks.foreach(b -> {
            String print = b._1() + ": ";
            for (MapDocument doc : b._2()) {
                print += doc.getIdentifier() + " ";
            }
            System.out.println(print);
        });

        //create relations by comparing only elements in the same group
        final JavaPairRDD<String, String> relationRDD = blocks.flatMapToPair(it -> {
                    final SparkReporter reporter = new SparkReporter(counter);
                    new BlockProcessor(config).process(it._1(), it._2(), reporter);
                    return reporter.getReport().iterator();
                });

        final RDD<Edge<String>> edgeRdd = relationRDD.map(it -> new Edge<>(it._1().hashCode(),it._2().hashCode(), "similarTo")).rdd();

        JavaRDD<ConnectedComponent> ccs = GraphProcessor.findCCs(vertexes, edgeRdd, 20).toJavaRDD();

        final JavaRDD<ConnectedComponent> connectedComponents = ccs.filter(cc -> cc.getDocs().size()>1);
        final JavaRDD<ConnectedComponent> nonDeduplicated = ccs.filter(cc -> cc.getDocs().size()==1);

        System.out.println("Non duplicates: " + nonDeduplicated.count());
        System.out.println("Duplicates: " + connectedComponents.flatMap(cc -> cc.getDocs().iterator()).count());
        System.out.println("Connected Components: " + connectedComponents.count());

        counter.getAccumulators().values().forEach(it-> System.out.println(it.getGroup()+" "+it.getName()+" -->"+it.value()));

        //print deduped
        connectedComponents.foreach(cc -> {
            System.out.println("cc = " + cc.getId());
            for (MapDocument doc: cc.getDocs()) {
                System.out.println(doc.getIdentifier() + "; ln: " + doc.getFieldMap().get("legalname").stringValue() + "; sn: " + doc.getFieldMap().get("legalshortname").stringValue());
            }
        });
        //print nondeduped
        nonDeduplicated.foreach(cc -> {
            System.out.println("nd = " + cc.getId());
            System.out.println(cc.getDocs().iterator().next().getFieldMap().get("legalname").stringValue() + "; sn: " + cc.getDocs().iterator().next().getFieldMap().get("legalshortname").stringValue());
        });

//        print ids
//        ccs.foreach(cc -> System.out.println(cc.getId()));
//        connectedComponents.saveAsTextFile("file:///Users/miconis/Downloads/dumps/organizations_dedup");

    }

}