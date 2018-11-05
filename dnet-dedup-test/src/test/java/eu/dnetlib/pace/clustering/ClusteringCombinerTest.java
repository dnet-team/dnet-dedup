package eu.dnetlib.pace.clustering;

import eu.dnetlib.pace.AbstractProtoPaceTest;
import eu.dnetlib.pace.clustering.ClusteringCombiner;
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
		config = getResultFullConf();
	}

	@Test
	public void testCombine() {
		String title = "Dipping in Cygnus X-2 in a multi-wavelength campaign due to absorption of extended ADC emission";
		MapDocument result = result(config, "A", title, "2013");

		FieldListImpl fl = new FieldListImpl();
		fl.add(new FieldValueImpl(Type.String, "desc", "lorem ipsum cabalie qwerty"));

		result.getFieldMap().put("desc", fl);
		log.info(title);
		log.info(ClusteringCombiner.combine(result, config));
	}

}
