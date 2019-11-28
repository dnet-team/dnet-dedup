package eu.dnetlib.pace;

import eu.dnetlib.Deduper;
import eu.dnetlib.pace.config.DedupConfig;
import eu.dnetlib.pace.model.MapDocument;
import eu.dnetlib.pace.tree.support.TreeProcessor;
import eu.dnetlib.pace.tree.support.TreeStats;
import eu.dnetlib.pace.utils.PaceUtils;
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

        config = DedupConfig.load(Utility.readFromClasspath("/eu/dnetlib/pace/config/organization.strict.conf", DedupLocalTest.class));
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

        String JSONEntity1 = "{\"dateoftransformation\":\"2018-06-04\",\"originalId\":[\"opendoar____::Universiti_Sains_Malaysia\"],\"collectedfrom\":[{\"value\":\"OpenDOAR\",\"key\":\"10|openaire____::47ce9e9f4fad46e732cff06419ecaabb\"}],\"organization\":{\"metadata\":{\"eclegalbody\":{\"value\":\"false\"},\"eclegalperson\":{\"value\":\"false\"},\"ecinternationalorganization\":{\"value\":\"false\"},\"ecresearchorganization\":{\"value\":\"false\"},\"ecnonprofit\":{\"value\":\"false\"},\"ecenterprise\":{\"value\":\"false\"},\"websiteurl\":{\"value\":\"http://www.usm.my/my/\"},\"ecnutscode\":{\"value\":\"false\"},\"ecinternationalorganizationeurinterests\":{\"value\":\"false\"},\"legalname\":{\"value\":\"Universiti Sains Malaysia\"},\"country\":{\"classid\":\"MY\",\"classname\":\"Malaysia\",\"schemename\":\"dnet:countries\",\"schemeid\":\"dnet:countries\"},\"echighereducation\":{\"value\":\"false\"},\"ecsmevalidated\":{\"value\":\"false\"}}},\"dateofcollection\":\"2015-08-24\",\"type\":20,\"id\":\"20|opendoar____::04315c25b0eb56eacb967901557f86b1\"}";
        String JSONEntity2 = "{\"dateoftransformation\":\"2019-10-07\",\"originalId\":[\"corda_______::997941627\"],\"collectedfrom\":[{\"value\":\"CORDA - COmmon Research DAta Warehouse\",\"key\":\"10|openaire____::b30dac7baac631f3da7c2bb18dd9891f\"}],\"organization\":{\"metadata\":{\"eclegalbody\":{\"value\":\"true\"},\"eclegalperson\":{\"value\":\"true\"},\"ecinternationalorganization\":{\"value\":\"false\"},\"legalshortname\":{\"value\":\"USM\"},\"ecresearchorganization\":{\"value\":\"true\"},\"ecnonprofit\":{\"value\":\"true\"},\"ecenterprise\":{\"value\":\"false\"},\"websiteurl\":{\"value\":\"http://www.usm.my/my\"},\"ecnutscode\":{\"value\":\"false\"},\"ecinternationalorganizationeurinterests\":{\"value\":\"false\"},\"legalname\":{\"value\":\"UNIVERSITI SAINS MALAYSIA*\"},\"country\":{\"classid\":\"MY\",\"classname\":\"Malaysia\",\"schemename\":\"dnet:countries\",\"schemeid\":\"dnet:countries\"},\"echighereducation\":{\"value\":\"true\"}}},\"dateofcollection\":\"2015-09-10\",\"type\":20,\"id\":\"20|corda_______::1fb0c86ddf389377454d5520d2796dad\"}";

        MapDocument mapDoc1 = PaceUtils.asMapDocument(config, JSONEntity1);
        MapDocument mapDoc2 = PaceUtils.asMapDocument(config, JSONEntity2);

        TreeStats treeStats = treeProcessor.evaluateTree(mapDoc1, mapDoc2);

        System.out.println(treeStats);

    }

    @Ignore
    @Test
    public void parseJSONEntityTest(){
        String jsonEntity = "{\"dateoftransformation\":\"2018-09-19\",\"originalId\":[\"doajarticles::Sociedade_Brasileira_de_Reumatologia\"],\"collectedfrom\":[{\"value\":\"DOAJ-Articles\",\"key\":\"10|driver______::bee53aa31dc2cbb538c10c2b65fa5824\"}],\"organization\":{\"metadata\":{\"eclegalbody\":{\"value\":\"false\"},\"eclegalperson\":{\"value\":\"false\"},\"ecinternationalorganization\":{\"value\":\"false\"},\"legalshortname\":{\"value\":\"Sociedade Brasileira de Reumatologia\"},\"ecresearchorganization\":{\"value\":\"false\"},\"ecnonprofit\":{\"value\":\"false\"},\"ecenterprise\":{\"value\":\"false\"},\"ecnutscode\":{\"value\":\"false\"},\"ecinternationalorganizationeurinterests\":{\"value\":\"false\"},\"legalname\":{\"value\":\"Sociedade Brasileira de Reumatologia\"},\"country\":{\"classid\":\"BR\",\"classname\":\"Brazil\",\"schemename\":\"dnet:countries\",\"schemeid\":\"dnet:countries\"},\"echighereducation\":{\"value\":\"false\"},\"ecsmevalidated\":{\"value\":\"false\"}}},\"dateofcollection\":\"2018-09-19\",\"type\":20,\"id\":\"20|doajarticles::0019ba7a22c5bc733c3206bde28ff568\"}";

        MapDocument mapDocument = PaceUtils.asMapDocument(config, jsonEntity);

        System.out.println("mapDocument = " + mapDocument);
    }

}