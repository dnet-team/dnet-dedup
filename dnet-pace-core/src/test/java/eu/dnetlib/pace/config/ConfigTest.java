package eu.dnetlib.pace.config;

import eu.dnetlib.pace.AbstractPaceTest;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ConfigTest extends AbstractPaceTest {

	@Test
	public void dedupConfigSerializationTest() {
		final DedupConfig cfgFromClasspath = DedupConfig.load(readFromClasspath("organization.current.conf"));

		final String conf = cfgFromClasspath.toString();

		final DedupConfig cfgFromSerialization = DedupConfig.load(conf);

		assertEquals(cfgFromClasspath.toString(), cfgFromSerialization.toString());

		assertNotNull(cfgFromClasspath);
		assertNotNull(cfgFromSerialization);

	}

	@Test
	public void dedupConfigTest() {

		DedupConfig load = DedupConfig.load(readFromClasspath("organization.current.conf"));

		System.out.println(load.toString());
	}

	@Test
	public void initTranslationMapTest() {

		DedupConfig load = DedupConfig.load(readFromClasspath("organization.current.conf"));

		Map<String, String> translationMap = load.translationMap();

		System.out.println("translationMap = " + translationMap.size());

		for (String key: translationMap.keySet()) {
			if (translationMap.get(key).equals("key::1"))
				System.out.println("key = " + key);
		}

	}

	@Test
	public void emptyTranslationMapTest() {

		DedupConfig load = DedupConfig.load(readFromClasspath("organization.no_synonyms.conf"));

		assertEquals(0, load.getPace().translationMap().keySet().size());
	}

}
