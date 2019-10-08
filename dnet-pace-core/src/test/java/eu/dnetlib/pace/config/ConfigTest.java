package eu.dnetlib.pace.config;

import eu.dnetlib.pace.AbstractPaceTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ConfigTest extends AbstractPaceTest {

	@Test
	public void dedupConfigSerializationTest() {
		final DedupConfig cfgFromClasspath = DedupConfig.load(readFromClasspath("org.curr.conf"));

		final String conf = cfgFromClasspath.toString();

//		System.out.println("*****SERIALIZED*****");
//		System.out.println(conf);
//		System.out.println("*****FROM CLASSPATH*****");
//		System.out.println(readFromClasspath("result.pace.conf.json"));

		final DedupConfig cfgFromSerialization = DedupConfig.load(conf);

		assertEquals(cfgFromClasspath.toString(), cfgFromSerialization.toString());

		assertNotNull(cfgFromClasspath);
		assertNotNull(cfgFromSerialization);


	}

	@Test
	public void dedupConfigTest() {

		DedupConfig load = DedupConfig.load(readFromClasspath("org.curr.conf"));

		System.out.println(load.toString());
	}

	@Test
	public void translationMapTest() {

		DedupConfig load = DedupConfig.load(readFromClasspath("org.curr.conf"));

		System.out.println("translationMap = " + load.getPace().translationMap().toString());
	}

}
