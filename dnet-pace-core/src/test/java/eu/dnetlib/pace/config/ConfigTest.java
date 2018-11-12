package eu.dnetlib.pace.config;

import eu.dnetlib.pace.AbstractPaceTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ConfigTest extends AbstractPaceTest {

	@Test
	public void dedupConfigSerializationTest() {
		String fromClasspath = readFromClasspath("result.pace.conf.json");
		System.out.println("fromClasspath = " + fromClasspath);

		final DedupConfig conf = DedupConfig.load(fromClasspath);

		assertNotNull(conf);


		String parsed = conf.toString();

		System.out.println("parsed = " + parsed);

		DedupConfig conf2 = DedupConfig.load(parsed);

		assertNotNull(conf2);

		System.out.println("conf2 = " + conf2);

		assertEquals(parsed, conf2.toString());
	}

	@Test
	public void dedupConfigTest() {

		DedupConfig load = DedupConfig.load(readFromClasspath("result.pace.conf.json"));

		System.out.println(load.toString());
	}

}
