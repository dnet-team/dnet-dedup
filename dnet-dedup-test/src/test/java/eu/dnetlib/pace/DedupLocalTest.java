package eu.dnetlib.pace;

import eu.dnetlib.Deduper;
import eu.dnetlib.graph.JavaGraphProcessor;
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
import eu.dnetlib.support.ConnectedComponent;
import eu.dnetlib.support.Relation;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.ForeachFunction;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.graphx.Edge;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import scala.Tuple2;

import java.awt.*;
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
            .get(DedupLocalTest.class.getResource("/eu/dnetlib/pace/examples/publications.dump.1000.json").toURI())
            .toFile()
            .getAbsolutePath();

    final static String workingPath    = "/tmp/working_dir";
    final static String numPartitions  = "20";

    final String dedupConfPath = Paths
            .get(DedupLocalTest.class.getResource("/eu/dnetlib/pace/config/pubs.fdup.exp.json").toURI())
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
    @Disabled
    public void deduplicationTest() throws Exception {

        //custom parameters for this test
        DedupConfig dedupConfig = DedupConfig.load(readFileFromHDFS(
                Paths.get(DedupLocalTest.class.getResource("/eu/dnetlib/pace/config/ds.tree.conf.json").toURI()).toFile().getAbsolutePath()
        ));

        String inputPath = Paths.get(DedupLocalTest.class.getResource("/eu/dnetlib/pace/examples/publications.to.fix.json").toURI()).toFile().getAbsolutePath();

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

//        FileUtils.deleteDirectory(new File(workingPath));
    }

    @Test //test the match between two JSON
    @Disabled
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
    @Disabled
    public void dedupTest() throws Exception {
        final String entitiesPath = Paths
                .get(DedupLocalTest.class.getResource("/eu/dnetlib/pace/examples/openorgs.to.fix.json").toURI())
                .toFile()
                .getAbsolutePath();

        DedupConfig dedupConf = DedupConfig.load(readFileFromHDFS(Paths
                .get(DedupLocalTest.class.getResource("/eu/dnetlib/pace/config/organization.current.conf.json").toURI())
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
    @Disabled
    public void asMapDocument() throws Exception {

        final String json = "{\"dataInfo\": {\"provenanceaction\": {\"classid\": \"sysimport:crosswalk:datasetarchive\", \"classname\": \"Harvested\", \"schemeid\": \"dnet:provenanceActions\", \"schemename\": \"dnet:provenanceActions\"}, \"deletedbyinference\": true, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"resourcetype\": {\"classid\": \"0001\", \"classname\": \"0001\", \"schemeid\": \"dnet:dataCite_resource\", \"schemename\": \"dnet:dataCite_resource\"}, \"pid\": [{\"dataInfo\": {\"provenanceaction\": {\"classid\": \"sysimport:crosswalk:datasetarchive\", \"classname\": \"Harvested\", \"schemeid\": \"dnet:provenanceActions\", \"schemename\": \"dnet:provenanceActions\"}, \"deletedbyinference\": false, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"qualifier\": {\"classid\": \"handle\", \"classname\": \"Handle\", \"schemeid\": \"dnet:pid_types\", \"schemename\": \"dnet:pid_types\"}, \"value\": \"11370/8fb3e34b-47c4-4e87-a675-c889db060e19\"}], \"contributor\": [], \"bestaccessright\": {\"classid\": \"CLOSED\", \"classname\": \"Closed Access\", \"schemeid\": \"dnet:access_modes\", \"schemename\": \"dnet:access_modes\"}, \"relevantdate\": [{\"dataInfo\": {\"provenanceaction\": {\"classid\": \"sysimport:crosswalk:datasetarchive\", \"classname\": \"Harvested\", \"schemeid\": \"dnet:provenanceActions\", \"schemename\": \"dnet:provenanceActions\"}, \"deletedbyinference\": false, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"qualifier\": {\"classid\": \"issued\", \"classname\": \"issued\", \"schemeid\": \"dnet:dataCite_date\", \"schemename\": \"dnet:dataCite_date\"}, \"value\": \"2018-01-01\"}], \"collectedfrom\": [{\"dataInfo\": null, \"key\": \"10|openaire____::c6df70599aa984f16ee52b4b86d2e89f\", \"value\": \"DANS (Data Archiving and Networked Services)\"}], \"id\": \"50|DansKnawCris::4ad8a5701b7d06b851966e1b323a2a95\", \"subject\": [{\"dataInfo\": {\"provenanceaction\": {\"classid\": \"sysimport:crosswalk:datasetarchive\", \"classname\": \"Harvested\", \"schemeid\": \"dnet:provenanceActions\", \"schemename\": \"dnet:provenanceActions\"}, \"deletedbyinference\": false, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"qualifier\": {\"classid\": \"keyword\", \"classname\": \"keyword\", \"schemeid\": \"dnet:subject_classification_typologies\", \"schemename\": \"dnet:subject_classification_typologies\"}, \"value\": \"OF-THE-ART\"}, {\"dataInfo\": {\"provenanceaction\": {\"classid\": \"sysimport:crosswalk:datasetarchive\", \"classname\": \"Harvested\", \"schemeid\": \"dnet:provenanceActions\", \"schemename\": \"dnet:provenanceActions\"}, \"deletedbyinference\": false, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"qualifier\": {\"classid\": \"keyword\", \"classname\": \"keyword\", \"schemeid\": \"dnet:subject_classification_typologies\", \"schemename\": \"dnet:subject_classification_typologies\"}, \"value\": \"SCHOOL LEADERSHIP\"}, {\"dataInfo\": {\"provenanceaction\": {\"classid\": \"sysimport:crosswalk:datasetarchive\", \"classname\": \"Harvested\", \"schemeid\": \"dnet:provenanceActions\", \"schemename\": \"dnet:provenanceActions\"}, \"deletedbyinference\": false, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"qualifier\": {\"classid\": \"keyword\", \"classname\": \"keyword\", \"schemeid\": \"dnet:subject_classification_typologies\", \"schemename\": \"dnet:subject_classification_typologies\"}, \"value\": \"DYNAMIC-MODEL\"}, {\"dataInfo\": {\"provenanceaction\": {\"classid\": \"sysimport:crosswalk:datasetarchive\", \"classname\": \"Harvested\", \"schemeid\": \"dnet:provenanceActions\", \"schemename\": \"dnet:provenanceActions\"}, \"deletedbyinference\": false, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"qualifier\": {\"classid\": \"keyword\", \"classname\": \"keyword\", \"schemeid\": \"dnet:subject_classification_typologies\", \"schemename\": \"dnet:subject_classification_typologies\"}, \"value\": \"IMPLEMENTATION\"}, {\"dataInfo\": {\"provenanceaction\": {\"classid\": \"sysimport:crosswalk:datasetarchive\", \"classname\": \"Harvested\", \"schemeid\": \"dnet:provenanceActions\", \"schemename\": \"dnet:provenanceActions\"}, \"deletedbyinference\": false, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"qualifier\": {\"classid\": \"keyword\", \"classname\": \"keyword\", \"schemeid\": \"dnet:subject_classification_typologies\", \"schemename\": \"dnet:subject_classification_typologies\"}, \"value\": \"OUTCOMES\"}, {\"dataInfo\": {\"provenanceaction\": {\"classid\": \"sysimport:crosswalk:datasetarchive\", \"classname\": \"Harvested\", \"schemeid\": \"dnet:provenanceActions\", \"schemename\": \"dnet:provenanceActions\"}, \"deletedbyinference\": false, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"qualifier\": {\"classid\": \"keyword\", \"classname\": \"keyword\", \"schemeid\": \"dnet:subject_classification_typologies\", \"schemename\": \"dnet:subject_classification_typologies\"}, \"value\": \"To be checked by Faculty\"}], \"embargoenddate\": null, \"lastupdatetimestamp\": 1645007805304, \"author\": [{\"surname\": \"Kyriakides\", \"name\": \"Leonidas\", \"pid\": [], \"rank\": 1, \"affiliation\": [], \"fullname\": \"Kyriakides, Leonidas\"}, {\"surname\": \"Georgiou\", \"name\": \"Maria P.\", \"pid\": [], \"rank\": 2, \"affiliation\": [], \"fullname\": \"Georgiou, Maria P.\"}, {\"surname\": \"Creemers\", \"name\": \"Bert P. M.\", \"pid\": [], \"rank\": 3, \"affiliation\": [], \"fullname\": \"Creemers, Bert P. M.\"}, {\"surname\": \"Panayiotou\", \"name\": \"Anastasia\", \"pid\": [], \"rank\": 4, \"affiliation\": [], \"fullname\": \"Panayiotou, Anastasia\"}, {\"surname\": \"Reynolds\", \"name\": \"David\", \"pid\": [], \"rank\": 5, \"affiliation\": [], \"fullname\": \"Reynolds, David\"}], \"instance\": [{\"refereed\": {\"classid\": \"0000\", \"classname\": \"UNKNOWN\", \"schemeid\": \"dnet:review_levels\", \"schemename\": \"dnet:review_levels\"}, \"hostedby\": {\"dataInfo\": null, \"key\": \"10|issn___print::2763d4e6e2e870a7ffd4bf8863c6bbff\", \"value\": \"School Effectiveness and School Improvement\"}, \"accessright\": {\"classid\": \"CLOSED\", \"classname\": \"Closed Access\", \"schemeid\": \"dnet:access_modes\", \"schemename\": \"dnet:access_modes\", \"openAccessRoute\": null}, \"license\": null, \"url\": [\"\", \"http://dx.doi.org/10.1080/09243453.2017.1398761\"], \"measures\": null, \"pid\": [{\"dataInfo\": {\"provenanceaction\": {\"classid\": \"sysimport:crosswalk:datasetarchive\", \"classname\": \"Harvested\", \"schemeid\": \"dnet:provenanceActions\", \"schemename\": \"dnet:provenanceActions\"}, \"deletedbyinference\": false, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"qualifier\": {\"classid\": \"handle\", \"classname\": \"Handle\", \"schemeid\": \"dnet:pid_types\", \"schemename\": \"dnet:pid_types\"}, \"value\": \"11370/8fb3e34b-47c4-4e87-a675-c889db060e19\"}], \"distributionlocation\": null, \"processingchargecurrency\": null, \"alternateIdentifier\": [{\"dataInfo\": {\"provenanceaction\": {\"classid\": \"sysimport:crosswalk:datasetarchive\", \"classname\": \"Harvested\", \"schemeid\": \"dnet:provenanceActions\", \"schemename\": \"dnet:provenanceActions\"}, \"deletedbyinference\": false, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"qualifier\": {\"classid\": \"urn\", \"classname\": \"urn\", \"schemeid\": \"dnet:pid_types\", \"schemename\": \"dnet:pid_types\"}, \"value\": \"urn:nbn:nl:ui:11-8fb3e34b-47c4-4e87-a675-c889db060e19\"}, {\"dataInfo\": {\"provenanceaction\": {\"classid\": \"sysimport:crosswalk:datasetarchive\", \"classname\": \"Harvested\", \"schemeid\": \"dnet:provenanceActions\", \"schemename\": \"dnet:provenanceActions\"}, \"deletedbyinference\": false, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"qualifier\": {\"classid\": \"doi\", \"classname\": \"Digital Object Identifier\", \"schemeid\": \"dnet:pid_types\", \"schemename\": \"dnet:pid_types\"}, \"value\": \"10.1080/09243453.2017.1398761\"}], \"dateofacceptance\": {\"dataInfo\": {\"provenanceaction\": {\"classid\": \"sysimport:crosswalk:datasetarchive\", \"classname\": \"Harvested\", \"schemeid\": \"dnet:provenanceActions\", \"schemename\": \"dnet:provenanceActions\"}, \"deletedbyinference\": false, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"value\": \"2018-01-01\"}, \"collectedfrom\": {\"dataInfo\": null, \"key\": \"10|openaire____::c6df70599aa984f16ee52b4b86d2e89f\", \"value\": \"DANS (Data Archiving and Networked Services)\"}, \"processingchargeamount\": null, \"instancetype\": {\"classid\": \"0001\", \"classname\": \"Article\", \"schemeid\": \"dnet:publication_resource\", \"schemename\": \"dnet:publication_resource\"}}], \"resulttype\": {\"classid\": \"publication\", \"classname\": \"publication\", \"schemeid\": \"dnet:result_typologies\", \"schemename\": \"dnet:result_typologies\"}, \"dateofcollection\": \"2020-11-12T23:04:09.482Z\", \"fulltext\": [], \"dateoftransformation\": \"2020-11-13T01:33:00.881Z\", \"description\": [{\"dataInfo\": {\"provenanceaction\": {\"classid\": \"sysimport:crosswalk:datasetarchive\", \"classname\": \"Harvested\", \"schemeid\": \"dnet:provenanceActions\", \"schemename\": \"dnet:provenanceActions\"}, \"deletedbyinference\": false, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"value\": \"This paper investigates the impact of national policies for improving teaching and the school learning environment (SLE) on student achievement. In each participating country (i.e., Belgium/Flanders, Cyprus, Germany, Greece, Ireland, and Slovenia), a sample of at least 50 schools was drawn and tests in mathematics and science were administered to all Grade 4 students (N = 10,742) at the beginning and end of school year 2010\\u20132011. National policies were measured through (a) content analysis of policy documents, (b) interviews with policymakers, and (c) head-teacher questionnaires. Multilevel analyses revealed that most aspects of national policies for teaching and SLE were associated with student achievement in each subject irrespective of the source of data used to measure them. Implications are, finally, drawn.\"}], \"format\": [], \"processingchargecurrency\": null, \"journal\": {\"issnPrinted\": \"0924-3453\", \"conferencedate\": null, \"vol\": \"29\", \"conferenceplace\": null, \"name\": \"School Effectiveness and School Improvement\", \"iss\": \"2\", \"sp\": \"171\", \"edition\": \"\", \"dataInfo\": {\"provenanceaction\": {\"classid\": \"sysimport:crosswalk:datasetarchive\", \"classname\": \"Harvested\", \"schemeid\": \"dnet:provenanceActions\", \"schemename\": \"dnet:provenanceActions\"}, \"deletedbyinference\": false, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"issnOnline\": \"\", \"ep\": \"203\", \"issnLinking\": \"\"}, \"measures\": null, \"dateofacceptance\": {\"dataInfo\": {\"provenanceaction\": {\"classid\": \"sysimport:crosswalk:datasetarchive\", \"classname\": \"Harvested\", \"schemeid\": \"dnet:provenanceActions\", \"schemename\": \"dnet:provenanceActions\"}, \"deletedbyinference\": false, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"value\": \"2018-01-01\"}, \"coverage\": [], \"processingchargeamount\": null, \"externalReference\": [], \"publisher\": null, \"language\": {\"classid\": \"eng\", \"classname\": \"English\", \"schemeid\": \"dnet:languages\", \"schemename\": \"dnet:languages\"}, \"oaiprovenance\": {\"originDescription\": {\"metadataNamespace\": \"\", \"harvestDate\": \"2020-11-12T23:04:09.482Z\", \"baseURL\": \"http%3A%2F%2Fservices.nod.dans.knaw.nl%2Foa-cerif\", \"datestamp\": \"2020-11-12T00:51:43Z\", \"altered\": true, \"identifier\": \"oai:services.nod.dans.knaw.nl:Publications/rug:oai:pure.rug.nl:publications/8fb3e34b-47c4-4e87-a675-c889db060e19\"}}, \"country\": [], \"extraInfo\": [], \"originalId\": [\"oai:services.nod.dans.knaw.nl:Publications/rug:oai:pure.rug.nl:publications/8fb3e34b-47c4-4e87-a675-c889db060e19\", \"50|DansKnawCris::4ad8a5701b7d06b851966e1b323a2a95\"], \"source\": [], \"context\": [], \"title\": [{\"dataInfo\": {\"provenanceaction\": {\"classid\": \"sysimport:crosswalk:datasetarchive\", \"classname\": \"Harvested\", \"schemeid\": \"dnet:provenanceActions\", \"schemename\": \"dnet:provenanceActions\"}, \"deletedbyinference\": false, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"qualifier\": {\"classid\": \"main title\", \"classname\": \"main title\", \"schemeid\": \"dnet:dataCite_title\", \"schemename\": \"dnet:dataCite_title\"}, \"value\": \"The impact of national educational policies on student achievement\"}, {\"dataInfo\": {\"provenanceaction\": {\"classid\": \"sysimport:crosswalk:datasetarchive\", \"classname\": \"Harvested\", \"schemeid\": \"dnet:provenanceActions\", \"schemename\": \"dnet:provenanceActions\"}, \"deletedbyinference\": false, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"0.9\"}, \"qualifier\": {\"classid\": \"subtitle\", \"classname\": \"subtitle\", \"schemeid\": \"dnet:dataCite_title\", \"schemename\": \"dnet:dataCite_title\"}, \"value\": \"a European study\"}]}";

        DedupConfig dedupConfig = DedupConfig.load(readFileFromHDFS(
                Paths.get(DedupLocalTest.class.getResource("/eu/dnetlib/pace/config/pub.new.tree.conf.json").toURI()).toFile().getAbsolutePath()
        ));

        final MapDocument mapDocument = MapDocumentUtil.asMapDocumentWithJPath(dedupConfig, json);

        for(String field: mapDocument.getFieldMap().keySet()) {
            System.out.println(field + ": " + mapDocument.getFieldMap().get(field).stringValue());
        }
    }

    @Test
    @Disabled
    public void noMatchTest() throws Exception {

        //custom parameters for this test
        DedupConfig dedupConfig = DedupConfig.load(readFileFromHDFS(
                Paths.get(DedupLocalTest.class.getResource("/eu/dnetlib/pace/config/pub.new.tree.conf.json").toURI()).toFile().getAbsolutePath()
        ));
        String inputPath = Paths.get(DedupLocalTest.class.getResource("/eu/dnetlib/pace/examples/publications.dump.1000.json").toURI()).toFile().getAbsolutePath();
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