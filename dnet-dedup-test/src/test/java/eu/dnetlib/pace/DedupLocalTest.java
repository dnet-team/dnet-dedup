package eu.dnetlib.pace;

import eu.dnetlib.Deduper;
import eu.dnetlib.pace.config.DedupConfig;
import eu.dnetlib.pace.model.MapDocument;
import eu.dnetlib.pace.tree.support.TreeProcessor;
import eu.dnetlib.pace.tree.support.TreeStats;
import eu.dnetlib.pace.util.MapDocumentUtil;
import eu.dnetlib.pace.utils.Utility;
import eu.dnetlib.support.ConnectedComponent;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import scala.Tuple2;

import java.net.URL;
import java.util.List;
import java.util.Map;

public class DedupLocalTest extends DedupTestUtils {

    JavaSparkContext context;
    JavaRDD<String> entities;
    DedupConfig config;
    TreeProcessor treeProcessor;

    @Before
    public void setup() {

        config = DedupConfig.load(Utility.readFromClasspath("/eu/dnetlib/pace/config/organization.current.conf.json", DedupLocalTest.class));
        treeProcessor = new TreeProcessor(config);

        final SparkSession spark = SparkSession
                .builder()
                .appName("Deduplication")
                .master("local[*]")
                .getOrCreate();
        context = new JavaSparkContext(spark.sparkContext());

        final URL dataset = getClass().getResource("/eu/dnetlib/pace/examples/organization.to.fix.json");
        entities = context.textFile(dataset.getPath());

    }

    @Ignore
    @Test
    public void dedupTest(){

        double startTime = System.currentTimeMillis();

        JavaRDD<ConnectedComponent> ccs = Deduper.dedup(context, entities, config);

        System.out.println("total time = " + (System.currentTimeMillis()-startTime));

        printStatistics(ccs);

    }

    @Ignore
    @Test
    public void relationsTest() {

        List<String> entitiesList = entities.collect();

        //create vertexes of the graph: <ID, MapDocument>
        JavaPairRDD<String, MapDocument> mapDocs = Deduper.mapToVertexes(context, entities, config);
        Map<String, MapDocument> docsMap = mapDocs.collectAsMap();

        //create blocks for deduplication
        JavaPairRDD<String, Iterable<MapDocument>> blocks = Deduper.createBlocks(context, mapDocs, config);

        //create relations by comparing only elements in the same group
        JavaPairRDD<String, String> relationRDD = Deduper.computeRelations(context, blocks, config);

        List<Tuple2<String, String>> rels = relationRDD
                .filter(rel -> {
                    String legalname1 = getOrganizationLegalname(docsMap.get(rel._1()))
                            .replaceAll("&ndash;", " ")
                            .replaceAll("&amp;", " ")
                            .replaceAll("&quot;", " ")
                            .replaceAll("&minus;", " ")
                            .replaceAll("([0-9]+)", " $1 ")
                            .replaceAll("[^\\p{ASCII}]", "")
                            .replaceAll("[\\p{Punct}]", " ")
                            .replaceAll("\\n", " ")
                            .replaceAll("(?m)\\s+", " ")
                            .toLowerCase()
                            .trim();
                    String legalname2 = getOrganizationLegalname(docsMap.get(rel._2()))
                            .replaceAll("&ndash;", " ")
                            .replaceAll("&amp;", " ")
                            .replaceAll("&quot;", " ")
                            .replaceAll("&minus;", " ")
                            .replaceAll("([0-9]+)", " $1 ")
                            .replaceAll("[^\\p{ASCII}]", "")
                            .replaceAll("[\\p{Punct}]", " ")
                            .replaceAll("\\n", " ")
                            .replaceAll("(?m)\\s+", " ")
                            .toLowerCase()
                            .trim();
                    return !legalname1.equals(legalname2);
                })
                .collect();

        System.out.println("Dubious relations = " + rels.size());

        for (Tuple2<String,String> rel : rels) {
            System.out.println(rel._1() + " ---> " + rel._2());
            System.out.println(treeProcessor.evaluateTree(docsMap.get(rel._1()), docsMap.get(rel._2())));
            System.out.println("---------------------------------------------");
        }

    }

    @Ignore
    @Test
    public void matchTest(){

        String JSONEntity1 = "{\"dateoftransformation\":\"2019-10-14 08:59:35.295767\",\"originalId\":[\"openorgs____::0000010656\"],\"pid\":[{\"qualifier\":{\"classid\":\"ISNI\",\"classname\":\"ISNI\",\"schemename\":\"dnet:pid_types\",\"schemeid\":\"dnet:pid_types\"},\"value\":\"0000 0004 0370 7052\"},{\"qualifier\":{\"classid\":\"Wikidata\",\"classname\":\"Wikidata\",\"schemename\":\"dnet:pid_types\",\"schemeid\":\"dnet:pid_types\"},\"value\":\"Q17012267\"},{\"qualifier\":{\"classid\":\"grid.ac\",\"classname\":\"grid.ac\",\"schemename\":\"dnet:pid_types\",\"schemeid\":\"dnet:pid_types\"},\"value\":\"grid.418822.5\"}],\"collectedfrom\":[{\"value\":\"OpenOrgs Database\",\"key\":\"10|openaire____::0362fcdb3076765d9c0041ad331553e8\"}],\"organization\":{\"metadata\":{\"legalshortname\":{\"value\":\"ENVIRON (United States)\"},\"websiteurl\":{\"value\":\"http://www.ramboll-environ.com/\"},\"country\":{\"classid\":\"US\",\"classname\":\"United States\",\"schemename\":\"dnet:countries\",\"schemeid\":\"dnet:countries\"},\"alternativeNames\":[{\"value\":\"ENVIRON (United States)\"},{\"value\":\"Ramboll Environ\"}],\"legalname\":{\"value\":\"ENVIRON (United States)\"}}},\"dateofcollection\":\"\",\"type\":20,\"id\":\"20|openorgs____::d3c5966e2089c408f43aa899fd0df656\"}";
        String JSONEntity2 = "{\"dateoftransformation\":\"2018-06-04\",\"originalId\":[\"nsf_________::United_States_Military_Academy\"],\"collectedfrom\":[{\"value\":\"NSF - National Science Foundation\",\"key\":\"10|openaire____::dd69b4a1513c9de9f46faf24048da1e8\"}],\"organization\":{\"metadata\":{\"eclegalbody\":{\"value\":\"false\"},\"eclegalperson\":{\"value\":\"false\"},\"ecinternationalorganization\":{\"value\":\"false\"},\"ecnonprofit\":{\"value\":\"false\"},\"ecresearchorganization\":{\"value\":\"false\"},\"ecenterprise\":{\"value\":\"false\"},\"ecnutscode\":{\"value\":\"false\"},\"ecinternationalorganizationeurinterests\":{\"value\":\"false\"},\"legalname\":{\"value\":\"United States Military Academy\"},\"country\":{\"classid\":\"US\",\"classname\":\"United States\",\"schemename\":\"dnet:countries\",\"schemeid\":\"dnet:countries\"},\"echighereducation\":{\"value\":\"false\"},\"ecsmevalidated\":{\"value\":\"false\"}}},\"dateofcollection\":\"2016-03-10\",\"type\":20,\"id\":\"20|nsf_________::177e8a2cf0c987cf8ac33933ddf3e260\"}";

        MapDocument mapDoc1 = MapDocumentUtil.asMapDocumentWithJPath(config, JSONEntity1);
        MapDocument mapDoc2 = MapDocumentUtil.asMapDocumentWithJPath(config, JSONEntity2);

        TreeStats treeStats = treeProcessor.evaluateTree(mapDoc1, mapDoc2);

        System.out.println(treeStats);

    }
}