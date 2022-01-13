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
        String json1 = "{\"context\": [], \"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"sysimport:crosswalk:repository\", \"classname\": \"sysimport:crosswalk:repository\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}, \"inferred\": true, \"inferenceprovenance\": \"dedup-similarity-result-levenstein\", \"invisible\": false, \"trust\": \"0.9\"}, \"resourcetype\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"pid\": [], \"contributor\": [], \"resulttype\": {\"classid\": \"publication\", \"classname\": \"publication\", \"schemename\": \"dnet:result_typologies\", \"schemeid\": \"dnet:result_typologies\"}, \"relevantdate\": [], \"collectedfrom\": [{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"Scientific Electronic Library Online - Brazil\", \"key\": \"10|opendoar____::996a7fa078cc36c46d02f9af3bef918b\"}], \"id\": \"50|od_______608::5cea59e3711ee7fa68f626b4350ca947\", \"subject\": [{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"qualifier\": {\"classid\": \"keyword\", \"classname\": \"keyword\", \"schemename\": \"dnet:result_subject\", \"schemeid\": \"dnet:result_subject\"}, \"value\": \"dengue 4\"}, {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"qualifier\": {\"classid\": \"keyword\", \"classname\": \"keyword\", \"schemename\": \"dnet:result_subject\", \"schemeid\": \"dnet:result_subject\"}, \"value\": \"epidemiology\"}, {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"qualifier\": {\"classid\": \"keyword\", \"classname\": \"keyword\", \"schemename\": \"dnet:result_subject\", \"schemeid\": \"dnet:result_subject\"}, \"value\": \"deaths\"}, {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"qualifier\": {\"classid\": \"keyword\", \"classname\": \"keyword\", \"schemename\": \"dnet:result_subject\", \"schemeid\": \"dnet:result_subject\"}, \"value\": \"genotyping\"}, {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"qualifier\": {\"classid\": \"keyword\", \"classname\": \"keyword\", \"schemename\": \"dnet:result_subject\", \"schemeid\": \"dnet:result_subject\"}, \"value\": \"Cear\\\\\\\\xe1/Brazil\"}], \"embargoenddate\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"lastupdatetimestamp\": 0, \"author\": [{\"surname\": \"Ramalho\", \"name\": \"Izabel Leti\\\\\\\\u0301cia Cavalcante\", \"pid\": [], \"rank\": 1, \"affiliation\": [], \"fullname\": \"Ramalho, Izabel Let\\\\\\\\xedcia Cavalcante\"}, {\"surname\": \"Arau\\\\\\\\u0301jo\", \"name\": \"Fernanda Montenegro Carvalho\", \"pid\": [], \"rank\": 2, \"affiliation\": [], \"fullname\": \"Ara\\\\\\\\xfajo, Fernanda Montenegro de Carvalho\"}, {\"surname\": \"Cavalcanti\", \"name\": \"Luciano Pamplona Go\\\\\\\\u0301es\", \"pid\": [], \"rank\": 3, \"affiliation\": [], \"fullname\": \"Cavalcanti, Luciano Pamplona de G\\\\\\\\xf3es\"}, {\"surname\": \"Braga\", \"name\": \"Deborah Nunes Melo\", \"pid\": [], \"rank\": 4, \"affiliation\": [], \"fullname\": \"Braga, Deborah Nunes Melo\"}, {\"surname\": \"Perdiga\\\\\\\\u0303o\", \"name\": \"Anne Carolinne Bezerra\", \"pid\": [], \"rank\": 5, \"affiliation\": [], \"fullname\": \"Perdig\\\\\\\\xe3o, Anne Carolinne Bezerra\"}, {\"surname\": \"Santos\", \"name\": \"Flavia Barreto Dos\", \"pid\": [], \"rank\": 6, \"affiliation\": [], \"fullname\": \"Santos, Flavia Barreto dos\"}, {\"surname\": \"Nogueira\", \"name\": \"Fernanda Bruycker\", \"pid\": [], \"rank\": 7, \"affiliation\": [], \"fullname\": \"Nogueira, Fernanda de Bruycker\"}, {\"surname\": \"Esco\\\\\\\\u0301ssia\", \"name\": \"Kiliana Nogueira Farias Da\", \"pid\": [], \"rank\": 8, \"affiliation\": [], \"fullname\": \"Esc\\\\\\\\xf3ssia, Kiliana Nogueira Farias da\"}, {\"surname\": \"Guedes\", \"name\": \"Maria Izabel Florindo\", \"pid\": [], \"rank\": 9, \"affiliation\": [], \"fullname\": \"Guedes, Maria Izabel Florindo\"}], \"instance\": [{\"refereed\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"hostedby\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"Scientific Electronic Library Online - Brazil\", \"key\": \"10|opendoar____::996a7fa078cc36c46d02f9af3bef918b\"}, \"processingchargeamount\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"license\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"http://creativecommons.org/licenses/by/4.0/\"}, \"url\": [\"http://www.scielo.br/scielo.php?script=sci_arttext&pid=S0074-02762018001100300&lng=en&tlng=en\"], \"distributionlocation\": \"\", \"processingchargecurrency\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"dateofacceptance\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"2018-10-18\"}, \"collectedfrom\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"Scientific Electronic Library Online - Brazil\", \"key\": \"10|opendoar____::996a7fa078cc36c46d02f9af3bef918b\"}, \"accessright\": {\"classid\": \"OPEN\", \"classname\": \"Open Access\", \"schemename\": \"dnet:access_modes\", \"schemeid\": \"dnet:access_modes\"}, \"instancetype\": {\"classid\": \"0001\", \"classname\": \"Article\", \"schemename\": \"dnet:publication_resource\", \"schemeid\": \"dnet:publication_resource\"}}], \"dateofcollection\": \"2018-10-17T15:29:29.5Z\", \"fulltext\": [], \"dateoftransformation\": \"2020-03-05T17:09:49.618Z\", \"description\": [{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"BACKGROUND The first dengue cases in Brazil with laboratory confirmation occurred in the northern region of the country, with the isolation of two serotypes, dengue virus 1 (DENV-1) and DENV-4. In Cear\\\\\\\\xe1, the introduction of DENV-4 was reported during a DENV-1 epidemic in 2011, with only two isolations. OBJECTIVES The aim of this study was to characterise the first DENV-4 epidemic in the state of Cear\\\\\\\\xe1, Brazil. METHODS The study population was composed of patients with suspected dengue that were reported to health care units from January to December 2012. The laboratory confirmation of infection was made by viral isolation, reverse transcription polymerase chain reaction (RT-PCR), AgNS1, immunohistochemistry and IgM enzyme-linked immunosorbent assay (ELISA). MAIN CONCLUSIONS In the study year, 72,211 suspected dengue cases were reported and 51,865 of these cases (71.8%) were confirmed to be positive. Co-circulation of three serotypes, DENV-1, DENV-3 and DENV-4, was detected with a predominance of DENV-4 (95.3%). Most cases were not severe, but there were 44 fatal outcomes. DENV-4 Genotype II was identified for the first time in Cear\\\\\\\\xe1.\"}], \"format\": [{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"text/html\"}], \"journal\": {\"issnPrinted\": \"\", \"conferencedate\": \"\", \"conferenceplace\": \"\", \"name\": \"\", \"edition\": \"\", \"vol\": \"\", \"sp\": \"\", \"iss\": \"\", \"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"issnOnline\": \"\", \"ep\": \"\", \"issnLinking\": \"\"}, \"coverage\": [], \"publisher\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"Instituto Oswaldo Cruz, Minist\\\\\\\\xe9rio da Sa\\\\\\\\xfade\"}, \"language\": {\"classid\": \"eng\", \"classname\": \"English\", \"schemename\": \"dnet:languages\", \"schemeid\": \"dnet:languages\"}, \"bestaccessright\": {\"classid\": \"OPEN\", \"classname\": \"Open Access\", \"schemename\": \"dnet:access_modes\", \"schemeid\": \"dnet:access_modes\"}, \"country\": [], \"extraInfo\": [], \"originalId\": [\"oai:scielo:S0074-02762018001100300\"], \"source\": [{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"Mem\\\\\\\\xf3rias do Instituto Oswaldo Cruz, Volume: 113, Issue: 11, Article number: e180320, Published: 18 OCT 2018\"}], \"dateofacceptance\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"2018-10-18\"}, \"title\": [{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"qualifier\": {\"classid\": \"main title\", \"classname\": \"main title\", \"schemename\": \"dnet:dataCite_title\", \"schemeid\": \"dnet:dataCite_title\"}, \"value\": \"Dengue 4 in Cear\\\\\\\\xe1, Brazil: characterisation of epidemiological and laboratorial aspects and causes of death during the first epidemic in the state\"}]}";
        String json2 = "{\"context\": [], \"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"sysimport:actionset\", \"classname\": \"sysimport:actionset\", \"schemename\": \"dnet:provenanceActions\", \"schemeid\": \"dnet:provenanceActions\"}, \"inferred\": true, \"inferenceprovenance\": \"dedup-similarity-result-levenstein\", \"invisible\": false, \"trust\": \"0.9\"}, \"resourcetype\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"pid\": [{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"qualifier\": {\"classid\": \"doi\", \"classname\": \"doi\", \"schemename\": \"dnet:pid_types\", \"schemeid\": \"dnet:pid_types\"}, \"value\": \"10.1590/0074-02760180320\"}], \"contributor\": [], \"resulttype\": {\"classid\": \"publication\", \"classname\": \"publication\", \"schemename\": \"dnet:result_typologies\", \"schemeid\": \"dnet:result_typologies\"}, \"relevantdate\": [{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"qualifier\": {\"classid\": \"published-online\", \"classname\": \"published-online\", \"schemename\": \"dnet:dataCite_date\", \"schemeid\": \"dnet:dataCite_date\"}, \"value\": \"2018-10-18\"}], \"collectedfrom\": [{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"Crossref\", \"key\": \"10|openaire____::081b82f96300b6a6e3d282bad31cb6e2\"}, {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"Microsoft Academic Graph\", \"key\": \"10|openaire____::5f532a3fc4f1ea403f37070f59a7a53a\"}, {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"ORCID\", \"key\": \"10|openaire____::806360c771262b4d6770e7cdf04b5c5a\"}, {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"UnpayWall\", \"key\": \"10|openaire____::8ac8380272269217cb09a928c8caa993\"}], \"id\": \"50|doiboost____::ceb0e80cefc02755597cfc1221fc5582\", \"subject\": [{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"qualifier\": {\"classid\": \"keyword\", \"classname\": \"keyword\", \"schemename\": \"dnet:subject\", \"schemeid\": \"dnet:subject\"}, \"value\": \"Microbiology (medical)\"}], \"embargoenddate\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"lastupdatetimestamp\": 0, \"author\": [{\"surname\": \"Ramalho\", \"name\": \"Izabel Let\\\\\\\\xedcia Cavalcante\", \"pid\": [{\"qualifier\": {\"classid\": \"MAG Identifier\", \"classname\": \"MAG Identifier\"}, \"value\": \"2278015306\"}], \"rank\": 1, \"affiliation\": [], \"fullname\": \"Izabel Let\\\\\\\\xedcia Cavalcante Ramalho\"}, {\"surname\": \"Ara\\\\\\\\xfajo\", \"name\": \"Fernanda Montenegro de Carvalho\", \"pid\": [{\"qualifier\": {\"classid\": \"MAG Identifier\", \"classname\": \"MAG Identifier\"}, \"value\": \"2155354179\"}], \"rank\": 2, \"affiliation\": [], \"fullname\": \"Fernanda Montenegro de Carvalho Ara\\\\\\\\xfajo\"}, {\"surname\": \"Cavalcanti\", \"name\": \"Luciano Pamplona de G\\\\\\\\xf3es\", \"pid\": [{\"qualifier\": {\"classid\": \"ORCID\", \"classname\": \"ORCID\"}, \"value\": \"0000-0002-3440-1182\"}, {\"qualifier\": {\"classid\": \"MAG Identifier\", \"classname\": \"MAG Identifier\"}, \"value\": \"2169061508\"}], \"rank\": 3, \"affiliation\": [{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"Federal University of Cear\\\\\\\\xe1\"}], \"fullname\": \"Luciano Pamplona de G\\\\\\\\xf3es Cavalcanti\"}, {\"surname\": \"Braga\", \"name\": \"Deborah Nunes Melo\", \"pid\": [{\"qualifier\": {\"classid\": \"MAG Identifier\", \"classname\": \"MAG Identifier\"}, \"value\": \"2269038449\"}], \"rank\": 4, \"affiliation\": [], \"fullname\": \"Deborah Nunes Melo Braga\"}, {\"surname\": \"Perdig\\\\\\\\xe3o\", \"name\": \"Anne Carolinne Bezerra\", \"pid\": [{\"qualifier\": {\"classid\": \"MAG Identifier\", \"classname\": \"MAG Identifier\"}, \"value\": \"2052668005\"}], \"rank\": 5, \"affiliation\": [], \"fullname\": \"Anne Carolinne Bezerra Perdig\\\\\\\\xe3o\"}, {\"surname\": \"Santos\", \"name\": \"Flavia Barreto dos\", \"pid\": [{\"qualifier\": {\"classid\": \"ORCID\", \"classname\": \"ORCID\"}, \"value\": \"0000-0002-1309-5366\"}, {\"qualifier\": {\"classid\": \"MAG Identifier\", \"classname\": \"MAG Identifier\"}, \"value\": \"2557272706\"}], \"rank\": 6, \"affiliation\": [{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"Oswaldo Cruz Foundation\"}], \"fullname\": \"Flavia Barreto dos Santos\"}, {\"surname\": \"Nogueira\", \"name\": \"Fernanda de Bruycker\", \"pid\": [{\"qualifier\": {\"classid\": \"MAG Identifier\", \"classname\": \"MAG Identifier\"}, \"value\": \"2335476746\"}], \"rank\": 7, \"affiliation\": [{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"Oswaldo Cruz Foundation\"}], \"fullname\": \"Fernanda de Bruycker Nogueira\"}, {\"surname\": \"Esc\\\\\\\\xf3ssia\", \"name\": \"Kiliana Nogueira Farias da\", \"pid\": [{\"qualifier\": {\"classid\": \"MAG Identifier\", \"classname\": \"MAG Identifier\"}, \"value\": \"329386728\"}], \"rank\": 8, \"affiliation\": [], \"fullname\": \"Kiliana Nogueira Farias da Esc\\\\\\\\xf3ssia\"}, {\"surname\": \"Guedes\", \"name\": \"Maria Izabel Florindo\", \"pid\": [{\"qualifier\": {\"classid\": \"MAG Identifier\", \"classname\": \"MAG Identifier\"}, \"value\": \"2132750169\"}], \"rank\": 9, \"affiliation\": [], \"fullname\": \"Maria Izabel Florindo Guedes\"}], \"instance\": [{\"refereed\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"hostedby\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"Unknown Repository\", \"key\": \"10|openaire____::55045bd2a65019fd8e6741a755395c8c\"}, \"processingchargeamount\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"license\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"url\": [\"http://www.scielo.br/pdf/mioc/v113n11/1678-8060-mioc-113-11-e180320.pdf\"], \"distributionlocation\": \"\", \"processingchargecurrency\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"dateofacceptance\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"collectedfrom\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"UnpayWall\", \"key\": \"10|openaire____::8ac8380272269217cb09a928c8caa993\"}, \"accessright\": {\"classid\": \"OPEN\", \"classname\": \"Open Access\", \"schemename\": \"dnet:access_modes\", \"schemeid\": \"dnet:access_modes\"}, \"instancetype\": {\"classid\": \"0001\", \"classname\": \"Article\", \"schemename\": \"dnet:publication_resource\", \"schemeid\": \"dnet:publication_resource\"}}, {\"refereed\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"hostedby\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"Unknown Repository\", \"key\": \"10|openaire____::55045bd2a65019fd8e6741a755395c8c\"}, \"processingchargeamount\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"license\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"url\": [\"http://www.scielo.br/pdf/mioc/v113n11/1678-8060-mioc-113-11-e180320.pdf\"], \"distributionlocation\": \"\", \"processingchargecurrency\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"dateofacceptance\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"collectedfrom\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"Crossref\", \"key\": \"10|openaire____::081b82f96300b6a6e3d282bad31cb6e2\"}, \"accessright\": {\"classid\": \"UNKNOWN\", \"classname\": \"not available\", \"schemename\": \"dnet:access_modes\", \"schemeid\": \"dnet:access_modes\"}, \"instancetype\": {\"classid\": \"0001\", \"classname\": \"Article\", \"schemename\": \"dnet:publication_resource\", \"schemeid\": \"dnet:publication_resource\"}}, {\"refereed\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"hostedby\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"Unknown Repository\", \"key\": \"10|openaire____::55045bd2a65019fd8e6741a755395c8c\"}, \"processingchargeamount\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"license\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"url\": [\"https://academic.microsoft.com/#/detail/2897780704\"], \"distributionlocation\": \"\", \"processingchargecurrency\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"dateofacceptance\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"collectedfrom\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"Microsoft Academic Graph\", \"key\": \"10|openaire____::5f532a3fc4f1ea403f37070f59a7a53a\"}, \"accessright\": {\"classid\": \"UNKNOWN\", \"classname\": \"not available\", \"schemename\": \"dnet:access_modes\", \"schemeid\": \"dnet:access_modes\"}, \"instancetype\": {\"classid\": \"0001\", \"classname\": \"Article\", \"schemename\": \"dnet:publication_resource\", \"schemeid\": \"dnet:publication_resource\"}}, {\"refereed\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"hostedby\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"Mem\\\\\\\\xf3rias do Instituto Oswaldo Cruz.\", \"key\": \"10|doajarticles::91d64232489446b40bc194bf95493001\"}, \"processingchargeamount\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"license\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"url\": [\"http://dx.doi.org/10.1590/0074-02760180320\"], \"distributionlocation\": \"\", \"processingchargecurrency\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"dateofacceptance\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"collectedfrom\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"Crossref\", \"key\": \"10|openaire____::081b82f96300b6a6e3d282bad31cb6e2\"}, \"accessright\": {\"classid\": \"OPEN\", \"classname\": \"Open Access\", \"schemename\": \"dnet:access_modes\", \"schemeid\": \"dnet:access_modes\"}, \"instancetype\": {\"classid\": \"0001\", \"classname\": \"Article\", \"schemename\": \"dnet:publication_resource\", \"schemeid\": \"dnet:publication_resource\"}}], \"dateofcollection\": \"2019-02-15\", \"fulltext\": [], \"dateoftransformation\": \"\", \"description\": [{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"The first dengue cases in Brazil with laboratory confirmation occurred in the northern region of the country, with the isolation of two serotypes, dengue virus 1 (DENV-1) and DENV-4. In Ceara, the introduction of DENV-4 was reported during a DENV-1 epidemic in 2011, with only two isolations. OBJECTIVES The aim of this study was to characterise the first DENV-4 epidemic in the state of Ceara, Brazil. METHODS The study population was composed of patients with suspected dengue that were reported to health care units from January to December 2012. The laboratory confirmation of infection was made by viral isolation, reverse transcription polymerase chain reaction (RT-PCR), AgNS1, immunohistochemistry and IgM enzyme-linked immunosorbent assay (ELISA). MAIN CONCLUSIONS In the study year, 72,211 suspected dengue cases were reported and 51,865 of these cases (71.8%) were confirmed to be positive. Co-circulation of three serotypes, DENV-1, DENV-3 and DENV-4, was detected with a predominance of DENV-4 (95.3%). Most cases were not severe, but there were 44 fatal outcomes. DENV-4 Genotype II was identified for the first time in Ceara\"}], \"format\": [], \"journal\": {\"issnPrinted\": \"0074-0276\", \"conferencedate\": \"\", \"conferenceplace\": \"\", \"name\": \"FapUNIFESP (SciELO)\", \"edition\": \"\", \"vol\": \"\", \"sp\": \"\", \"iss\": \"\", \"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"issnOnline\": \"1678-8060\", \"ep\": \"\", \"issnLinking\": \"\"}, \"coverage\": [], \"publisher\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"\"}, \"language\": {\"classid\": \"und\", \"classname\": \"Undetermined\", \"schemename\": \"dnet:languages\", \"schemeid\": \"dnet:languages\"}, \"bestaccessright\": {\"classid\": \"OPEN\", \"classname\": \"Open Access\", \"schemename\": \"dnet:access_modes\", \"schemeid\": \"dnet:access_modes\"}, \"country\": [], \"extraInfo\": [], \"originalId\": [\"10.1590/0074-02760180320\"], \"source\": [], \"dateofacceptance\": {\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"value\": \"2018-10-18\"}, \"title\": [{\"dataInfo\": {\"deletedbyinference\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemename\": \"\", \"schemeid\": \"\"}, \"inferred\": false, \"inferenceprovenance\": \"\", \"invisible\": false, \"trust\": \"\"}, \"qualifier\": {\"classid\": \"main title\", \"classname\": \"main title\", \"schemename\": \"dnet:dataCite_title\", \"schemeid\": \"dnet:dataCite_title\"}, \"value\": \"Dengue 4 in Cear\\\\\\\\xe1, Brazil: characterisation of epidemiological and laboratorial aspects and causes of death during the first epidemic in the state\"}]}";

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