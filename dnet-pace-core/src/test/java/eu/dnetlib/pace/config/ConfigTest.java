package eu.dnetlib.pace.config;


import eu.dnetlib.pace.AbstractPaceTest;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldList;
import eu.dnetlib.pace.model.MapDocument;
import eu.dnetlib.pace.tree.JsonListMatch;
import eu.dnetlib.pace.util.MapDocumentUtil;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ConfigTest extends AbstractPaceTest {

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
	public void asMapDocumentTest() throws Exception {

		DedupConfig dedupConf = DedupConfig.load(readFromClasspath("publication.current.conf.json"));

		final String json = readFromClasspath("pub2.json");

		final MapDocument mapDocument = MapDocumentUtil.asMapDocumentWithJPath(dedupConf, json);

		System.out.println("mapDocument = " + mapDocument.getFieldMap());


		System.out.println(mapDocument.getFieldMap().values().stream().map(Field::isEmpty).count());

    }





    @Test
    public  void testJPath()  {
        final String json = readFromClasspath("pub2.json");

        final String jpath ="$.pid";


        final List<String> jPathList = MapDocumentUtil.getJPathList(jpath, json, Type.JSON);

        System.out.println("jPathList = " + jPathList);


    }
}
