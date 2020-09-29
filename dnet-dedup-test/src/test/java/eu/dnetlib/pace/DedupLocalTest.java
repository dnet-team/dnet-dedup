package eu.dnetlib.pace;

import eu.dnetlib.Deduper;
import eu.dnetlib.jobs.SparkCreateDedupEntity;
import eu.dnetlib.jobs.SparkCreateMergeRels;
import eu.dnetlib.jobs.SparkCreateSimRels;
import eu.dnetlib.pace.config.DedupConfig;
import eu.dnetlib.pace.utils.Utility;
import eu.dnetlib.support.ArgumentApplicationParser;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class DedupLocalTest extends DedupTestUtils {

    SparkSession spark;
    DedupConfig config;
    JavaSparkContext context;

    final String entitiesPath   = "/Users/miconis/IdeaProjects/DnetDedup/dnet-dedup/dnet-dedup-test/src/test/resources/eu/dnetlib/pace/examples/openorgs.to.fix.json";
    final String workingPath    = "/tmp/working_dir";
    final String numPartitions  = "10";
    final String dedupConfPath  = "/eu/dnetlib/pace/config/organization.strict.conf.json";

    @Before
    public void setup() {

        config = DedupConfig.load(Utility.readFromClasspath("/eu/dnetlib/pace/config/organization.strict.conf.json", DedupLocalTest.class));

        spark = SparkSession
                .builder()
                .appName("Deduplication")
                .master("local[*]")
                .getOrCreate();
        context = JavaSparkContext.fromSparkContext(spark.sparkContext());

    }

    @Test
    public void createSimRelTest() throws Exception {

        ArgumentApplicationParser parser = new ArgumentApplicationParser(Utility.readFromClasspath("/eu/dnetlib/pace/parameters/createSimRels_parameters.json", SparkCreateSimRels.class));

        parser.parseArgument(
                        new String[] {
                                "-e", entitiesPath,
                                "-w", workingPath,
                                "-np", numPartitions,
                                "-dc", dedupConfPath
                        });

        new SparkCreateSimRels(
                parser,
                spark
        ).run();
    }

    @Test
    public void createMergeRelTest() throws Exception {

        ArgumentApplicationParser parser = new ArgumentApplicationParser(Utility.readFromClasspath("/eu/dnetlib/pace/parameters/createMergeRels_parameters.json", SparkCreateMergeRels.class));

        parser.parseArgument(
                new String[] {
                        "-e", entitiesPath,
                        "-w", workingPath,
                        "-np", numPartitions,
                        "-dc", dedupConfPath
                });

        new SparkCreateMergeRels(
                parser,
                spark
        ).run();
    }

    @Test
    public void createDedupEntityTest() throws Exception {

        ArgumentApplicationParser parser = new ArgumentApplicationParser(Utility.readFromClasspath("/eu/dnetlib/pace/parameters/createDedupEntity_parameters.json", SparkCreateDedupEntity.class));

        parser.parseArgument(
                new String[] {
                        "-e", entitiesPath,
                        "-w", workingPath,
                        "-np", numPartitions,
                        "-dc", dedupConfPath
                });

        new SparkCreateDedupEntity(
                parser,
                spark
        ).run();
    }

    @Test
    public void deduplicationTest() {

        Deduper.createSimRels(
                config,
                spark,
                entitiesPath,
                "/tmp/deduptest/publication_simrel"
        );

        Deduper.createMergeRels(
                config,
                entitiesPath,
                "/tmp/deduptest/publication_mergerel",
                "/tmp/deduptest/publication_simrel",
                spark
        );

        Deduper.createDedupEntity(
                config,
                "/tmp/deduptest/publication_mergerel",
                entitiesPath,
                spark,
                "/tmp/deduptest/dedupentity"
        );

    }

}