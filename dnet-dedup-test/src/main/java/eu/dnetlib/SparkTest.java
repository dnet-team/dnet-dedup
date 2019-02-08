package eu.dnetlib;

import com.google.common.collect.Sets;
import eu.dnetlib.graph.GraphProcessor;
import eu.dnetlib.pace.clustering.BlacklistAwareClusteringCombiner;
import eu.dnetlib.pace.config.DedupConfig;
import eu.dnetlib.pace.model.MapDocument;
import eu.dnetlib.pace.util.BlockProcessor;
import eu.dnetlib.pace.utils.PaceUtils;
import eu.dnetlib.reporter.SparkCounter;
import eu.dnetlib.reporter.SparkReporter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.graphx.Edge;
import org.apache.spark.rdd.RDD;
import scala.Tuple2;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.Set;
import java.util.stream.Collectors;

public class SparkTest {
    public static SparkCounter counter ;
    private static final Log log = LogFactory.getLog(SparkTest.class);

    public static void main(String[] args) {
        final JavaSparkContext context = new JavaSparkContext(new SparkConf().setAppName("Deduplication").setMaster("local[*]"));

        final URL dataset = SparkTest.class.getResource("/eu/dnetlib/pace/authors.json");//"/eu/dnetlib/pace/orgs2.json");
        final JavaRDD<String> dataRDD = context.textFile(dataset.getPath());

        counter = new SparkCounter(context);

        //read the configuration from the classpath
        final DedupConfig config = DedupConfig.load(readFromClasspath("/eu/dnetlib/pace/authors.test.pace.conf"));//"/eu/dnetlib/pace/organization.test2.pace.conf"));

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
        final JavaPairRDD<String, String> relationRDD = mapDocs.reduceByKey((a, b) -> a)    //the reduce is just to be sure that we haven't document with same id
                //Clustering: from <id, doc> to List<groupkey,doc>
                .flatMapToPair(a -> {
                    final MapDocument currentDocument = a._2();

                    return getGroupingKeys(config, currentDocument).stream()
                            .map(it -> new Tuple2<>(it, currentDocument)).collect(Collectors.toList()).iterator();
                }).groupByKey()     //group documents basing on the key
                //create relations by comparing only elements in the same group
                .flatMapToPair(it -> {
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

        connectedComponents.foreach(cc -> System.out.println("cc = " + cc.toString() + " size =" + cc.getDocs().size()));
        nonDeduplicated.foreach(cc -> System.out.println("nd = " + cc.toString()));

        //print ids
//        ccs.foreach(cc -> System.out.println(cc.getId()));
//        connectedComponents.saveAsTextFile("file:///Users/miconis/Downloads/dumps/organizations_dedup");

    }

    static String readFromClasspath(final String filename) {
        final StringWriter sw = new StringWriter();
        try {
            IOUtils.copy(SparkTest.class.getResourceAsStream(filename), sw);
            return sw.toString();
        } catch (final IOException e) {
            throw new RuntimeException("cannot load resource from classpath: " + filename);
        }
    }

    static Set<String> getGroupingKeys(DedupConfig conf, MapDocument doc) {
        return Sets.newHashSet(BlacklistAwareClusteringCombiner.filterAndCombine(doc, conf));
    }

}