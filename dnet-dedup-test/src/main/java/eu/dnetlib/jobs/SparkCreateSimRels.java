package eu.dnetlib.jobs;

import eu.dnetlib.Deduper;
import eu.dnetlib.pace.utils.Utility;
import eu.dnetlib.support.ArgumentApplicationParser;
import org.apache.commons.io.IOUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class SparkCreateSimRels extends AbstractSparkJob {

    private static final Logger log = LoggerFactory.getLogger(SparkCreateSimRels.class);

    public SparkCreateSimRels(ArgumentApplicationParser parser, SparkSession spark) {
        super(parser, spark);
    }

    public static void main(String[] args) throws Exception {

        ArgumentApplicationParser parser = new ArgumentApplicationParser(
                Utility.readFromClasspath("/eu/dnetlib/pace/parameters/createSimRels_parameters.json", SparkCreateSimRels.class)
        );

        parser.parseArgument(args);

        SparkConf conf = new SparkConf();

        new SparkCreateSimRels(
                parser,
                getSparkSession(conf)
        ).run();
    }

    @Override
    public void run() {

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

        Deduper.createSimRels(
                loadDedupConfig(dedupConfPath),
                spark,
                entitiesPath,
                workingPath + "/simrels"
        );
    }
}
