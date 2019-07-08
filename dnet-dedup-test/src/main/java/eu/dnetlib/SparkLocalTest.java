package eu.dnetlib;

import com.google.common.collect.Iterables;
import eu.dnetlib.graph.GraphProcessor;
import eu.dnetlib.pace.config.DedupConfig;
import eu.dnetlib.pace.model.MapDocument;
import eu.dnetlib.pace.utils.PaceUtils;
import eu.dnetlib.reporter.SparkBlockProcessor;
import eu.dnetlib.reporter.SparkReporter;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.graphx.Edge;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.util.LongAccumulator;
import scala.Tuple2;

import java.net.URL;
import java.util.Map;
import java.util.stream.Collectors;

public class SparkLocalTest {

    public static void main(String[] args) {

        final SparkSession spark = SparkSession
                .builder()
                .appName("Deduplication")
                .master("local[*]")
                .getOrCreate();

        final JavaSparkContext context = new JavaSparkContext(spark.sparkContext());

        final URL dataset = SparkLocalTest.class.getResource("/eu/dnetlib/pace/organization.to.fix.json");
        final JavaRDD<String> dataRDD = context.textFile(dataset.getPath());

        //read the configuration from the classpath
        final DedupConfig config = DedupConfig.load(Utility.readFromClasspath("/eu/dnetlib/pace/org.curr.conf", SparkLocalTest.class));

        Map<String, LongAccumulator> accumulators = Utility.constructAccumulator(config, context.sc());

        //create vertexes of the graph: <ID, MapDocument>
        JavaPairRDD<String, MapDocument> mapDocs = dataRDD.mapToPair(it -> {
            MapDocument mapDocument = PaceUtils.asMapDocument(config, it);
            return new Tuple2<>(mapDocument.getIdentifier(), mapDocument);
        });

//        mapDocs.filter(d -> d._2().getFieldMap().get("doi").stringValue().length() > 0).foreach(d -> System.out.println(d));
//        mapDocs.filter(d -> d._2().getFieldMap().get("documentationUrl").stringValue().length() > 0).foreach(d -> System.out.println(d));

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
//        blocks = blocks.filter(b -> Iterables.size(b._2())>1);
////        vertexes = blocks.flatMap(b -> b._2().iterator()).map(t -> new Tuple2<Object, MapDocument>((long) t.getIdentifier().hashCode(), t)).rdd();
//        blocks.map(group -> new DocumentsBlock(group._1(), group._2())).foreach(b -> System.out.println(b));

        //create relations by comparing only elements in the same group
        final JavaPairRDD<String, String> relationRDD = blocks.flatMapToPair(it -> {
            final SparkReporter reporter = new SparkReporter();
            new SparkBlockProcessor(config).process(it._1(), it._2(), reporter, accumulators);
            return reporter.getReport().iterator();
        });

        final RDD<Edge<String>> edgeRdd = relationRDD.map(it -> new Edge<>(it._1().hashCode(),it._2().hashCode(), "similarTo")).rdd();

        JavaRDD<ConnectedComponent> ccs = GraphProcessor.findCCs(vertexes, edgeRdd, 20).toJavaRDD();

        final JavaRDD<ConnectedComponent> connectedComponents = ccs.filter(cc -> cc.getDocs().size()>1);
        final JavaRDD<ConnectedComponent> nonDeduplicated = ccs.filter(cc -> cc.getDocs().size()==1);

        //print deduped
        connectedComponents.foreach(cc -> {
            System.out.println(cc);
        });
        //print nondeduped
        nonDeduplicated.foreach(cc -> {
            System.out.println(cc);
        });

        System.out.println("Non duplicates: " + nonDeduplicated.count());
        System.out.println("Duplicates: " + connectedComponents.flatMap(cc -> cc.getDocs().iterator()).count());
        System.out.println("Connected Components: " + connectedComponents.count());

        accumulators.forEach((name, acc) -> System.out.println(name + " -> " + acc.value()));

//        //print ids
//        ccs.foreach(cc -> System.out.println(cc.getId()));
//        connectedComponents.saveAsTextFile("file:///Users/miconis/Downloads/dumps/organizations_dedup");

    }

}