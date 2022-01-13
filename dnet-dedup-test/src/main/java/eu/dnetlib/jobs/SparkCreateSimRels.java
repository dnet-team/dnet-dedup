package eu.dnetlib.jobs;

import eu.dnetlib.Deduper;
import eu.dnetlib.pace.config.DedupConfig;
import eu.dnetlib.pace.model.MapDocument;
import eu.dnetlib.pace.util.MapDocumentUtil;
import eu.dnetlib.pace.utils.Utility;
import eu.dnetlib.support.ArgumentApplicationParser;
import eu.dnetlib.support.Block;
import eu.dnetlib.support.Relation;
import org.apache.commons.io.IOUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.io.IOException;
import java.util.Optional;

public class SparkCreateSimRels extends AbstractSparkJob {

    private static final Logger log = LoggerFactory.getLogger(SparkCreateSimRels.class);

    public SparkCreateSimRels(ArgumentApplicationParser parser, SparkSession spark) {
        super(parser, spark);
    }

    public static void main(String[] args) throws Exception {

        ArgumentApplicationParser parser = new ArgumentApplicationParser(
                readResource("/jobs/parameters/createSimRels_parameters.json", SparkCreateSimRels.class)
        );

        parser.parseArgument(args);

        SparkConf conf = new SparkConf();

        new SparkCreateSimRels(
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
        final boolean useTree = Boolean.parseBoolean(parser.get("useTree"));

        log.info("entitiesPath:  '{}'", entitiesPath);
        log.info("workingPath:   '{}'", workingPath);
        log.info("dedupConfPath: '{}'", dedupConfPath);
        log.info("numPartitions: '{}'", numPartitions);
        log.info("useTree:       '{}'", useTree);

        JavaSparkContext sc = JavaSparkContext.fromSparkContext(spark.sparkContext());
        DedupConfig dedupConfig = loadDedupConfig(dedupConfPath);

        JavaPairRDD<String, MapDocument> mapDocuments = sc
                .textFile(entitiesPath)
                .repartition(numPartitions)
                .mapToPair(
                        (PairFunction<String, String, MapDocument>) s -> {
                            MapDocument d = MapDocumentUtil.asMapDocumentWithJPath(dedupConfig, s);
                            return new Tuple2<>(d.getIdentifier(), d);
                        });

        // create blocks for deduplication
        JavaPairRDD<String, Block> blocks = Deduper.createSortedBlocks(mapDocuments, dedupConfig);

        // create relations by comparing only elements in the same group
        JavaRDD<Relation> relations = Deduper.computeRelations(sc, blocks, dedupConfig, useTree, false);

        // save the simrel in the workingdir
        spark
                .createDataset(relations.rdd(), Encoders.bean(Relation.class))
                .write()
                .mode(SaveMode.Overwrite)
                .save(workingPath + "/simrels");
    }
}
