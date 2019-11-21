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
//        accumulators.forEach((name, acc) -> System.out.println(name + " -> " + acc.value()));

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
                .filter(rel -> !getOrganizationLegalname(docsMap.get(rel._1())).equals(getOrganizationLegalname(docsMap.get(rel._2()))))
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

        String JSONEntity1 = "{\"dateoftransformation\":\"2018-09-19\",\"originalId\":[\"doajarticles::Sociedade_Brasileira_de_Ciência_do_Solo\"],\"collectedfrom\":[{\"value\":\"DOAJ-Articles\",\"key\":\"10|driver______::bee53aa31dc2cbb538c10c2b65fa5824\"}],\"organization\":{\"metadata\":{\"eclegalbody\":{\"value\":\"false\"},\"eclegalperson\":{\"value\":\"false\"},\"ecinternationalorganization\":{\"value\":\"false\"},\"legalshortname\":{\"value\":\"Sociedade Brasileira de Ciência do Solo\"},\"ecresearchorganization\":{\"value\":\"false\"},\"ecnonprofit\":{\"value\":\"false\"},\"ecenterprise\":{\"value\":\"false\"},\"ecnutscode\":{\"value\":\"false\"},\"ecinternationalorganizationeurinterests\":{\"value\":\"false\"},\"legalname\":{\"value\":\"Sociedade Brasileira de Ciência do Solo\"},\"country\":{\"classid\":\"BR\",\"classname\":\"Brazil\",\"schemename\":\"dnet:countries\",\"schemeid\":\"dnet:countries\"},\"echighereducation\":{\"value\":\"false\"},\"ecsmevalidated\":{\"value\":\"false\"}}},\"dateofcollection\":\"2018-09-19\",\"type\":20,\"id\":\"20|doajarticles::699ed9ecc727c90e9321e43e495e03ee\"}";
        String JSONEntity2 = "{\"dateoftransformation\":\"2018-09-19\",\"originalId\":[\"doajarticles::Sociedade_Brasileira_de_Educação_Matemática\"],\"collectedfrom\":[{\"value\":\"DOAJ-Articles\",\"key\":\"10|driver______::bee53aa31dc2cbb538c10c2b65fa5824\"}],\"organization\":{\"metadata\":{\"eclegalbody\":{\"value\":\"false\"},\"eclegalperson\":{\"value\":\"false\"},\"ecinternationalorganization\":{\"value\":\"false\"},\"legalshortname\":{\"value\":\"Sociedade Brasileira de Educação Matemática\"},\"ecresearchorganization\":{\"value\":\"false\"},\"ecnonprofit\":{\"value\":\"false\"},\"ecenterprise\":{\"value\":\"false\"},\"ecnutscode\":{\"value\":\"false\"},\"ecinternationalorganizationeurinterests\":{\"value\":\"false\"},\"legalname\":{\"value\":\"Sociedade Brasileira de Educação Matemática\"},\"country\":{\"classid\":\"BR\",\"classname\":\"Brazil\",\"schemename\":\"dnet:countries\",\"schemeid\":\"dnet:countries\"},\"echighereducation\":{\"value\":\"false\"},\"ecsmevalidated\":{\"value\":\"false\"}}},\"dateofcollection\":\"2018-09-19\",\"type\":20,\"id\":\"20|doajarticles::ec10c30a33588ad4884e042a4ea76a4a\"}";

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