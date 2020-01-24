package eu.dnetlib.pace;

import eu.dnetlib.Deduper;
import eu.dnetlib.pace.config.DedupConfig;
import eu.dnetlib.pace.model.Field;
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

        final URL dataset = getClass().getResource("/eu/dnetlib/pace/examples/organizations.custom.dump.json");
        entities = context.textFile(dataset.getPath());

    }

    @Ignore
    @Test
    public void dedupTest(){

        JavaRDD<ConnectedComponent> ccs = Deduper.dedup(context, entities, config);

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
                    String c1 = docsMap.get(rel._1()).getFieldMap().get("country").stringValue();
                    String c2 = docsMap.get(rel._2()).getFieldMap().get("country").stringValue();
                    return !c1.isEmpty() && !c2.isEmpty();
                })
                .filter(rel -> {
                    String w1 = docsMap.get(rel._1()).getFieldMap().get("websiteurl").stringValue();
                    String w2 = docsMap.get(rel._2()).getFieldMap().get("websiteurl").stringValue();
                    return (!w1.isEmpty() && !w2.isEmpty()) && w1.equals(w2);
                })
                .collect();

//        List<Tuple2<String, String>> rels = relationRDD
//                .filter(rel -> {
//                    String legalname1 = getOrganizationLegalname(docsMap.get(rel._1()))
//                            .replaceAll("&ndash;", " ")
//                            .replaceAll("&amp;", " ")
//                            .replaceAll("&quot;", " ")
//                            .replaceAll("&minus;", " ")
//                            .replaceAll("([0-9]+)", " $1 ")
//                            .replaceAll("[^\\p{ASCII}]", "")
//                            .replaceAll("[\\p{Punct}]", " ")
//                            .replaceAll("\\n", " ")
//                            .replaceAll("(?m)\\s+", " ")
//                            .toLowerCase()
//                            .trim();
//                    String legalname2 = getOrganizationLegalname(docsMap.get(rel._2()))
//                            .replaceAll("&ndash;", " ")
//                            .replaceAll("&amp;", " ")
//                            .replaceAll("&quot;", " ")
//                            .replaceAll("&minus;", " ")
//                            .replaceAll("([0-9]+)", " $1 ")
//                            .replaceAll("[^\\p{ASCII}]", "")
//                            .replaceAll("[\\p{Punct}]", " ")
//                            .replaceAll("\\n", " ")
//                            .replaceAll("(?m)\\s+", " ")
//                            .toLowerCase()
//                            .trim();
//                    return !legalname1.equals(legalname2);
//                })
//                .collect();

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

        String JSONEntity1 = "{\"eclegalbody\": {\"dataInfo\": {\"invisible\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemeid\": \"\", \"schemename\": \"\"}, \"inferenceprovenance\": \"\", \"inferred\": false, \"deletedbyinference\": false}, \"value\": \"\"}, \"ecresearchorganization\": {\"dataInfo\": {\"invisible\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemeid\": \"\", \"schemename\": \"\"}, \"inferenceprovenance\": \"\", \"inferred\": false, \"deletedbyinference\": false}, \"value\": \"\"}, \"logourl\": {\"dataInfo\": {\"invisible\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemeid\": \"\", \"schemename\": \"\"}, \"inferenceprovenance\": \"\", \"inferred\": false, \"deletedbyinference\": false}, \"value\": \"\"}, \"pid\": [{\"dataInfo\": {\"invisible\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemeid\": \"\", \"schemename\": \"\"}, \"inferenceprovenance\": \"\", \"inferred\": false, \"deletedbyinference\": false}, \"qualifier\": {\"classid\": \"grid\", \"classname\": \"grid\", \"schemeid\": \"dnet:pid_types\", \"schemename\": \"dnet:pid_types\"}, \"value\": \"grid.443291.9\"}], \"websiteurl\": {\"dataInfo\": {\"invisible\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemeid\": \"\", \"schemename\": \"\"}, \"inferenceprovenance\": \"\", \"inferred\": false, \"deletedbyinference\": false}, \"value\": \"\"}, \"ecnutscode\": {\"dataInfo\": {\"invisible\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemeid\": \"\", \"schemename\": \"\"}, \"inferenceprovenance\": \"\", \"inferred\": false, \"deletedbyinference\": false}, \"value\": \"\"}, \"legalname\": {\"dataInfo\": {\"invisible\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemeid\": \"\", \"schemename\": \"\"}, \"inferenceprovenance\": \"\", \"inferred\": false, \"deletedbyinference\": false}, \"value\": \"Universitas Hindu Indonesia\"}, \"collectedfrom\": [{\"dataInfo\": {\"invisible\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemeid\": \"\", \"schemename\": \"\"}, \"inferenceprovenance\": \"\", \"inferred\": false, \"deletedbyinference\": false}, \"key\": \"10|openaire____::ff4a008470319a22d9cf3d14af485977\", \"value\": \"GRID - Global Research Identifier Database\"}], \"dataInfo\": {\"invisible\": false, \"provenanceaction\": {\"classid\": \"UNKNOWN\", \"classname\": \"UNKNOWN\", \"schemeid\": \"dnet:provenanceActions\", \"schemename\": \"dnet:provenanceActions\"}, \"inferenceprovenance\": \"dedup-similarity-organization-simple\", \"inferred\": true, \"deletedbyinference\": true}, \"alternativeNames\": [], \"echighereducation\": {\"dataInfo\": {\"invisible\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemeid\": \"\", \"schemename\": \"\"}, \"inferenceprovenance\": \"\", \"inferred\": false, \"deletedbyinference\": false}, \"value\": \"\"}, \"id\": \"20|grid________::7746c66da537090d6f5bc3997471e6f8\", \"eclegalperson\": {\"dataInfo\": {\"invisible\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemeid\": \"\", \"schemename\": \"\"}, \"inferenceprovenance\": \"\", \"inferred\": false, \"deletedbyinference\": false}, \"value\": \"\"}, \"lastupdatetimestamp\": 1566902409376, \"ecinternationalorganizationeurinterests\": {\"dataInfo\": {\"invisible\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemeid\": \"\", \"schemename\": \"\"}, \"inferenceprovenance\": \"\", \"inferred\": false, \"deletedbyinference\": false}, \"value\": \"\"}, \"ecnonprofit\": {\"dataInfo\": {\"invisible\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemeid\": \"\", \"schemename\": \"\"}, \"inferenceprovenance\": \"\", \"inferred\": false, \"deletedbyinference\": false}, \"value\": \"\"}, \"ecenterprise\": {\"dataInfo\": {\"invisible\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemeid\": \"\", \"schemename\": \"\"}, \"inferenceprovenance\": \"\", \"inferred\": false, \"deletedbyinference\": false}, \"value\": \"\"}, \"ecinternationalorganization\": {\"dataInfo\": {\"invisible\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemeid\": \"\", \"schemename\": \"\"}, \"inferenceprovenance\": \"\", \"inferred\": false, \"deletedbyinference\": false}, \"value\": \"\"}, \"legalshortname\": {\"dataInfo\": {\"invisible\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemeid\": \"\", \"schemename\": \"\"}, \"inferenceprovenance\": \"\", \"inferred\": false, \"deletedbyinference\": false}, \"value\": \"Universitas Hindu Indonesia\"}, \"country\": {\"classid\": \"ID\", \"classname\": \"Indonesia\", \"schemeid\": \"dnet:countries\", \"schemename\": \"dnet:countries\"}, \"extraInfo\": [], \"originalId\": [], \"ecsmevalidated\": {\"dataInfo\": {\"invisible\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemeid\": \"\", \"schemename\": \"\"}, \"inferenceprovenance\": \"\", \"inferred\": false, \"deletedbyinference\": false}, \"value\": \"\"}}";
        String JSONEntity2 = "{\"eclegalbody\": {\"dataInfo\": {\"invisible\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemeid\": \"\", \"schemename\": \"\"}, \"inferenceprovenance\": \"\", \"inferred\": false, \"deletedbyinference\": false}, \"value\": \"false\"}, \"ecresearchorganization\": {\"dataInfo\": {\"invisible\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemeid\": \"\", \"schemename\": \"\"}, \"inferenceprovenance\": \"\", \"inferred\": false, \"deletedbyinference\": false}, \"value\": \"false\"}, \"logourl\": {\"dataInfo\": {\"invisible\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemeid\": \"\", \"schemename\": \"\"}, \"inferenceprovenance\": \"\", \"inferred\": false, \"deletedbyinference\": false}, \"value\": \"\"}, \"pid\": [], \"websiteurl\": {\"dataInfo\": {\"invisible\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemeid\": \"\", \"schemename\": \"\"}, \"inferenceprovenance\": \"\", \"inferred\": false, \"deletedbyinference\": false}, \"value\": \"http://www.ui.ac.id/\"}, \"ecnutscode\": {\"dataInfo\": {\"invisible\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemeid\": \"\", \"schemename\": \"\"}, \"inferenceprovenance\": \"\", \"inferred\": false, \"deletedbyinference\": false}, \"value\": \"false\"}, \"legalname\": {\"dataInfo\": {\"invisible\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemeid\": \"\", \"schemename\": \"\"}, \"inferenceprovenance\": \"\", \"inferred\": false, \"deletedbyinference\": false}, \"value\": \"Universitas Indonesia\"}, \"collectedfrom\": [{\"dataInfo\": {\"invisible\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemeid\": \"\", \"schemename\": \"\"}, \"inferenceprovenance\": \"\", \"inferred\": false, \"deletedbyinference\": false}, \"key\": \"10|openaire____::47ce9e9f4fad46e732cff06419ecaabb\", \"value\": \"OpenDOAR\"}], \"dataInfo\": {\"invisible\": false, \"provenanceaction\": {\"classid\": \"sysimport:crosswalk:entityregistry\", \"classname\": \"sysimport:crosswalk:entityregistry\", \"schemeid\": \"dnet:provenance_actions\", \"schemename\": \"dnet:provenance_actions\"}, \"inferenceprovenance\": \"dedup-similarity-organization-simple\", \"inferred\": true, \"deletedbyinference\": true}, \"alternativeNames\": [], \"echighereducation\": {\"dataInfo\": {\"invisible\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemeid\": \"\", \"schemename\": \"\"}, \"inferenceprovenance\": \"\", \"inferred\": false, \"deletedbyinference\": false}, \"value\": \"false\"}, \"id\": \"20|opendoar____::8dcf6331a28c2aa6d41f58b06ce3c385\", \"eclegalperson\": {\"dataInfo\": {\"invisible\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemeid\": \"\", \"schemename\": \"\"}, \"inferenceprovenance\": \"\", \"inferred\": false, \"deletedbyinference\": false}, \"value\": \"false\"}, \"lastupdatetimestamp\": 0, \"ecinternationalorganizationeurinterests\": {\"dataInfo\": {\"invisible\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemeid\": \"\", \"schemename\": \"\"}, \"inferenceprovenance\": \"\", \"inferred\": false, \"deletedbyinference\": false}, \"value\": \"false\"}, \"ecnonprofit\": {\"dataInfo\": {\"invisible\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemeid\": \"\", \"schemename\": \"\"}, \"inferenceprovenance\": \"\", \"inferred\": false, \"deletedbyinference\": false}, \"value\": \"false\"}, \"ecenterprise\": {\"dataInfo\": {\"invisible\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemeid\": \"\", \"schemename\": \"\"}, \"inferenceprovenance\": \"\", \"inferred\": false, \"deletedbyinference\": false}, \"value\": \"false\"}, \"ecinternationalorganization\": {\"dataInfo\": {\"invisible\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemeid\": \"\", \"schemename\": \"\"}, \"inferenceprovenance\": \"\", \"inferred\": false, \"deletedbyinference\": false}, \"value\": \"false\"}, \"legalshortname\": {\"dataInfo\": {\"invisible\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemeid\": \"\", \"schemename\": \"\"}, \"inferenceprovenance\": \"\", \"inferred\": false, \"deletedbyinference\": false}, \"value\": \"\"}, \"country\": {\"classid\": \"ID\", \"classname\": \"Indonesia\", \"schemeid\": \"dnet:countries\", \"schemename\": \"dnet:countries\"}, \"extraInfo\": [], \"originalId\": [\"opendoar____::Universitas_Indonesia\"], \"ecsmevalidated\": {\"dataInfo\": {\"invisible\": false, \"provenanceaction\": {\"classid\": \"\", \"classname\": \"\", \"schemeid\": \"\", \"schemename\": \"\"}, \"inferenceprovenance\": \"\", \"inferred\": false, \"deletedbyinference\": false}, \"value\": \"false\"}}";

        MapDocument mapDoc1 = MapDocumentUtil.asMapDocumentWithJPath(config, JSONEntity1);
        MapDocument mapDoc2 = MapDocumentUtil.asMapDocumentWithJPath(config, JSONEntity2);

        TreeStats treeStats = treeProcessor.evaluateTree(mapDoc1, mapDoc2);

        System.out.println(treeStats);

    }
}