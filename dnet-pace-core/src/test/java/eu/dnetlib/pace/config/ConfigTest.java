package eu.dnetlib.pace.config;

import java.io.IOException;

import eu.dnetlib.pace.AbstractPaceTest;
import eu.dnetlib.pace.model.MapDocument;
import eu.dnetlib.pace.model.MapDocumentSerializer;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class ConfigTest extends AbstractPaceTest {

	@Test
	public void test() throws IOException {
		final DedupConfig cfg = DedupConfig.load(readFromClasspath("result.pace.conf.json"));

		assertNotNull(cfg);

		System.out.println(cfg.toString());
	}

}
