package eu.dnetlib.pace;

import eu.dnetlib.Deduper;
import eu.dnetlib.jobs.SparkCreateDedupEntity;
import eu.dnetlib.jobs.SparkCreateMergeRels;
import eu.dnetlib.jobs.SparkCreateSimRels;
import eu.dnetlib.pace.config.DedupConfig;
import eu.dnetlib.pace.model.MapDocument;
import eu.dnetlib.pace.tree.support.TreeProcessor;
import eu.dnetlib.pace.util.MapDocumentUtil;
import eu.dnetlib.pace.utils.Utility;
import eu.dnetlib.support.ArgumentApplicationParser;
import eu.dnetlib.support.Block;
import eu.dnetlib.support.Relation;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.ForeachFunction;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import scala.Tuple2;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DedupLocalTest extends DedupTestUtils {

    static SparkSession spark;
    static DedupConfig config;
    static JavaSparkContext context;

    final String entitiesPath   = Paths
            .get(DedupLocalTest.class.getResource("/eu/dnetlib/pace/examples/organization").toURI())
            .toFile()
            .getAbsolutePath();

    final static String workingPath    = "/tmp/working_dir";
    final static String numPartitions  = "20";

    final String dedupConfPath = Paths
            .get(DedupLocalTest.class.getResource("/eu/dnetlib/pace/config/orgs.tree.conf.json").toURI())
            .toFile()
            .getAbsolutePath();

    final static String simRelsPath     = workingPath + "/simrels";
    final static String mergeRelsPath   = workingPath + "/mergerels";
    final static String dedupEntityPath = workingPath + "/dedupentities";

    public DedupLocalTest() throws URISyntaxException {
    }

    public static void cleanup() throws IOException {
        //remove directories to clean workspace
        FileUtils.deleteDirectory(new File(simRelsPath));
        FileUtils.deleteDirectory(new File(mergeRelsPath));
        FileUtils.deleteDirectory(new File(dedupEntityPath));
    }

    @BeforeAll
    public void setup() throws IOException {

        cleanup();

        config = DedupConfig.load(readFileFromHDFS(dedupConfPath));

        spark = SparkSession
                .builder()
                .appName("Deduplication")
                .master("local[*]")
                .getOrCreate();
        context = JavaSparkContext.fromSparkContext(spark.sparkContext());

    }

    @AfterAll
    public static void finalCleanUp() throws IOException {
        cleanup();
    }

    protected static String readFileFromHDFS(String filePath) throws IOException {

        Path path=new Path(filePath);
        FileSystem fs = FileSystem.get(new Configuration());
        BufferedReader br=new BufferedReader(new InputStreamReader(fs.open(path)));
        try {
            return String.join("", br.lines().collect(Collectors.toList()));
        } finally {
            br.close();
        }
    }

    @Test
    @Order(1)
    public void createSimRelTest() throws Exception {

        ArgumentApplicationParser parser = new ArgumentApplicationParser(Utility.readResource("/eu/dnetlib/pace/parameters/createSimRels_parameters.json", SparkCreateSimRels.class));

        parser.parseArgument(
                        new String[] {
                                "-e", entitiesPath,
                                "-w", workingPath,
                                "-np", numPartitions,
                                "-dc", dedupConfPath,
                                "-ut", "true"
                        });

        new SparkCreateSimRels(
                parser,
                spark
        ).run();

        long simrels_number = spark.read().load(simRelsPath).count();
        System.out.println("simrels_number = " + simrels_number);
    }

    @Test
    @Order(2)
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
    @Order(3)
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

    @Test //full deduplication workflow test
    @Ignore
    public void deduplicationTest() throws Exception {

        //custom parameters for this test
        DedupConfig dedupConfig = DedupConfig.load(readFileFromHDFS(
                Paths.get(DedupLocalTest.class.getResource("/eu/dnetlib/pace/config/pub.instancetype.tree.conf.json").toURI()).toFile().getAbsolutePath()
        ));
        String inputPath = Paths.get(DedupLocalTest.class.getResource("/eu/dnetlib/pace/examples/publications.dump.1000.json").toURI()).toFile().getAbsolutePath();
        String simRelsPath = workingPath + "/simrels";
        String mergeRelsPath = workingPath + "/mergerels";
        String outputPath = workingPath + "/dedup";

        long before_simrels = System.currentTimeMillis();
        Deduper.createSimRels(
                dedupConfig,
                spark,
                inputPath,
                simRelsPath,
                true,
                false
        );
        long simrels_time = System.currentTimeMillis() - before_simrels;

        long simrels_number = spark.read().load(simRelsPath).count();

        long before_mergerels = System.currentTimeMillis();
        Deduper.createMergeRels(
                dedupConfig,
                inputPath,
                mergeRelsPath,
                simRelsPath,
                spark
        );
        long mergerels_time = System.currentTimeMillis() - before_mergerels;

        long mergerels_number = spark.read().load(mergeRelsPath).count();

        long before_dedupentity = System.currentTimeMillis();
        Deduper.createDedupEntity(
                dedupConfig,
                mergeRelsPath,
                inputPath,
                spark,
                outputPath
        );
        long dedupentity_time = System.currentTimeMillis() - before_dedupentity;

        long dedupentity_number = context.textFile(outputPath).count();

        System.out.println("Number of simrels                   : " + simrels_number);
        System.out.println("Number of mergerels                 : " + mergerels_number);
        System.out.println("Number of dedupentities             : " + dedupentity_number);
        System.out.println("Total time for simrels creation     : " + simrels_time);
        System.out.println("Total time for mergerels creation   : " + mergerels_time);
        System.out.println("Total time for dedupentity creation : " + dedupentity_time);

        FileUtils.deleteDirectory(new File(workingPath));
    }

    @Test //test the match between two JSON
    @Ignore
    public void matchTest() throws Exception {
        String json1 = "{\"context\": [], \"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"sysimport:crosswalk:repository\", \"classname\": \"sysimport:crosswalk:repository\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}, \"inferred\": true, \"inferenceprovenance\": \"dedup-similarity-result-levenstein\", \"invisible\": false, \"trust\": \"0.9\"}, \"resourcetype\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"pid\": [{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"qualifier\": {\"classid\": \"pmc\", \"classname\": \"pmc\", \"schemename\": \"dnet:pid_types\", \"schemeid\": \"dnet:pid_types\"}, \"value\": \"PMC1688333\"}], \"contributor\": [], \"resulttype\": {\"classid\": \"publication\", \"classname\": \"publication\", \"schemename\": \"dnet:result_typologies\", \"schemeid\": \"dnet:result_typologies\"}, \"relevantdate\": [], \"collectedfrom\": [{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"PubMed Central\", \"key\": \"10|opendoar____::eda80a3d5b344bc40f3bc04f65b7a357\"}], \"id\": \"50|od_______267::12220bb41c4f62ee8ae2ec051b22444d\", \"subject\": [{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"qualifier\": {\"classid\": \"keyword\", \"classname\": \"keyword\", \"schemename\": \"dnet:subject_classification_typologies\", \"schemeid\": \"dnet:subject_classification_typologies\"}, \"value\": \"Correspondence\"}], \"embargoenddate\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"lastupdatetimestamp\": 0, \"author\": [{\"surname\": \"Smith\", \"name\": \"S. L. H.\", \"pid\": [], \"rank\": 1, \"affiliation\": [], \"fullname\": \"Smith, S L H\"}], \"instance\": [{\"refereed\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"hostedby\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"PubMed Central\", \"key\": \"10|opendoar____::eda80a3d5b344bc40f3bc04f65b7a357\"}, \"processingchargeamount\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"license\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"url\": [\"https://europepmc.org/articles/PMC1688333/\"], \"distributionlocation\": \"\", \"processingchargecurrency\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"dateofacceptance\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"1976-09-18\"}, \"collectedfrom\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"PubMed Central\", \"key\": \"10|opendoar____::eda80a3d5b344bc40f3bc04f65b7a357\"}, \"accessright\": {\"classid\": \"OPEN\", \"classname\": \"Open Access\", \"schemename\": \"dnet:access_modes\", \"schemeid\": \"dnet:access_modes\"}, \"instancetype\": {\"classid\": \"0038\", \"classname\": \"Other literature type\", \"schemename\": \"dnet:publication_resource\", \"schemeid\": \"dnet:publication_resource\"}}], \"dateofcollection\": \"2019-07-01T18:28:03Z\", \"fulltext\": [], \"dateoftransformation\": \"\", \"description\": [{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}], \"format\": [], \"journal\": {\"issnPrinted\": \"\", \"conferencedate\": \"\", \"conferenceplace\": \"\", \"name\": \"\", \"edition\": \"\", \"vol\": \"\", \"sp\": \"\", \"iss\": \"\", \"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"issnOnline\": \"\", \"ep\": \"\", \"issnLinking\": \"\"}, \"coverage\": [], \"publisher\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"language\": {\"classid\": \"eng\", \"classname\": \"English\", \"schemename\": \"dnet:languages\", \"schemeid\": \"dnet:languages\"}, \"bestaccessright\": {\"classid\": \"OPEN\", \"classname\": \"Open Access\", \"schemename\": \"dnet:access_modes\", \"schemeid\": \"dnet:access_modes\"}, \"country\": [], \"extraInfo\": [], \"originalId\": [\"oai:pubmedcentral.nih.gov:1688333\"], \"source\": [], \"dateofacceptance\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"1976-09-18\"}, \"title\": [{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"qualifier\": {\"classid\": \"main title\", \"classname\": \"main title\", \"schemename\": \"dnet:dataCite_title\", \"schemeid\": \"dnet:dataCite_title\"}, \"value\": \"Admission of old people to hospital\"}]}";
        String json2 = "{\"context\": [], \"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"sysimport:actionset\", \"classname\": \"sysimport:actionset\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}, \"inferred\": true, \"inferenceprovenance\": \"dedup-similarity-result-levenstein\", \"invisible\": false, \"trust\": \"0.9\"}, \"resourcetype\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"pid\": [{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"qualifier\": {\"classid\": \"doi\", \"classname\": \"doi\", \"schemename\": \"dnet:pid_types\", \"schemeid\": \"dnet:pid_types\"}, \"value\": \"10.1136/bmj.2.6039.812-d\"}], \"contributor\": [], \"resulttype\": {\"classid\": \"publication\", \"classname\": \"publication\", \"schemename\": \"dnet:result_typologies\", \"schemeid\": \"dnet:result_typologies\"}, \"relevantdate\": [{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"qualifier\": {\"classid\": \"published-print\", \"classname\": \"published-print\", \"schemename\": \"dnet:dataCite_date\", \"schemeid\": \"dnet:dataCite_date\"}, \"value\": \"1976-10-2\"}], \"collectedfrom\": [{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"Crossref\", \"key\": \"10|openaire____::081b82f96300b6a6e3d282bad31cb6e2\"}, {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"Microsoft Academic Graph\", \"key\": \"10|openaire____::5f532a3fc4f1ea403f37070f59a7a53a\"}, {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"UnpayWall\", \"key\": \"10|openaire____::8ac8380272269217cb09a928c8caa993\"}], \"id\": \"50|doiboost____::faf91584ab611ecac7be042c95e82939\", \"subject\": [{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"qualifier\": {\"classid\": \"keyword\", \"classname\": \"keyword\", \"schemename\": \"dnet:subject\", \"schemeid\": \"dnet:subject\"}, \"value\": \"General Medicine\"}], \"embargoenddate\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"lastupdatetimestamp\": 0, \"author\": [{\"surname\": \"Campbell\", \"name\": \"A G M\", \"pid\": [{\"qualifier\": {\"classid\": \"MAG Identifier\", \"classname\": \"MAG Identifier\"}, \"value\": \"2424823041\"}], \"rank\": 1, \"affiliation\": [], \"fullname\": \"A G M Campbell\"}], \"instance\": [{\"refereed\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"hostedby\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"Unknown Repository\", \"key\": \"10|openaire____::55045bd2a65019fd8e6741a755395c8c\"}, \"processingchargeamount\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"license\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"url\": [\"http://europepmc.org/articles/pmc1688323?pdf=render\"], \"distributionlocation\": \"\", \"processingchargecurrency\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"dateofacceptance\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"collectedfrom\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"UnpayWall\", \"key\": \"10|openaire____::8ac8380272269217cb09a928c8caa993\"}, \"accessright\": {\"classid\": \"OPEN\", \"classname\": \"Open Access\", \"schemename\": \"dnet:access_modes\", \"schemeid\": \"dnet:access_modes\"}, \"instancetype\": {\"classid\": \"0001\", \"classname\": \"Article\", \"schemename\": \"dnet:publication_resource\", \"schemeid\": \"dnet:publication_resource\"}}, {\"refereed\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"hostedby\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"Unknown Repository\", \"key\": \"10|openaire____::55045bd2a65019fd8e6741a755395c8c\"}, \"processingchargeamount\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"license\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"url\": [\"https://syndication.highwire.org/content/doi/10.1136/bmj.2.6039.812-d\"], \"distributionlocation\": \"\", \"processingchargecurrency\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"dateofacceptance\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"collectedfrom\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"Crossref\", \"key\": \"10|openaire____::081b82f96300b6a6e3d282bad31cb6e2\"}, \"accessright\": {\"classid\": \"UNKNOWN\", \"classname\": \"not available\", \"schemename\": \"dnet:access_modes\", \"schemeid\": \"dnet:access_modes\"}, \"instancetype\": {\"classid\": \"0001\", \"classname\": \"Article\", \"schemename\": \"dnet:publication_resource\", \"schemeid\": \"dnet:publication_resource\"}}, {\"refereed\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"hostedby\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"Unknown Repository\", \"key\": \"10|openaire____::55045bd2a65019fd8e6741a755395c8c\"}, \"processingchargeamount\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"license\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"url\": [\"https://academic.microsoft.com/#/detail/2054538157\"], \"distributionlocation\": \"\", \"processingchargecurrency\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"dateofacceptance\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"collectedfrom\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"Microsoft Academic Graph\", \"key\": \"10|openaire____::5f532a3fc4f1ea403f37070f59a7a53a\"}, \"accessright\": {\"classid\": \"UNKNOWN\", \"classname\": \"not available\", \"schemename\": \"dnet:access_modes\", \"schemeid\": \"dnet:access_modes\"}, \"instancetype\": {\"classid\": \"0001\", \"classname\": \"Article\", \"schemename\": \"dnet:publication_resource\", \"schemeid\": \"dnet:publication_resource\"}}, {\"refereed\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"hostedby\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"Unknown Repository\", \"key\": \"10|openaire____::55045bd2a65019fd8e6741a755395c8c\"}, \"processingchargeamount\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"license\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"url\": [\"http://dx.doi.org/10.1136/bmj.2.6039.812-d\"], \"distributionlocation\": \"\", \"processingchargecurrency\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"dateofacceptance\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"collectedfrom\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"Crossref\", \"key\": \"10|openaire____::081b82f96300b6a6e3d282bad31cb6e2\"}, \"accessright\": {\"classid\": \"RESTRICTED\", \"classname\": \"Restricted\", \"schemename\": \"dnet:access_modes\", \"schemeid\": \"dnet:access_modes\"}, \"instancetype\": {\"classid\": \"0001\", \"classname\": \"Article\", \"schemename\": \"dnet:publication_resource\", \"schemeid\": \"dnet:publication_resource\"}}], \"dateofcollection\": \"2019-02-15\", \"fulltext\": [], \"dateoftransformation\": \"\", \"description\": [], \"format\": [], \"journal\": {\"issnPrinted\": \"0959-8138\", \"conferencedate\": \"\", \"conferenceplace\": \"\", \"name\": \"BMJ\", \"edition\": \"\", \"vol\": \"\", \"sp\": \"\", \"iss\": \"\", \"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"issnOnline\": \"1468-5833\", \"ep\": \"\", \"issnLinking\": \"\"}, \"coverage\": [], \"publisher\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"language\": {\"classid\": \"und\", \"classname\": \"Undetermined\", \"schemename\": \"dnet:languages\", \"schemeid\": \"dnet:languages\"}, \"bestaccessright\": {\"classid\": \"OPEN\", \"classname\": \"Open Access\", \"schemename\": \"dnet:access_modes\", \"schemeid\": \"dnet:access_modes\"}, \"country\": [], \"extraInfo\": [], \"originalId\": [\"10.1136/bmj.2.6039.812-d\"], \"source\": [], \"dateofacceptance\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"1976-10-2\"}, \"title\": [{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"qualifier\": {\"classid\": \"main title\", \"classname\": \"main title\", \"schemename\": \"dnet:dataCite_title\", \"schemeid\": \"dnet:dataCite_title\"}, \"value\": \"Admission of old people to hospital\"}]}";

        DedupConfig config = DedupConfig.load(readFileFromHDFS(Paths
                .get(DedupLocalTest.class.getResource("/eu/dnetlib/pace/config/pub.new.tree.conf.json").toURI())
                .toFile()
                .getAbsolutePath()));

        MapDocument a = MapDocumentUtil.asMapDocumentWithJPath(config, json1);
        MapDocument b = MapDocumentUtil.asMapDocumentWithJPath(config, json2);

        boolean result = new TreeProcessor(config).compare(a,b);

        System.out.println("Tree Processor Result = " + result);

    }

    @Test //test the dedup of a group of JSON
    @Ignore
    public void dedupTest() throws Exception {
        final String entitiesPath = Paths
                .get(DedupLocalTest.class.getResource("/eu/dnetlib/pace/examples/publications.to.fix.json").toURI())
                .toFile()
                .getAbsolutePath();

        DedupConfig dedupConf = DedupConfig.load(readFileFromHDFS(Paths
                .get(DedupLocalTest.class.getResource("/eu/dnetlib/pace/config/pub.prod.tree.conf.json").toURI())
                .toFile()
                .getAbsolutePath()));

        JavaPairRDD<String, MapDocument> mapDocuments = context
                .textFile(entitiesPath)
                .mapToPair(
                        (PairFunction<String, String, MapDocument>) s -> {
                            MapDocument d = MapDocumentUtil.asMapDocumentWithJPath(dedupConf, s);
                            return new Tuple2<>(d.getIdentifier(), d);
                        })
                .reduceByKey((a,b) -> a);

        // create blocks for deduplication
        JavaPairRDD<String, Block> blocks = Deduper.createSortedBlocks(mapDocuments, dedupConf);
        for (Tuple2<String, Block> b : blocks.collect()) {
            System.out.println("*******GROUPS********");
            System.out.println("key = " + b._1());
            System.out.println("elements = " + b._2().elements());
            System.out.println("items = " + b._2().getDocuments().stream().map(d -> d.getIdentifier()).collect(Collectors.joining(",")));
            System.out.println("*********************");
        }

        // create relations by comparing only elements in the same group
        JavaRDD<Relation> relations = Deduper.computeRelations(context, blocks, dedupConf, true, false);
        for (Relation r: relations.collect()) {
            System.out.println("*******RELATIONS*******");
            System.out.println("source = " + r.getSource());
            System.out.println("target = " + r.getTarget());
            System.out.println("***********************");
        }

        //vertexes
        List<String> vertexes = mapDocuments.map(doc -> doc._1()).collect();

        //edges
        List<Tuple2<String, String>> edges = new ArrayList<>();
        relations.collect().stream().forEach(r -> edges.add(new Tuple2(r.getSource(), r.getTarget())));

        showGraph(vertexes, edges, mapDocuments);

        cleanup();
    }

    public void showGraph(List<String> vertexes, List<Tuple2<String, String>> edges, JavaPairRDD<String, MapDocument> mapDocuments)  {

        try {
            prepareGraphParams(
                    vertexes,
                    edges,
                    "/tmp/graph.html", Paths.get(DedupLocalTest.class.getResource("/graph_visualization_tool/graph_template.html").toURI()).toFile().getAbsolutePath(),
                    mapDocuments.collectAsMap());
            Desktop.getDesktop().browse(new File("/tmp/graph.html").toURI());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int nodeDegree(String id, List<Tuple2<String, String>> edges) {
        return (int) edges.stream().map(e -> e._1()).filter(s -> s.equalsIgnoreCase(id)).count();
    }

    public int minDegree(List<String> vertexes, List<Tuple2<String, String>> edges) {

        int minDegree = 100;
        for (String vertex: vertexes) {
            int deg = nodeDegree(vertex, edges);
            if (deg < minDegree)
                minDegree = deg;
        }
        return minDegree;
    }

    @Test
    @Ignore
    public void asMapDocument() throws Exception {

        final String json = "{\"context\": [], \"dataInfo\": {\"invisible\": false, \"trust\": \"0.9\", \"provenanceaction\": {\"classid\": \"sysimport:actionset\", \"classname\": \"sysimport:actionset\", \"schemeid\": \"dnet:provenanceActions\", \"schemename\": \"dnet:provenanceActions\"}, \"inferred\": false, \"deletedbyinference\": false}, \"resourcetype\": {\"classid\": \"0013\", \"classname\": \"0013\", \"schemeid\": \"dnet:dataCite_resource\", \"schemename\": \"dnet:dataCite_resource\"}, \"pid\": [{\"qualifier\": {\"classid\": \"doi\", \"classname\": \"doi\", \"schemeid\": \"dnet:pid_types\", \"schemename\": \"dnet:pid_types\"}, \"value\": \"10.1016/b978-0-323-54696-6.00057-4\"}], \"contributor\": [], \"bestaccessright\": {\"classid\": \"RESTRICTED\", \"classname\": \"Restricted\", \"schemeid\": \"dnet:access_modes\", \"schemename\": \"dnet:access_modes\"}, \"relevantdate\": [{\"qualifier\": {\"classid\": \"created\", \"classname\": \"created\", \"schemeid\": \"dnet:dataCite_date\", \"schemename\": \"dnet:dataCite_date\"}, \"value\": \"2018-11-30T10:52:46Z\"}], \"collectedfrom\": [{\"key\": \"10|openaire____::081b82f96300b6a6e3d282bad31cb6e2\", \"value\": \"Crossref\"}], \"id\": \"50|doiboost____::0a5280c186efacdc7c8ce845cec3fceb\", \"subject\": [], \"lastupdatetimestamp\": 1585062372132, \"author\": [{\"fullname\": \"Eric Caumes\", \"surname\": \"Caumes\", \"name\": \"Eric\", \"rank\": 1}], \"instance\": [{\"hostedby\": {\"key\": \"10|openaire____::55045bd2a65019fd8e6741a755395c8c\", \"value\": \"Unknown Repository\"}, \"license\": {\"value\": \"https://www.elsevier.com/tdm/userlicense/1.0/\"}, \"url\": [\"https://api.elsevier.com/content/article/PII:B9780323546966000574?httpAccept=text/xml\", \"https://api.elsevier.com/content/article/PII:B9780323546966000574?httpAccept=text/plain\", \"http://dx.doi.org/10.1016/b978-0-323-54696-6.00057-4\"], \"dateofacceptance\": {\"value\": \"2018-11-30T10:52:46Z\"}, \"collectedfrom\": {\"key\": \"10|openaire____::081b82f96300b6a6e3d282bad31cb6e2\", \"value\": \"Crossref\"}, \"accessright\": {\"classid\": \"RESTRICTED\", \"classname\": \"Restricted\", \"schemeid\": \"dnet:access_modes\", \"schemename\": \"dnet:access_modes\"}, \"instancetype\": {\"classid\": \"0013\", \"classname\": \"Part of book or chapter of book\", \"schemeid\": \"dnet:publication_resource\", \"schemename\": \"dnet:publication_resource\"}}], \"dateofcollection\": \"2020-03-24T15:06:12Z\", \"fulltext\": [], \"description\": [], \"format\": [], \"measures\": [], \"coverage\": [], \"externalReference\": [], \"publisher\": {\"value\": \"Elsevier\"}, \"resulttype\": {\"classid\": \"publication\", \"classname\": \"publication\", \"schemeid\": \"dnet:result_typologies\", \"schemename\": \"dnet:result_typologies\"}, \"country\": [], \"extraInfo\": [], \"originalId\": [\"10.1016/b978-0-323-54696-6.00057-4\"], \"source\": [{\"value\": \"Crossref\"}, {\"value\": \"Travel Medicine ISBN: 9780323546966\"}], \"dateofacceptance\": {\"value\": \"2018-11-30T10:52:46Z\"}, \"title\": [{\"qualifier\": {\"classid\": \"main title\", \"classname\": \"main title\", \"schemeid\": \"dnet:dataCite_title\", \"schemename\": \"dnet:dataCite_title\"}, \"value\": \"Skin Diseases\"}]}\n";

        DedupConfig dedupConfig = DedupConfig.load(readFileFromHDFS(
                Paths.get(DedupLocalTest.class.getResource("/eu/dnetlib/pace/config/pub.instancetype.tree.conf.json").toURI()).toFile().getAbsolutePath()
        ));

        final MapDocument mapDocument = MapDocumentUtil.asMapDocumentWithJPath(dedupConfig, json);

        for(String field: mapDocument.getFieldMap().keySet()) {
            System.out.println(field + ": " + mapDocument.getFieldMap().get(field).stringValue());
        }
    }

    @Test
    @Ignore
    public void noMatchTest() throws Exception {

        //custom parameters for this test
        DedupConfig dedupConfig = DedupConfig.load(readFileFromHDFS(
                Paths.get(DedupLocalTest.class.getResource("/eu/dnetlib/pace/config/pub.new.tree.conf.json").toURI()).toFile().getAbsolutePath()
        ));
//        String inputPath = Paths.get(DedupLocalTest.class.getResource("/eu/dnetlib/pace/examples/publications.dump.1000.json").toURI()).toFile().getAbsolutePath();
        String inputPath = "/Users/miconis/IdeaProjects/DnetHadoop/dnet-hadoop/dhp-workflows/dhp-dedup-openaire/src/test/resources/eu/dnetlib/dhp/dedup/entities/publication/publication.gz";
        String simRelsPath = workingPath + "/simrels";

        Deduper.createSimRels(
                dedupConfig,
                spark,
                inputPath,
                simRelsPath,
                true,
                true
        );
        Dataset<Relation> noMatches = spark.read().load(simRelsPath).as(Encoders.bean(Relation.class));

        System.out.println("noMatches = " + noMatches.count());

        noMatches.foreach((ForeachFunction<Relation>) r -> System.out.println(r.getSource() + " " + r.getTarget()));

        FileUtils.deleteDirectory(new File(workingPath));
    }
}