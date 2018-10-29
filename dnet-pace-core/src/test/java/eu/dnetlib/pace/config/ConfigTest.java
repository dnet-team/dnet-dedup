package eu.dnetlib.pace.config;

import java.io.IOException;

import eu.dnetlib.pace.AbstractPaceTest;
import eu.dnetlib.pace.model.MapDocument;
import eu.dnetlib.pace.model.MapDocumentSerializer;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ConfigTest extends AbstractPaceTest {

	@Test
	public void dedupConfigSerializationTest() {
		final DedupConfig cfgFromClasspath = DedupConfig.load(readFromClasspath("result.pace.conf.json"));

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

		DedupConfig load = DedupConfig.load(readFromClasspath("result.pace.conf.json"));

		System.out.println(load.toString());
	}

}
