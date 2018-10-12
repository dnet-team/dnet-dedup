package eu.dnetlib;

import com.google.common.collect.Sets;
import eu.dnetlib.graph.GraphProcessor;
import eu.dnetlib.pace.clustering.BlacklistAwareClusteringCombiner;
import eu.dnetlib.pace.config.DedupConfig;
import eu.dnetlib.pace.model.MapDocument;
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
import java.util.Set;
import java.util.stream.Collectors;


public class SparkTest {
    public static SparkCounter counter ;
    private static final Log log = LogFactory.getLog(SparkTest.class);

    public static void main(String[] args) {
        final JavaSparkContext context = new JavaSparkContext(new SparkConf().setAppName("Hello World").setMaster("local[*]"));
        final JavaRDD<String> dataRDD = context.textFile("file:///Users/sandro/Downloads/software.json");

        counter = new SparkCounter(context);

        final DedupConfig config = DedupConfig.load(readFromClasspath("/eu/dnetlib/pace/software.pace.conf"));
        BlockProcessor.constructAccumulator(config);

        BlockProcessor.accumulators.forEach(acc -> {

            final String[] values = acc.split("::");
            counter.incrementCounter(values[0], values[1], 0);

        });

        JavaPairRDD<String, MapDocument> mapDocs = dataRDD.mapToPair(it -> {
            MapDocument mapDocument = PaceUtils.asMapDocument(config, it);
            return new Tuple2<>(mapDocument.getIdentifier(), mapDocument);
        });


        final JavaPairRDD<String, String> relationRDD = mapDocs.reduceByKey((a, b) -> a)
                .flatMapToPair(a -> {
                    final MapDocument currentDocument = a._2();
                    return getGroupingKeys(config, currentDocument).stream()
                            .map(it -> new Tuple2<>(it, currentDocument)).collect(Collectors.toList()).iterator();
                }).groupByKey().flatMapToPair(it -> {

                    final SparkReporter reporter = new SparkReporter(counter);
                    new BlockProcessor(config).process(it._1(), it._2(), reporter);
                    return reporter.getReport().iterator();
                });

        RDD<Tuple2<Object, String>> vertexes = relationRDD.groupByKey().map(it -> {

            Long id = (long) it._1().hashCode();
            return new Tuple2<Object, String>(id, it._1());

        }).rdd();

        final RDD<Edge<String>> edgeRdd = relationRDD.map(it -> new Edge<>(it._1().hashCode(), it._2().hashCode(), "similarTo")).rdd();

        Tuple2<Object, RDD<String>> cc = GraphProcessor.findCCs(vertexes, edgeRdd, 20);

        final Long total = (Long) cc._1();


        final JavaRDD<String> map = mapDocs.map(Tuple2::_1);


        final JavaRDD<String> duplicatesRDD = cc._2().toJavaRDD();


        final JavaRDD<String> nonDuplicates = map.subtract(duplicatesRDD);


        relationRDD.collect().forEach(it-> System.out.println(it._1()+"<--->"+it._2()));

        System.out.println("Non duplicates: "+ nonDuplicates.count());
        System.out.println("Connected Components: "+ total);

        counter.getAccumulators().values().forEach(it-> System.out.println(it.getGroup()+" "+it.getName()+" -->"+it.value()));





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
