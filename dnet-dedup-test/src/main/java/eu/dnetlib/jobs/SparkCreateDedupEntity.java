package eu.dnetlib.jobs;

import eu.dnetlib.Deduper;
import eu.dnetlib.pace.config.DedupConfig;
import eu.dnetlib.pace.util.MapDocumentUtil;
import eu.dnetlib.pace.utils.Utility;
import eu.dnetlib.support.ArgumentApplicationParser;
import eu.dnetlib.support.ConnectedComponent;
import eu.dnetlib.support.Relation;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.io.IOException;
import java.util.Optional;

public class SparkCreateDedupEntity extends AbstractSparkJob {

        private static final Logger log = LoggerFactory.getLogger(eu.dnetlib.jobs.SparkCreateDedupEntity.class);

        public SparkCreateDedupEntity(ArgumentApplicationParser parser, SparkSession spark) {
            super(parser, spark);
        }

        public static void main(String[] args) throws Exception {

            ArgumentApplicationParser parser = new ArgumentApplicationParser(
                    Utility.readResource("/jobs/parameters/createDedupEntity_parameters.json", SparkCreateDedupEntity.class)
            );

            parser.parseArgument(args);

            SparkConf conf = new SparkConf();

            new SparkCreateDedupEntity(
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

            DedupConfig dedupConf = DedupConfig.load(readResource("/jobs/parameters/createDedupEntity_parameters.json", SparkCreateDedupEntity.class));

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
                    .load(workingPath + "/mergerels")
                    .as(Encoders.bean(Relation.class))
                    .toJavaRDD()
                    .mapToPair(r -> new Tuple2<>(r.getTarget(), r));

            JavaRDD<ConnectedComponent> dedupEntities = mergeRels.join(entities)
                    .mapToPair(t -> new Tuple2<>(t._2()._1().getSource(), t._2()._2()))
                    .groupByKey()
                    .map(t-> Deduper.entityMerger(t._1(), t._2().iterator()));

            dedupEntities.saveAsTextFile(workingPath + "dedupentity");

        }

}
