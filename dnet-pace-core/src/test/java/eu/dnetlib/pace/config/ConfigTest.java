package eu.dnetlib.pace.config;


import eu.dnetlib.pace.AbstractPaceTest;
import eu.dnetlib.pace.clustering.BlacklistAwareClusteringCombiner;
import eu.dnetlib.pace.clustering.ClusteringClass;
import eu.dnetlib.pace.clustering.ClusteringCombiner;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldList;
import eu.dnetlib.pace.model.MapDocument;
import eu.dnetlib.pace.tree.JsonListMatch;
import eu.dnetlib.pace.tree.support.AggType;
import eu.dnetlib.pace.tree.support.FieldConf;
import eu.dnetlib.pace.tree.support.TreeNodeDef;
import eu.dnetlib.pace.tree.support.TreeNodeStats;
import eu.dnetlib.pace.util.MapDocumentUtil;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class ConfigTest extends AbstractPaceTest {

	private static Map<String, String> params;

	@BeforeAll
	public static void setup() {
		params = new HashMap<>();
		params.put("jpath_value", "$.value");
		params.put("jpath_classid", "$.qualifier.classid");

	}

	@Test
	public void dedupConfigSerializationTest() {
		final DedupConfig cfgFromClasspath = DedupConfig.load(readFromClasspath("organization.current.conf.json"));

		final String conf = cfgFromClasspath.toString();

		final DedupConfig cfgFromSerialization = DedupConfig.load(conf);

		assertEquals(cfgFromClasspath.toString(), cfgFromSerialization.toString());

		assertNotNull(cfgFromClasspath);
		assertNotNull(cfgFromSerialization);
	}

	@Test
	public void dedupConfigTest() {

		DedupConfig load = DedupConfig.load(readFromClasspath("organization.current.conf.json"));

		System.out.println(load.toString());
	}

	@Test
	public void initTranslationMapTest() {

		DedupConfig load = DedupConfig.load(readFromClasspath("organization.current.conf.json"));

		Map<String, String> translationMap = load.translationMap();

		System.out.println("translationMap = " + translationMap.size());

		for (String key: translationMap.keySet()) {
			if (translationMap.get(key).equals("key::1"))
				System.out.println("key = " + key);
		}
	}

	@Test
	public void emptyTranslationMapTest() {

		DedupConfig load = DedupConfig.load(readFromClasspath("organization.no_synonyms.conf.json"));

		assertEquals(0, load.getPace().translationMap().keySet().size());
	}

	@Test
	public void asMapDocumentTest() {

		DedupConfig dedupConf = DedupConfig.load(readFromClasspath("publication.current.conf.json"));

		final String json = readFromClasspath("publication.json");

		final MapDocument mapDocument = MapDocumentUtil.asMapDocumentWithJPath(dedupConf, json);

//		System.out.println("mapDocument = " + mapDocument.getFieldMap());

//		JsonListMatch jsonListMatch = new JsonListMatch(params);
//
//		jsonListMatch.compare(mapDocument.getFieldMap().get("pid"), mapDocument.getFieldMap().get("pid"), null);

		System.out.println("mapDocument = " + mapDocument.getFieldMap().get("title").stringValue());


    }

    @Test
    public  void testJPath()  {
        final String json = readFromClasspath("organization.json");

        final String jpath ="$.id";

        System.out.println("result = " + MapDocumentUtil.getJPathString(jpath, json));
    }

    @Test
	public void clusteringCombinerTest() {

		DedupConfig dedupConf = DedupConfig.load(readFromClasspath("publication.current.conf.json"));

		final String json = readFromClasspath("publication.json");

		final MapDocument mapDocument = MapDocumentUtil.asMapDocumentWithJPath(dedupConf, json);

		String[] combine = ClusteringCombiner.combine(mapDocument, dedupConf).toArray(new String[3]);

		assertEquals("test", combine[0].split(":")[1]);
		assertEquals("title", combine[1].split(":")[1]);
		assertEquals("doi", combine[2].split(":")[1]);
	}

	@Test
	public void filterAndCombineTest() {

		DedupConfig dedupConf = DedupConfig.load(readFromClasspath("pub.prod.conf.json"));

		final String json = readFromClasspath("publication.example.json");

		final MapDocument mapDocument = MapDocumentUtil.asMapDocumentWithJPath(dedupConf, json);

		Collection<String> strings = BlacklistAwareClusteringCombiner.filterAndCombine(mapDocument, dedupConf);

		for (String s: strings) {
			System.out.println("s = " + s);
		}

	}

	@Test
	public void crossCompareTest() {

		DedupConfig dedupConf = DedupConfig.load(readFromClasspath("organization.cross.compare.conf.json"));

		TreeNodeDef treeNode = dedupConf.decisionTree().get("start");

		final String json = readFromClasspath("organization.json");

		final MapDocument doc = MapDocumentUtil.asMapDocumentWithJPath(dedupConf, json);

		TreeNodeStats nodeStats = treeNode.evaluate(doc, doc, dedupConf);

		assertTrue(nodeStats.getFinalScore(AggType.MAX)>0.7);

	}
}
