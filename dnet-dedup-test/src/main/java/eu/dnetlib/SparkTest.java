package eu.dnetlib;

import com.google.common.collect.Sets;
import eu.dnetlib.pace.clustering.BlacklistAwareClusteringCombiner;
import eu.dnetlib.pace.config.DedupConfig;
import eu.dnetlib.pace.model.MapDocument;
import eu.dnetlib.pace.utils.PaceUtils;
import org.apache.commons.io.IOUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;


import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class SparkTest {

    class Results extends HashMap<String, Set<String>> {
        public Results(Set<String> keys) {
            super(keys.size());
            keys.forEach(k -> put(k, new HashSet<>()));
        }
    }


    public static void main(String[] args) {
        final JavaSparkContext context = new JavaSparkContext(new SparkConf().setAppName("Hello World").setMaster("local[*]"));
        final JavaRDD<String> dataRDD = context.textFile("file:///Users/sandro/Downloads/organizations.json");

        final Counters c = new Counters();

        long count = dataRDD.mapToPair(it -> {
            final DedupConfig config = DedupConfig.load(readFromClasspath("/eu/dnetlib/pace/organization.pace.conf"));
            MapDocument mapDocument = PaceUtils.asMapDocument(config, it);
            return new Tuple2<>(mapDocument.getIdentifier(), mapDocument);
        }).reduceByKey((a, b) -> a).flatMapToPair(a -> {
            final MapDocument currentDocument = a._2();
            final DedupConfig config = DedupConfig.load(readFromClasspath("/eu/dnetlib/pace/organization.pace.conf"));
            return getGroupingKeys(config, currentDocument).stream()
                    .map(it -> new Tuple2<>(it, currentDocument)).collect(Collectors.toList()).iterator();
        }).groupByKey().flatMapToPair(it -> {
            final DedupConfig config = DedupConfig.load(readFromClasspath("/eu/dnetlib/pace/organization.pace.conf"));
            return process(config, it, c).iterator();
        }).count();


        System.out.println("total Element = " + count);

//        final MapDocument resA = result(config, "A", "Recent results from CDF");
//        final MapDocument resB = result(config, "B", "Recent results from CDF");
//
//        final ScoreResult sr = new PaceDocumentDistance().between(resA, resB, config);
//        final double d = sr.getScore();
//        System.out.println(String.format(" d ---> %s", d));

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


    static List<Tuple2<String, String>> process(DedupConfig conf, Tuple2<String, Iterable<MapDocument>> entry, Counters c) {
        try {
            return new BlockProcessor(conf).process(entry._1(), StreamSupport.stream(entry._2().spliterator(),false), new Reporter() {
                @Override
                public void incrementCounter(String counterGroup, String counterName, long delta) {
                    c.get(counterGroup, counterName).addAndGet(delta);
                }

                @Override
                public void emit(String type, String from, String to) {

                }
            });
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }









}
