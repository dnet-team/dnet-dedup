package eu.dnetlib.pace;

import eu.dnetlib.Deduper;
import eu.dnetlib.jobs.SparkCreateDedupEntity;
import eu.dnetlib.jobs.SparkCreateMergeRels;
import eu.dnetlib.jobs.SparkCreateSimRels;
import eu.dnetlib.pace.config.DedupConfig;
import eu.dnetlib.pace.model.MapDocument;
import eu.dnetlib.pace.tree.JsonListMatch;
import eu.dnetlib.pace.tree.LevensteinTitle;
import eu.dnetlib.pace.tree.SizeMatch;
import eu.dnetlib.pace.tree.TitleVersionMatch;
import eu.dnetlib.pace.tree.support.TreeProcessor;
import eu.dnetlib.pace.util.BlockProcessorForTesting;
import eu.dnetlib.pace.util.MapDocumentUtil;
import eu.dnetlib.pace.utils.Utility;
import eu.dnetlib.support.ArgumentApplicationParser;
import eu.dnetlib.support.Block;
import eu.dnetlib.support.Relation;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.apache.commons.crypto.utils.IoUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import scala.Tuple2;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DedupLocalTest extends DedupTestUtils {

    static SparkSession spark;
    static DedupConfig config;
    static JavaSparkContext context;

    private static Object lock = new Object();
    GraphDraw frame = new GraphDraw("Test Window");


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
    public void deduplicationTest() throws IOException {

        long before_simrels = System.currentTimeMillis();
        Deduper.createSimRels(
                config,
                spark,
                entitiesPath,
                simRelsPath,
                true
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

    @Test //test the match between two JSON
    @Ignore
    public void matchTest() throws Exception {
        String json1 = "{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"sysimport:crosswalk:repository\", \"classname\": \"sysimport:crosswalk:repository\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"pid\": [], \"oaiprovenance\": {\"originDescription\": {\"metadataNamespace\": \"\", \"harvestDate\": \"2021-06-10T10:03:36.091Z\", \"baseURL\": \"file%3A%2F%2F%2Fvar%2Flib%2Fdnet%2Fdata%2Fsygma%2Fnew_ingestion%2Fcrossref\", \"datestamp\": \"\", \"altered\": true, \"identifier\": \"\"}}, \"relevantdate\": [], \"contributor\": [], \"id\": \"50|sygma_______::3bbb03e6ec8df0d219b2d2165ea1d446\", \"subject\": [], \"lastupdatetimestamp\": 1628684944004, \"author\": [{\"surname\": \"Pan\", \"fullname\": \"Pan, Mengwu\", \"pid\": [], \"name\": \"Mengwu\", \"rank\": 1}, {\"surname\": \"Blattner\", \"fullname\": \"Blattner, Christine\", \"pid\": [{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"sysimport:crosswalk:repository\", \"classname\": \"sysimport:crosswalk:repository\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"qualifier\": {\"classid\": \"orcid_pending\", \"classname\": \"Open Researcher and Contributor ID\", \"schemename\": \"dnet:pid_types\", \"schemeid\": \"dnet:pid_types\"}, \"value\": \"0000-0002-7250-5273\"}], \"name\": \"Christine\", \"rank\": 2}], \"collectedfrom\": [{\"value\": \"Sygma\", \"key\": \"10|openaire____::a8db6f6b2ce4fe72e8b2314a9a93e7d9\"}], \"instance\": [{\"refereed\": {\"classid\": \"UNKNOWN\", \"classname\": \"Unknown\", \"schemename\": \"dnet:review_levels\", \"schemeid\": \"dnet:review_levels\"}, \"hostedby\": {\"value\": \"Cancers\", \"key\": \"10|issn__online::69ba871b903253074dcf4054e619afff\"}, \"license\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"sysimport:crosswalk:repository\", \"classname\": \"sysimport:crosswalk:repository\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"value\": \"https://creativecommons.org/licenses/by/4.0/\"}, \"url\": [\"http://dx.doi.org/10.3390/cancers13040745\"], \"pid\": [], \"distributionlocation\": \"\", \"alternateIdentifier\": [{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"sysimport:crosswalk:repository\", \"classname\": \"sysimport:crosswalk:repository\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"qualifier\": {\"classid\": \"doi\", \"classname\": \"Digital Object Identifier\", \"schemename\": \"dnet:pid_types\", \"schemeid\": \"dnet:pid_types\"}, \"value\": \"10.3390/cancers13040745\"}], \"collectedfrom\": {\"value\": \"Sygma\", \"key\": \"10|openaire____::a8db6f6b2ce4fe72e8b2314a9a93e7d9\"}, \"accessright\": {\"classid\": \"OPEN\", \"classname\": \"Open Access\", \"schemename\": \"dnet:access_modes\", \"schemeid\": \"dnet:access_modes\"}, \"instancetype\": {\"classid\": \"0001\", \"classname\": \"Article\", \"schemename\": \"dnet:publication_resource\", \"schemeid\": \"dnet:publication_resource\"}}], \"resulttype\": {\"classid\": \"publication\", \"classname\": \"publication\", \"schemename\": \"dnet:result_typologies\", \"schemeid\": \"dnet:result_typologies\"}, \"dateofcollection\": \"2021-06-10T10:03:36.091Z\", \"fulltext\": [], \"dateoftransformation\": \"2021-07-20T16:59:21.682Z\", \"description\": [], \"format\": [], \"journal\": {\"issnPrinted\": \"\", \"vol\": \"13\", \"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"sysimport:crosswalk:repository\", \"classname\": \"sysimport:crosswalk:repository\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"name\": \"Cancers\", \"iss\": \"4\", \"sp\": \"745\", \"edition\": \"\", \"issnOnline\": \"2072-6694\", \"ep\": \"\", \"issnLinking\": \"\"}, \"coverage\": [], \"externalReference\": [], \"language\": {\"classid\": \"eng\", \"classname\": \"English\", \"schemename\": \"dnet:languages\", \"schemeid\": \"dnet:languages\"}, \"bestaccessright\": {\"classid\": \"OPEN\", \"classname\": \"Open Access\", \"schemename\": \"dnet:access_modes\", \"schemeid\": \"dnet:access_modes\"}, \"country\": [], \"extraInfo\": [], \"originalId\": [\"10.3390/cancers13040745\", \"50|sygma_______::3bbb03e6ec8df0d219b2d2165ea1d446\"], \"source\": [], \"context\": [], \"title\": [{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"sysimport:crosswalk:repository\", \"classname\": \"sysimport:crosswalk:repository\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"qualifier\": {\"classid\": \"main title\", \"classname\": \"main title\", \"schemename\": \"dnet:dataCite_title\", \"schemeid\": \"dnet:dataCite_title\"}, \"value\": \"Regulation of p53 by E3s\"}]}";
        String json2 = "{\"dataInfo\": {\"invisible\": false, \"trust\": \"0.9\", \"deletedbyinference\": false, \"inferred\": false, \"provenanceaction\": {\"classid\": \"sysimport:actionset\", \"classname\": \"sysimport:actionset\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}}, \"resourcetype\": {\"classid\": \"0001\", \"classname\": \"0001\", \"schemename\": \"dnet:dataCite_resource\", \"schemeid\": \"dnet:dataCite_resource\"}, \"pid\": [{\"qualifier\": {\"classid\": \"doi\", \"classname\": \"doi\", \"schemename\": \"dnet:pid_types\", \"schemeid\": \"dnet:pid_types\"}, \"value\": \"10.3390/cancers13040745\"}], \"bestaccessright\": {\"classid\": \"OPEN\", \"classname\": \"Open Access\", \"schemename\": \"dnet:access_modes\", \"schemeid\": \"dnet:access_modes\"}, \"relevantdate\": [{\"qualifier\": {\"classid\": \"created\", \"classname\": \"created\", \"schemename\": \"dnet:dataCite_date\", \"schemeid\": \"dnet:dataCite_date\"}, \"value\": \"2021-02-12T21:12:10Z\"}, {\"qualifier\": {\"classid\": \"published-online\", \"classname\": \"published-online\", \"schemename\": \"dnet:dataCite_date\", \"schemeid\": \"dnet:dataCite_date\"}, \"value\": \"2021-02-11\"}], \"contributor\": [], \"id\": \"50|doi_________::3bbb03e6ec8df0d219b2d2165ea1d446\", \"description\": [{\"value\": \"<jats:p>More than 40 years of research on p53 have given us tremendous knowledge about this protein. Today we know that p53 plays a role in different biological processes such as proliferation, invasion, pluripotency, metabolism, cell cycle control, ROS (reactive oxygen species) production, apoptosis, inflammation and autophagy. In the nucleus, p53 functions as a bona-fide transcription factor which activates and represses transcription of a number of target genes. In the cytoplasm, p53 can interact with proteins of the apoptotic machinery and by this also induces cell death. Despite being so important for the fate of the cell, expression levels of p53 are kept low in unstressed cells and the protein is largely inactive. The reason for the low expression level is that p53 is efficiently degraded by the ubiquitin-proteasome system and the vast inactivity of the tumor suppressor protein under normal growth conditions is due to the absence of activating and the presence of inactivating posttranslational modifications. E3s are important enzymes for these processes as they decorate p53 with ubiquitin and small ubiquitin-like proteins and by this control p53 degradation, stability and its subcellular localization. In this review, we provide an overview about E3s that target p53 and discuss the connection between p53, E3s and tumorigenesis.</jats:p>\"}], \"lastupdatetimestamp\": 1613647061057, \"author\": [{\"fullname\": \"Mengwu Pan\", \"pid\": [{\"qualifier\": {\"classid\": \"URL\", \"classname\": \"URL\", \"schemename\": \"dnet:pid_types\", \"schemeid\": \"dnet:pid_types\"}, \"value\": \"https://academic.microsoft.com/#/detail/3128025883\"}], \"rank\": 1}, {\"fullname\": \"Christine Blattner\", \"pid\": [{\"qualifier\": {\"classid\": \"URL\", \"classname\": \"URL\", \"schemename\": \"dnet:pid_types\", \"schemeid\": \"dnet:pid_types\"}, \"value\": \"https://academic.microsoft.com/#/detail/3126711219\"}, {\"dataInfo\": {\"invisible\": false, \"trust\": \"0.9\", \"deletedbyinference\": false, \"inferred\": false, \"provenanceaction\": {\"classid\": \"sysimport:actionset\", \"classname\": \"sysimport:actionset\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}}, \"qualifier\": {\"classid\": \"orcid_pending\", \"classname\": \"orcid_pending\", \"schemename\": \"dnet:pid_types\", \"schemeid\": \"dnet:pid_types\"}, \"value\": \"http://orcid.org/0000-0002-7250-5273\"}, {\"dataInfo\": {\"invisible\": false, \"trust\": \"0.91\", \"deletedbyinference\": false, \"inferred\": false, \"provenanceaction\": {\"classid\": \"sysimport:crosswalk:entityregistry\", \"classname\": \"Harvested\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}}, \"qualifier\": {\"classid\": \"orcid\", \"classname\": \"orcid\", \"schemename\": \"dnet:pid_types\", \"schemeid\": \"dnet:pid_types\"}, \"value\": \"0000-0002-7250-5273\"}], \"rank\": 2}], \"collectedfrom\": [{\"value\": \"Crossref\", \"key\": \"10|openaire____::081b82f96300b6a6e3d282bad31cb6e2\"}, {\"value\": \"UnpayWall\", \"key\": \"10|openaire____::8ac8380272269217cb09a928c8caa993\"}, {\"value\": \"ORCID\", \"key\": \"10|openaire____::806360c771262b4d6770e7cdf04b5c5a\"}, {\"value\": \"Microsoft Academic Graph\", \"key\": \"10|openaire____::5f532a3fc4f1ea403f37070f59a7a53a\"}], \"instance\": [{\"hostedby\": {\"value\": \"Cancers\", \"key\": \"10|doajarticles::69ba871b903253074dcf4054e619afff\"}, \"license\": {\"value\": \"https://creativecommons.org/licenses/by/4.0/\"}, \"url\": [\"https://www.mdpi.com/2072-6694/13/4/745/pdf\", \"http://dx.doi.org/10.3390/cancers13040745\"], \"pid\": [{\"qualifier\": {\"classid\": \"doi\", \"classname\": \"doi\", \"schemename\": \"dnet:pid_types\", \"schemeid\": \"dnet:pid_types\"}, \"value\": \"10.3390/cancers13040745\"}], \"dateofacceptance\": {\"value\": \"2021-02-11\"}, \"collectedfrom\": {\"value\": \"Crossref\", \"key\": \"10|openaire____::081b82f96300b6a6e3d282bad31cb6e2\"}, \"accessright\": {\"classid\": \"OPEN\", \"classname\": \"Open Access\", \"schemename\": \"dnet:access_modes\", \"schemeid\": \"dnet:access_modes\"}, \"instancetype\": {\"classid\": \"0001\", \"classname\": \"Article\", \"schemename\": \"dnet:publication_resource\", \"schemeid\": \"dnet:publication_resource\"}}, {\"hostedby\": {\"value\": \"Cancers\", \"key\": \"10|doajarticles::69ba871b903253074dcf4054e619afff\"}, \"license\": {\"value\": \"cc-by\"}, \"url\": [\"https://res.mdpi.com/d_attachment/cancers/cancers-13-00745/article_deploy/cancers-13-00745.pdf\"], \"pid\": [{\"qualifier\": {\"classid\": \"doi\", \"classname\": \"doi\", \"schemename\": \"dnet:pid_types\", \"schemeid\": \"dnet:pid_types\"}, \"value\": \"10.3390/cancers13040745\"}], \"collectedfrom\": {\"value\": \"UnpayWall\", \"key\": \"10|openaire____::8ac8380272269217cb09a928c8caa993\"}, \"accessright\": {\"classid\": \"OPEN\", \"classname\": \"Open Access\", \"schemename\": \"dnet:access_modes\", \"schemeid\": \"dnet:access_modes\"}, \"instancetype\": {\"classid\": \"0001\", \"classname\": \"Article\", \"schemename\": \"dnet:publication_resource\", \"schemeid\": \"dnet:publication_resource\"}}, {\"hostedby\": {\"value\": \"Cancers\", \"key\": \"10|doajarticles::69ba871b903253074dcf4054e619afff\"}, \"url\": [\"https://www.mdpi.com/2072-6694/13/4/745\", \"https://www.mdpi.com/2072-6694/13/4/745/pdf\", \"https://academic.microsoft.com/#/detail/3128658507\"], \"pid\": [{\"qualifier\": {\"classid\": \"doi\", \"classname\": \"doi\", \"schemename\": \"dnet:pid_types\", \"schemeid\": \"dnet:pid_types\"}, \"value\": \"10.3390/cancers13040745\"}], \"collectedfrom\": {\"value\": \"Microsoft Academic Graph\", \"key\": \"10|openaire____::5f532a3fc4f1ea403f37070f59a7a53a\"}, \"accessright\": {\"classid\": \"OPEN\", \"classname\": \"Open Access\", \"schemename\": \"dnet:access_modes\", \"schemeid\": \"dnet:access_modes\"}, \"instancetype\": {\"classid\": \"0001\", \"classname\": \"Article\", \"schemename\": \"dnet:publication_resource\", \"schemeid\": \"dnet:publication_resource\"}}], \"dateofcollection\": \"2021-02-18T11:17:41Z\", \"fulltext\": [], \"dateofacceptance\": {\"value\": \"2021-02-11\"}, \"format\": [], \"journal\": {\"vol\": \"13\", \"sp\": \"745\", \"issnOnline\": \"2072-6694\", \"name\": \"Cancers\"}, \"measures\": [], \"subject\": [{\"qualifier\": {\"classid\": \"keywords\", \"classname\": \"keywords\", \"schemename\": \"dnet:subject_classification_typologies\", \"schemeid\": \"dnet:subject_classification_typologies\"}, \"value\": \"Cancer Research\"}, {\"qualifier\": {\"classid\": \"keywords\", \"classname\": \"keywords\", \"schemename\": \"dnet:subject_classification_typologies\", \"schemeid\": \"dnet:subject_classification_typologies\"}, \"value\": \"Oncology\"}, {\"qualifier\": {\"classid\": \"MAG\", \"classname\": \"Microsoft Academic Graph classification\", \"schemename\": \"dnet:subject_classification_typologies\", \"schemeid\": \"dnet:subject_classification_typologies\"}, \"value\": \"Carcinogenesis\"}, {\"dataInfo\": {\"invisible\": false, \"trust\": \"0.51921105\", \"deletedbyinference\": false, \"inferred\": false, \"provenanceaction\": {\"classid\": \"sysimport:actionset\", \"classname\": \"sysimport:actionset\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}}, \"qualifier\": {\"classid\": \"MAG\", \"classname\": \"Microsoft Academic Graph classification\", \"schemename\": \"dnet:subject_classification_typologies\", \"schemeid\": \"dnet:subject_classification_typologies\"}, \"value\": \"medicine.disease_cause\"}, {\"dataInfo\": {\"invisible\": false, \"trust\": \"0.51921105\", \"deletedbyinference\": false, \"inferred\": false, \"provenanceaction\": {\"classid\": \"sysimport:actionset\", \"classname\": \"sysimport:actionset\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}}, \"qualifier\": {\"classid\": \"MAG\", \"classname\": \"Microsoft Academic Graph classification\", \"schemename\": \"dnet:subject_classification_typologies\", \"schemeid\": \"dnet:subject_classification_typologies\"}, \"value\": \"medicine\"}, {\"qualifier\": {\"classid\": \"MAG\", \"classname\": \"Microsoft Academic Graph classification\", \"schemename\": \"dnet:subject_classification_typologies\", \"schemeid\": \"dnet:subject_classification_typologies\"}, \"value\": \"Cytoplasm\"}, {\"qualifier\": {\"classid\": \"MAG\", \"classname\": \"Microsoft Academic Graph classification\", \"schemename\": \"dnet:subject_classification_typologies\", \"schemeid\": \"dnet:subject_classification_typologies\"}, \"value\": \"Transcription factor\"}, {\"qualifier\": {\"classid\": \"MAG\", \"classname\": \"Microsoft Academic Graph classification\", \"schemename\": \"dnet:subject_classification_typologies\", \"schemeid\": \"dnet:subject_classification_typologies\"}, \"value\": \"Cell biology\"}, {\"qualifier\": {\"classid\": \"MAG\", \"classname\": \"Microsoft Academic Graph classification\", \"schemename\": \"dnet:subject_classification_typologies\", \"schemeid\": \"dnet:subject_classification_typologies\"}, \"value\": \"Ubiquitin\"}, {\"dataInfo\": {\"invisible\": false, \"trust\": \"0.5209853\", \"deletedbyinference\": false, \"inferred\": false, \"provenanceaction\": {\"classid\": \"sysimport:actionset\", \"classname\": \"sysimport:actionset\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}}, \"qualifier\": {\"classid\": \"MAG\", \"classname\": \"Microsoft Academic Graph classification\", \"schemename\": \"dnet:subject_classification_typologies\", \"schemeid\": \"dnet:subject_classification_typologies\"}, \"value\": \"biology.protein\"}, {\"dataInfo\": {\"invisible\": false, \"trust\": \"0.5209853\", \"deletedbyinference\": false, \"inferred\": false, \"provenanceaction\": {\"classid\": \"sysimport:actionset\", \"classname\": \"sysimport:actionset\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}}, \"qualifier\": {\"classid\": \"MAG\", \"classname\": \"Microsoft Academic Graph classification\", \"schemename\": \"dnet:subject_classification_typologies\", \"schemeid\": \"dnet:subject_classification_typologies\"}, \"value\": \"biology\"}, {\"qualifier\": {\"classid\": \"MAG\", \"classname\": \"Microsoft Academic Graph classification\", \"schemename\": \"dnet:subject_classification_typologies\", \"schemeid\": \"dnet:subject_classification_typologies\"}, \"value\": \"Cell\"}, {\"dataInfo\": {\"invisible\": false, \"trust\": \"0.51552147\", \"deletedbyinference\": false, \"inferred\": false, \"provenanceaction\": {\"classid\": \"sysimport:actionset\", \"classname\": \"sysimport:actionset\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}}, \"qualifier\": {\"classid\": \"MAG\", \"classname\": \"Microsoft Academic Graph classification\", \"schemename\": \"dnet:subject_classification_typologies\", \"schemeid\": \"dnet:subject_classification_typologies\"}, \"value\": \"medicine.anatomical_structure\"}, {\"qualifier\": {\"classid\": \"MAG\", \"classname\": \"Microsoft Academic Graph classification\", \"schemename\": \"dnet:subject_classification_typologies\", \"schemeid\": \"dnet:subject_classification_typologies\"}, \"value\": \"Programmed cell death\"}, {\"qualifier\": {\"classid\": \"MAG\", \"classname\": \"Microsoft Academic Graph classification\", \"schemename\": \"dnet:subject_classification_typologies\", \"schemeid\": \"dnet:subject_classification_typologies\"}, \"value\": \"Autophagy\"}, {\"qualifier\": {\"classid\": \"MAG\", \"classname\": \"Microsoft Academic Graph classification\", \"schemename\": \"dnet:subject_classification_typologies\", \"schemeid\": \"dnet:subject_classification_typologies\"}, \"value\": \"Chemistry\"}, {\"qualifier\": {\"classid\": \"MAG\", \"classname\": \"Microsoft Academic Graph classification\", \"schemename\": \"dnet:subject_classification_typologies\", \"schemeid\": \"dnet:subject_classification_typologies\"}, \"value\": \"Subcellular localization\"}], \"coverage\": [], \"externalReference\": [], \"publisher\": {\"value\": \"MDPI AG\"}, \"resulttype\": {\"classid\": \"publication\", \"classname\": \"publication\", \"schemename\": \"dnet:result_typologies\", \"schemeid\": \"dnet:result_typologies\"}, \"country\": [], \"extraInfo\": [], \"originalId\": [\"cancers13040745\", \"10.3390/cancers13040745\", \"50|doiboost____::3bbb03e6ec8df0d219b2d2165ea1d446\", \"3128658507\"], \"source\": [{\"value\": \"Crossref\"}, {}], \"context\": [], \"title\": [{\"qualifier\": {\"classid\": \"alternative title\", \"classname\": \"alternative title\", \"schemename\": \"dnet:dataCite_title\", \"schemeid\": \"dnet:dataCite_title\"}, \"value\": \"Regulation of p53 by E3s\"}, {\"qualifier\": {\"classid\": \"main title\", \"classname\": \"main title\", \"schemename\": \"dnet:dataCite_title\", \"schemeid\": \"dnet:dataCite_title\"}, \"value\": \"Regulation of p53 by E3s\"}]}";

        MapDocument a = MapDocumentUtil.asMapDocumentWithJPath(config, json1);
        MapDocument b = MapDocumentUtil.asMapDocumentWithJPath(config, json2);

        boolean result = new TreeProcessor(config).compare(a,b);

        System.out.println("Tree Processor Result = " + result);

    }

    @Test //test the dedup of a group of JSON
    @Ignore
    public void dedupTest() throws Exception {
        final String entitiesPath = Paths
                .get(DedupLocalTest.class.getResource("/eu/dnetlib/pace/examples/openorgs.to.fix.json").toURI())
                .toFile()
                .getAbsolutePath();

        DedupConfig dedupConf = DedupConfig.load(readFileFromHDFS(Paths
                .get(DedupLocalTest.class.getResource("/eu/dnetlib/pace/config/orgs.tree.conf.json").toURI())
                .toFile()
                .getAbsolutePath()));

        JavaPairRDD<String, MapDocument> mapDocuments = context
                .textFile(entitiesPath)
                .mapToPair(
                        (PairFunction<String, String, MapDocument>) s -> {
                            MapDocument d = MapDocumentUtil.asMapDocumentWithJPath(dedupConf, s);
                            return new Tuple2<>(d.getIdentifier(), d);
                        });

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
        JavaRDD<Relation> relations = Deduper.computeRelations(context, blocks, dedupConf, true);
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

        drawGraph(vertexes, edges);

        cleanup();

        synchronized(lock) {
            while (frame.isVisible())
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
    }

    public void drawGraph(List<String> vertexes, List<Tuple2<String, String>> edges) {

        frame.setSize(2000,2000);

        frame.setVisible(true);

        int nVertexes = vertexes.size();
        int nRow = (int) Math.round(Math.floor(Math.sqrt(nVertexes)));
        int nCol = (int) Math.round(Math.ceil(Math.sqrt(nVertexes)));
        int cStepSize = 500;
        int rStepSize = 200;
        for(int i = 0; i < nRow; i++){
            for(int j = 0; j < nCol; j++){
                int index = nCol*i + j;
                frame.addNode(vertexes.get(index), 200 + j*cStepSize, 50 + i*rStepSize);
                if (index == nVertexes-1)
                    continue;
            }
        }

        for (Tuple2<String, String> e: edges) {
            frame.addEdge(vertexes.indexOf(e._1()), vertexes.indexOf(e._2()));
        }

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                synchronized (lock) {
                    frame.setVisible(false);
                    lock.notify();
                }
            }
        });
    }

}

// function mocking the tree processor by considering every comparison instead of using early exits
//    private boolean publicationCompare(MapDocument a, MapDocument b, DedupConfig config) {
//
//        double score = 0.0;
//        //LAYER 1 - comparison of the PIDs json lists
//        Map<String, String> params = new HashMap<>();
//        params.put("jpath_value", "$.value");
//        params.put("jpath_classid", "$.qualifier.classid");
//        JsonListMatch jsonListMatch = new JsonListMatch(params);
//        double result = jsonListMatch.compare(a.getFieldMap().get("pid"), b.getFieldMap().get("pid"), config);
//        if (result >= 0.5) //if the result of the comparison is greater than the threshold
//            score += 10.0;  //high score because it should match when the first condition is satisfied
//        else
//            score += 0.0;
//
//        //LAYER 2 - comparison of the title version and the size of the authors lists
//        TitleVersionMatch titleVersionMatch = new TitleVersionMatch(params);
//        double result1 = titleVersionMatch.compare(a.getFieldMap().get("title"), b.getFieldMap().get("title"), config);
//        SizeMatch sizeMatch = new SizeMatch(params);
//        double result2 = sizeMatch.compare(a.getFieldMap().get("authors"), b.getFieldMap().get("authors"), config);
//        if (Math.min(result1, result2) != 0)
//            score+=0;
//        else
//            score-=2;
//
//        //LAYER 3 - computation of levenshtein on titles
//        LevensteinTitle levensteinTitle = new LevensteinTitle(params);
//        double result3 = levensteinTitle.compare(a.getFieldMap().get("title"), b.getFieldMap().get("title"), config);
//        score += Double.isNaN(result3)?0.0:result3;;
//
//        return score >= 0.99;
//    }