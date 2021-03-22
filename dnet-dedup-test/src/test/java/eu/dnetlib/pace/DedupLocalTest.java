package eu.dnetlib.pace;

import eu.dnetlib.Deduper;
import eu.dnetlib.jobs.SparkCreateDedupEntity;
import eu.dnetlib.jobs.SparkCreateMergeRels;
import eu.dnetlib.jobs.SparkCreateSimRels;
import eu.dnetlib.pace.config.DedupConfig;
import eu.dnetlib.pace.utils.Utility;
import eu.dnetlib.support.ArgumentApplicationParser;
import org.apache.commons.io.FileUtils;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

@Disabled
public class DedupLocalTest extends DedupTestUtils {

    static SparkSession spark;
    static DedupConfig config;
    static JavaSparkContext context;


    final String entitiesPath   = Paths
            .get(DedupLocalTest.class.getResource("/eu/dnetlib/pace/examples/orgs_dump").toURI())
            .toFile()
            .getAbsolutePath();
    final static String workingPath    = "/tmp/working_dir";
    final static String numPartitions  = "20";

    final static String dedupConfPath   = "/eu/dnetlib/pace/config/orgs.tree.conf.json";
    final static String simRelsPath     = workingPath + "/organization_simrel";
    final static String mergeRelsPath   = workingPath + "/organization_mergerel";
    final static String dedupEntityPath = workingPath + "/organization_dedupentity";

    public DedupLocalTest() throws URISyntaxException {
    }

    public static void cleanup() throws IOException {
        //remove directories to clean workspace
        FileUtils.deleteDirectory(new File(simRelsPath));
        FileUtils.deleteDirectory(new File(mergeRelsPath));
        FileUtils.deleteDirectory(new File(dedupEntityPath));
    }

    @BeforeAll
    public static void setup() throws IOException {

        cleanup();

        config = DedupConfig.load(Utility.readFromClasspath("/eu/dnetlib/pace/config/orgs.tree.conf.json", DedupLocalTest.class));

        spark = SparkSession
                .builder()
                .appName("Deduplication")
                .master("local[*]")
                .getOrCreate();
        context = JavaSparkContext.fromSparkContext(spark.sparkContext());

    }

    @Test
    public void createSimRelTest() throws Exception {

        ArgumentApplicationParser parser = new ArgumentApplicationParser(Utility.readResource("/eu/dnetlib/pace/parameters/createSimRels_parameters.json", SparkCreateSimRels.class));

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

        ArgumentApplicationParser parser = new ArgumentApplicationParser(Utility.readResource("/eu/dnetlib/pace/parameters/createMergeRels_parameters.json", SparkCreateMergeRels.class));

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

        ArgumentApplicationParser parser = new ArgumentApplicationParser(Utility.readResource("/eu/dnetlib/pace/parameters/createDedupEntity_parameters.json", SparkCreateDedupEntity.class));

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
    public void deduplicationTest() throws IOException {

        long before_simrels = System.currentTimeMillis();
        Deduper.createSimRels(
                config,
                spark,
                entitiesPath,
                simRelsPath
        );
        long simrels_time = System.currentTimeMillis() - before_simrels;

        long simrels_number = spark.read().load(simRelsPath).count();

        long before_mergerels = System.currentTimeMillis();
        Deduper.createMergeRels(
                config,
                entitiesPath,
                mergeRelsPath,
                simRelsPath,
                spark
        );
        long mergerels_time = System.currentTimeMillis() - before_mergerels;

        long mergerels_number = spark.read().load(mergeRelsPath).count();

        long before_dedupentity = System.currentTimeMillis();
        Deduper.createDedupEntity(
                config,
                mergeRelsPath,
                entitiesPath,
                spark,
                dedupEntityPath
        );
        long dedupentity_time = System.currentTimeMillis() - before_dedupentity;

        long dedupentity_number = context.textFile(dedupEntityPath).count();

        System.out.println("Number of simrels                   : " + simrels_number);
        System.out.println("Number of mergerels                 : " + mergerels_number);
        System.out.println("Number of dedupentities             : " + dedupentity_number);
        System.out.println("Total time for simrels creation     : " + simrels_time);
        System.out.println("Total time for mergerels creation   : " + mergerels_time);
        System.out.println("Total time for dedupentity creation : " + dedupentity_time);

        cleanup();
    }
}