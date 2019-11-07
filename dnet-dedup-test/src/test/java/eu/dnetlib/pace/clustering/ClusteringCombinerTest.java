package eu.dnetlib.pace.clustering;

import eu.dnetlib.pace.AbstractProtoPaceTest;
import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.config.Type;
import eu.dnetlib.pace.model.FieldListImpl;
import eu.dnetlib.pace.model.FieldValueImpl;
import eu.dnetlib.pace.model.MapDocument;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

public class ClusteringCombinerTest extends AbstractProtoPaceTest {

	private static final Log log = LogFactory.getLog(ClusteringCombinerTest.class);

	private Config config;

	@Before
	public void setUp() {
		config = getOrganizationTestConf();
	}

	@Test
	public void testCombine() {

		final MapDocument organization = organization(config, "A", "University of Turin", "UNITO");
		log.info("University of Turin");
		log.info(ClusteringCombiner.combine(organization, config));
	}

	@Test
	public void testCombineBlacklistAware() {

		final MapDocument organization = organization(config, "A", "University of Turin", "UNITO");
		log.info("University of Turin");
		log.info(BlacklistAwareClusteringCombiner.filterAndCombine(organization, config));
	}

}
