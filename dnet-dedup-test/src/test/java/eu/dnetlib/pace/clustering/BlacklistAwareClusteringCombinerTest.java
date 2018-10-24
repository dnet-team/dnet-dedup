//package eu.dnetlib.pace.clustering;
//
//import eu.dnetlib.pace.AbstractProtoPaceTest;
//import eu.dnetlib.pace.clustering.BlacklistAwareClusteringCombiner;
//import eu.dnetlib.pace.config.Config;
//import eu.dnetlib.pace.config.Type;
//import eu.dnetlib.pace.model.FieldListImpl;
//import eu.dnetlib.pace.model.FieldValueImpl;
//import eu.dnetlib.pace.model.MapDocument;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.junit.Before;
//import org.junit.Test;
//
//public class BlacklistAwareClusteringCombinerTest extends AbstractProtoPaceTest {
//
//	private static final Log log = LogFactory.getLog(BlacklistAwareClusteringCombinerTest.class);
//
//	private Config config;
//
//	@Before
//	public void setUp() {
//		config = getResultFullConf();
//	}
//
//	@Test
//	public void testCombine() {
//		final MapDocument result =
//				result(config, "A", "Dipping in Cygnus X-2 in a multi-wavelength campaign due to absorption of extended ADC emission", "2013");
//		final FieldListImpl fl = new FieldListImpl();
//		fl.add(new FieldValueImpl(Type.String, "desc", "hello world description pipeline"));
//
//		result.getFieldMap().put("desc", fl);
//
//		fl.clear();
//		fl.add(new FieldValueImpl(Type.String, "title", "lorem ipsum cabalie qwerty"));
//		final FieldListImpl field = (FieldListImpl) result.getFieldMap().get("title");
//		field.add(fl);
//
//		log.info(BlacklistAwareClusteringCombiner.filterAndCombine(result, config));
//	}
//}
