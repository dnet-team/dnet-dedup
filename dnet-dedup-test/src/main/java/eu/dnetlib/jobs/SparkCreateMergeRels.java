package eu.dnetlib.jobs;

import eu.dnetlib.Deduper;
import eu.dnetlib.graph.JavaGraphProcessor;
import eu.dnetlib.pace.config.DedupConfig;
import eu.dnetlib.pace.util.MapDocumentUtil;
import eu.dnetlib.pace.utils.Utility;
import eu.dnetlib.support.ArgumentApplicationParser;
import eu.dnetlib.support.ConnectedComponent;
import eu.dnetlib.support.Relation;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.graphx.Edge;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.io.IOException;
import java.util.Optional;

import static eu.dnetlib.Deduper.hash;

public class SparkCreateMergeRels extends AbstractSparkJob {

    private static final Logger log = LoggerFactory.getLogger(SparkCreateMergeRels.class);

    public SparkCreateMergeRels(ArgumentApplicationParser parser, SparkSession spark) {
        super(parser, spark);
    }

    public static void main(String[] args) throws Exception {

        ArgumentApplicationParser parser = new ArgumentApplicationParser(
                Utility.readResource("/jobs/parameters/createMergeRels_parameters.json", SparkCreateMergeRels.class)
        );

        parser.parseArgument(args);

        SparkConf conf = new SparkConf();

        new SparkCreateMergeRels(
                parser,
                getSparkSession(conf)
        ).run();
    }

    @Override
    public void run() throws IOException {

        // read oozie parameters
        final String entitiesPath = parser.get("entitiesPath");
        final String workingPath = parser.get("workingPath");
        final String dedupConfPath = parser.get("dedupConfPath");
        final int numPartitions = Optional
                .ofNullable(parser.get("numPartitions"))
                .map(Integer::valueOf)
                .orElse(NUM_PARTITIONS);

        log.info("entitiesPath:  '{}'", entitiesPath);
        log.info("workingPath:   '{}'", workingPath);
        log.info("dedupConfPath: '{}'", dedupConfPath);
        log.info("numPartitions: '{}'", numPartitions);

        DedupConfig dedupConf = DedupConfig.load(readFileFromHDFS(dedupConfPath));

        JavaSparkContext sc = JavaSparkContext.fromSparkContext(spark.sparkContext());

        final JavaPairRDD<Object, String> vertexes = sc
                .textFile(entitiesPath)
                .map(s -> MapDocumentUtil.getJPathString(dedupConf.getWf().getIdPath(), s))
                .mapToPair((PairFunction<String, Object, String>) s -> new Tuple2<>(hash(s), s));

        final JavaRDD<Edge<String>> edgeRdd = spark
                .read()
                .load(workingPath + "/simrels")
                .as(Encoders.bean(Relation.class))
                .javaRDD()
                .map(Relation::toEdgeRdd);

        JavaRDD<ConnectedComponent> ccs = JavaGraphProcessor
                .findCCs(vertexes, edgeRdd, dedupConf.getWf().getMaxIterations())
                .toJavaRDD();

        JavaRDD<Relation> mergeRel = ccs
                .filter(k -> k.getDocs().size() > 1)
                .flatMap(cc -> Deduper.ccToMergeRel(cc, dedupConf))
                .map(it -> new Relation(it._1(), it._2(), "mergeRel"));

        final Dataset<Relation> mergeRels = spark
                .createDataset(
                        mergeRel.rdd(),
                        Encoders.bean(Relation.class));

        mergeRels.write().mode(SaveMode.Overwrite).parquet(workingPath + "/mergerels");

    }
}
