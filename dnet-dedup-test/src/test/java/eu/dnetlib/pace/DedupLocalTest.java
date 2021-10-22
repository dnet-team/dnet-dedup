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
import org.apache.spark.api.java.function.PairFunction;
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
    public void deduplicationTest() throws IOException {

        //custom parameters for this test
        DedupConfig dedupConfig = DedupConfig.load(readFileFromHDFS("/Users/miconis/IdeaProjects/DnetDedup/dnet-dedup/dnet-dedup-test/src/test/resources/eu/dnetlib/pace/config/ds.tree.conf.json"));
        String inputPath = "/Users/miconis/Desktop/Fairsharing dedup/datasources";
        String workingPath = "/tmp/fairsharing_working_dir";
        String simRelsPath = workingPath + "/simrels";
        String mergeRelsPath = workingPath + "/mergerels";
        String outputPath = workingPath + "/dedup";

        long before_simrels = System.currentTimeMillis();
        Deduper.createSimRels(
                dedupConfig,
                spark,
                inputPath,
                simRelsPath,
                true
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

        cleanup();
    }

    @Test //test the match between two JSON
    @Ignore
    public void matchTest() throws Exception {
        String json1 = "{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"sysimport:crosswalk:repository\", \"classname\": \"sysimport:crosswalk:repository\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"pid\": [], \"oaiprovenance\": {\"originDescription\": {\"metadataNamespace\": \"http://www.openarchives.org/OAI/2.0/oai_dc/\", \"harvestDate\": \"2020-05-16T09:19:18.795Z\", \"baseURL\": \"https%3A%2F%2Flekythos.library.ucy.ac.cy%2Foai%2Frequest\", \"datestamp\": \"2020-05-14T07:35:25Z\", \"altered\": true, \"identifier\": \"oai:lekythos.library.ucy.ac.cy:10797/13865\"}}, \"relevantdate\": [], \"contributor\": [{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"sysimport:crosswalk:repository\", \"classname\": \"sysimport:crosswalk:repository\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"value\": \"Agosti, Maristella\"}, {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"sysimport:crosswalk:repository\", \"classname\": \"sysimport:crosswalk:repository\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"value\": \"Borbinha, Jos\\u00e9\"}, {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"sysimport:crosswalk:repository\", \"classname\": \"sysimport:crosswalk:repository\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"value\": \"\\u039a\\u03b1\\u03c0\\u03b9\\u03b4\\u03ac\\u03ba\\u03b7\\u03c2, \\u03a3\\u03b1\\u03c1\\u03ac\\u03bd\\u03c4\\u03bf\\u03c2\"}, {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"sysimport:crosswalk:repository\", \"classname\": \"sysimport:crosswalk:repository\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"value\": \"Kapidakis, Sarantos\"}, {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"sysimport:crosswalk:repository\", \"classname\": \"sysimport:crosswalk:repository\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"value\": \"\\u03a4\\u03bc\\u03ae\\u03bc\\u03b1 \\u0391\\u03c1\\u03c7\\u03b5\\u03b9\\u03bf\\u03bd\\u03bf\\u03bc\\u03af\\u03b1\\u03c2 \\u03ba\\u03b1\\u03b9 \\u0392\\u03b9\\u03b2\\u03bb\\u03b9\\u03bf\\u03b8\\u03b7\\u03ba\\u03bf\\u03bd\\u03bf\\u03bc\\u03af\\u03b1\\u03c2, \\u0395\\u03c1\\u03b3\\u03b1\\u03c3\\u03c4\\u03ae\\u03c1\\u03b9\\u03bf \\u03a8\\u03b7\\u03c6\\u03b9\\u03b1\\u03ba\\u03ce\\u03bd \\u0392\\u03b9\\u03b2\\u03bb\\u03b9\\u03bf\\u03b8\\u03b7\\u03ba\\u03ce\\u03bd \\u03ba\\u03b1\\u03b9 \\u0397\\u03bb\\u03b5\\u03ba\\u03c4\\u03c1\\u03bf\\u03bd\\u03b9\\u03ba\\u03ae\\u03c2 \\u0394\\u03b7\\u03bc\\u03bf\\u03c3\\u03af\\u03b5\\u03c5\\u03c3\\u03b7\\u03c2, \\u0399\\u03cc\\u03bd\\u03b9\\u03bf \\u03a0\\u03b1\\u03bd\\u03b5\\u03c0\\u03b9\\u03c3\\u03c4\\u03ae\\u03bc\\u03b9\\u03bf\"}], \"id\": \"50|od______2389::3e967b6804e6dfafb1d47c81ad5011f3\", \"description\": [], \"lastupdatetimestamp\": 1628685153367, \"author\": [], \"collectedfrom\": [{\"value\": \"LEKYTHOS\", \"key\": \"10|opendoar____::063e26c670d07bb7c4d30e6fc69fe056\"}], \"instance\": [{\"refereed\": {\"classid\": \"UNKNOWN\", \"classname\": \"Unknown\", \"schemename\": \"dnet:review_levels\", \"schemeid\": \"dnet:review_levels\"}, \"hostedby\": {\"value\": \"LEKYTHOS\", \"key\": \"10|opendoar____::063e26c670d07bb7c4d30e6fc69fe056\"}, \"url\": [\"http://hdl.handle.net/10797/13865\"], \"pid\": [], \"distributionlocation\": \"\", \"alternateIdentifier\": [], \"dateofacceptance\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"sysimport:crosswalk:repository\", \"classname\": \"sysimport:crosswalk:repository\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"value\": \"2009-01-01\"}, \"collectedfrom\": {\"value\": \"LEKYTHOS\", \"key\": \"10|opendoar____::063e26c670d07bb7c4d30e6fc69fe056\"}, \"accessright\": {\"classid\": \"OPEN\", \"classname\": \"Open Access\", \"schemename\": \"dnet:access_modes\", \"schemeid\": \"dnet:access_modes\"}, \"instancetype\": {\"classid\": \"0004\", \"classname\": \"Conference object\", \"schemename\": \"dnet:publication_resource\", \"schemeid\": \"dnet:publication_resource\"}}], \"resulttype\": {\"classid\": \"publication\", \"classname\": \"publication\", \"schemename\": \"dnet:result_typologies\", \"schemeid\": \"dnet:result_typologies\"}, \"dateofcollection\": \"2020-05-16T09:19:18.795Z\", \"fulltext\": [], \"dateoftransformation\": \"2021-05-09T17:22:10.634Z\", \"dateofacceptance\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"sysimport:crosswalk:repository\", \"classname\": \"sysimport:crosswalk:repository\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"value\": \"2009-01-01\"}, \"format\": [], \"subject\": [{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"sysimport:crosswalk:repository\", \"classname\": \"sysimport:crosswalk:repository\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"qualifier\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"value\": \"\\u0392\\u03b9\\u03b2\\u03bb\\u03b9\\u03bf\\u03b8\\u03ae\\u03ba\\u03b5\\u03c2, \\u03a3\\u03c5\\u03bd\\u03ad\\u03b4\\u03c1\\u03b9\\u03b1, \\u0395\\u03bb\\u03bb\\u03ac\\u03b4\\u03b1\"}, {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"sysimport:crosswalk:repository\", \"classname\": \"sysimport:crosswalk:repository\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"qualifier\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"value\": \"Libraries, Congresses, Greece\"}, {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"sysimport:crosswalk:repository\", \"classname\": \"sysimport:crosswalk:repository\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"qualifier\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"value\": \"\\u0392\\u03b9\\u03b2\\u03bb\\u03b9\\u03bf\\u03b8\\u03ae\\u03ba\\u03b5\\u03c2 \\u03c9\\u03c2 \\u03c6\\u03c5\\u03c3\\u03b9\\u03ba\\u03ad\\u03c2 \\u03c3\\u03c5\\u03bb\\u03bb\\u03bf\\u03b3\\u03ad\\u03c2\"}, {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"sysimport:crosswalk:repository\", \"classname\": \"sysimport:crosswalk:repository\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"qualifier\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"value\": \"Libraries as physical collections\"}], \"coverage\": [], \"externalReference\": [], \"publisher\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"sysimport:crosswalk:repository\", \"classname\": \"sysimport:crosswalk:repository\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"value\": \"Springer\"}, \"language\": {\"classid\": \"eng\", \"classname\": \"English\", \"schemename\": \"dnet:languages\", \"schemeid\": \"dnet:languages\"}, \"bestaccessright\": {\"classid\": \"OPEN\", \"classname\": \"Open Access\", \"schemename\": \"dnet:access_modes\", \"schemeid\": \"dnet:access_modes\"}, \"country\": [], \"extraInfo\": [], \"originalId\": [\"oai:lekythos.library.ucy.ac.cy:10797/13865\", \"50|od______2389::3e967b6804e6dfafb1d47c81ad5011f3\"], \"source\": [{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"sysimport:crosswalk:repository\", \"classname\": \"sysimport:crosswalk:repository\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"value\": \"13th European Conference, ECDL 2009\"}, {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"sysimport:crosswalk:repository\", \"classname\": \"sysimport:crosswalk:repository\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"value\": \"https://www.springer.com/gp/book/9783642043451\"}], \"context\": [], \"title\": [{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"sysimport:crosswalk:repository\", \"classname\": \"sysimport:crosswalk:repository\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"qualifier\": {\"classid\": \"main title\", \"classname\": \"main title\", \"schemename\": \"dnet:dataCite_title\", \"schemeid\": \"dnet:dataCite_title\"}, \"value\": \"Research and Advanced Technology for Digital Libraries\"}]}";
        String json2 = "{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"sysimport:crosswalk:datasetarchive\", \"classname\": \"sysimport:crosswalk:datasetarchive\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"resourcetype\": {\"classid\": \"0002\", \"classname\": \"0002\", \"schemename\": \"dnet:dataCite_resource\", \"schemeid\": \"dnet:dataCite_resource\"}, \"pid\": [{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"sysimport:crosswalk:datasetarchive\", \"classname\": \"sysimport:crosswalk:datasetarchive\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"qualifier\": {\"classid\": \"handle\", \"classname\": \"Handle\", \"schemename\": \"dnet:pid_types\", \"schemeid\": \"dnet:pid_types\"}, \"value\": \"11245.1/b69d387a-80f6-4675-8130-2488bd93ed43\"}], \"oaiprovenance\": {\"originDescription\": {\"metadataNamespace\": \"\", \"harvestDate\": \"2021-08-04T12:49:02.536Z\", \"baseURL\": \"http%3A%2F%2Fservices.nod.dans.knaw.nl%2Foa-cerif\", \"datestamp\": \"2020-06-02T10:56:09Z\", \"altered\": true, \"identifier\": \"oai:services.nod.dans.knaw.nl:Publications/uvapub:oai:dare.uva.nl:publications/b69d387a-80f6-4675-8130-2488bd93ed43\"}}, \"relevantdate\": [{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"sysimport:crosswalk:datasetarchive\", \"classname\": \"sysimport:crosswalk:datasetarchive\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"qualifier\": {\"classid\": \"Issued\", \"classname\": \"Issued\", \"schemename\": \"dnet:dataCite_date\", \"schemeid\": \"dnet:dataCite_date\"}, \"value\": \"2017-01-01\"}], \"contributor\": [], \"id\": \"50|dris___00893::f8a8a3eb400fb07d19c5b28942f06453\", \"description\": [{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"sysimport:crosswalk:datasetarchive\", \"classname\": \"sysimport:crosswalk:datasetarchive\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"value\": \"This book constitutes the proceedings of the 21st International Conference on Theory and Practice of Digital Libraries, TPDL 2017, held in Thessaloniki, Greece, in September 2017. The 39 full papers, 11 short papers, and 10 poster papers presented in this volume were carefully reviewed and selected from 106 submissions. In addition the book contains 7 doctoral consortium papers. The contributions are organized in topical sections named: linked data; corpora; data in digital libraries; quality in digital libraries; digital humanities; entities; scholarly communication; sentiment analysis; information behavior; information retrieval.\"}], \"lastupdatetimestamp\": 1628685003505, \"author\": [{\"surname\": \"Kamps\", \"name\": \"J.\", \"pid\": [], \"rank\": 1, \"affiliation\": [], \"fullname\": \"Kamps, J.\"}, {\"surname\": \"Tsakonas\", \"name\": \"G.\", \"pid\": [], \"rank\": 2, \"affiliation\": [], \"fullname\": \"Tsakonas, G.\"}, {\"surname\": \"Manolopoulos\", \"name\": \"Y.\", \"pid\": [], \"rank\": 3, \"affiliation\": [], \"fullname\": \"Manolopoulos, Y.\"}, {\"surname\": \"Iliadis\", \"name\": \"L.\", \"pid\": [], \"rank\": 4, \"affiliation\": [], \"fullname\": \"Iliadis, L.\"}, {\"surname\": \"Karydis\", \"name\": \"I.\", \"pid\": [], \"rank\": 5, \"affiliation\": [], \"fullname\": \"Karydis, I.\"}], \"collectedfrom\": [{\"value\": \"NARCIS\", \"key\": \"10|eurocrisdris::fe4903425d9040f680d8610d9079ea14\"}], \"instance\": [{\"refereed\": {\"classid\": \"UNKNOWN\", \"classname\": \"Unknown\", \"schemename\": \"dnet:review_levels\", \"schemeid\": \"dnet:review_levels\"}, \"hostedby\": {\"value\": \"NARCIS\", \"key\": \"10|eurocrisdris::fe4903425d9040f680d8610d9079ea14\"}, \"url\": [\"\", \"http://dx.doi.org/10.1007/978-3-319-67008-9\"], \"pid\": [{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"sysimport:crosswalk:datasetarchive\", \"classname\": \"sysimport:crosswalk:datasetarchive\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"qualifier\": {\"classid\": \"handle\", \"classname\": \"Handle\", \"schemename\": \"dnet:pid_types\", \"schemeid\": \"dnet:pid_types\"}, \"value\": \"11245.1/b69d387a-80f6-4675-8130-2488bd93ed43\"}], \"alternateIdentifier\": [{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"sysimport:crosswalk:datasetarchive\", \"classname\": \"sysimport:crosswalk:datasetarchive\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"qualifier\": {\"classid\": \"doi\", \"classname\": \"Digital Object Identifier\", \"schemename\": \"dnet:pid_types\", \"schemeid\": \"dnet:pid_types\"}, \"value\": \"10.1007/978-3-319-67008-9\"}, {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"sysimport:crosswalk:datasetarchive\", \"classname\": \"sysimport:crosswalk:datasetarchive\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"qualifier\": {\"classid\": \"urn\", \"classname\": \"urn\", \"schemename\": \"dnet:pid_types\", \"schemeid\": \"dnet:pid_types\"}, \"value\": \"urn:nbn:nl:ui:29-b69d387a-80f6-4675-8130-2488bd93ed43\"}], \"dateofacceptance\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"sysimport:crosswalk:datasetarchive\", \"classname\": \"sysimport:crosswalk:datasetarchive\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"value\": \"2017-01-01\"}, \"collectedfrom\": {\"value\": \"NARCIS\", \"key\": \"10|eurocrisdris::fe4903425d9040f680d8610d9079ea14\"}, \"accessright\": {\"classid\": \"CLOSED\", \"classname\": \"Closed Access\", \"schemename\": \"dnet:access_modes\", \"schemeid\": \"dnet:access_modes\"}, \"instancetype\": {\"classid\": \"0002\", \"classname\": \"Book\", \"schemename\": \"dnet:publication_resource\", \"schemeid\": \"dnet:publication_resource\"}}], \"resulttype\": {\"classid\": \"publication\", \"classname\": \"publication\", \"schemename\": \"dnet:result_typologies\", \"schemeid\": \"dnet:result_typologies\"}, \"dateofcollection\": \"2021-08-04T12:49:02.536Z\", \"fulltext\": [], \"dateoftransformation\": \"2021-08-05T10:41:57.844Z\", \"dateofacceptance\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"sysimport:crosswalk:datasetarchive\", \"classname\": \"sysimport:crosswalk:datasetarchive\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"value\": \"2017-01-01\"}, \"format\": [], \"subject\": [], \"coverage\": [], \"externalReference\": [], \"language\": {\"classid\": \"en\", \"classname\": \"en\", \"schemename\": \"dnet:languages\", \"schemeid\": \"dnet:languages\"}, \"bestaccessright\": {\"classid\": \"CLOSED\", \"classname\": \"Closed Access\", \"schemename\": \"dnet:access_modes\", \"schemeid\": \"dnet:access_modes\"}, \"country\": [], \"extraInfo\": [], \"originalId\": [\"50|dris___00893::f8a8a3eb400fb07d19c5b28942f06453\", \"oai:services.nod.dans.knaw.nl:Publications/uvapub:oai:dare.uva.nl:publications/b69d387a-80f6-4675-8130-2488bd93ed43\"], \"source\": [], \"context\": [], \"title\": [{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"sysimport:crosswalk:datasetarchive\", \"classname\": \"sysimport:crosswalk:datasetarchive\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"qualifier\": {\"classid\": \"main title\", \"classname\": \"main title\", \"schemename\": \"dnet:dataCite_title\", \"schemeid\": \"dnet:dataCite_title\"}, \"value\": \"Research and Advanced Technology for Digital Libraries\"}, {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"sysimport:crosswalk:datasetarchive\", \"classname\": \"sysimport:crosswalk:datasetarchive\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"qualifier\": {\"classid\": \"main title\", \"classname\": \"main title\", \"schemename\": \"dnet:dataCite_title\", \"schemeid\": \"dnet:dataCite_title\"}, \"value\": \"21st International Conference on Theory and Practice of Digital Libraries, TPDL 2017, Thessaloniki, Greece, September 18-21, 2017 : proceedings\"}]}";

        DedupConfig config = DedupConfig.load(readFileFromHDFS(Paths
                .get(DedupLocalTest.class.getResource("/eu/dnetlib/pace/config/pub.prod.tree.conf.json").toURI())
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