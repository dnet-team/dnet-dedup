package eu.dnetlib.pace.comparators;

import eu.dnetlib.pace.clustering.NGramUtils;
import eu.dnetlib.pace.tree.CityMatch;
import eu.dnetlib.pace.tree.JaroWinklerNormalizedName;
import eu.dnetlib.pace.config.DedupConfig;
import org.junit.Before;
import org.junit.Test;

import eu.dnetlib.pace.common.AbstractPaceFunctions;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class ComparatorTest extends AbstractPaceFunctions {

	private Map<String, Number> params;
	private DedupConfig conf;

	@Before
	public void setup() {
		params = new HashMap<>();
		params.put("weight", 1.0);
		conf = DedupConfig.load(readFromClasspath("/eu/dnetlib/pace/config/organization.current.conf", ComparatorTest.class));

	}

	@Test
	public void testCleanForSorting() {
		NGramUtils utils = new NGramUtils();
		System.out.println("utils = " + utils.cleanupForOrdering("University of Pisa"));
	}

	@Test
	public void testJaroWinklerNormalizedName() {
		final JaroWinklerNormalizedName jaroWinklerNormalizedName = new JaroWinklerNormalizedName(params);
		double result = jaroWinklerNormalizedName.distance("Free University of Bozen-Bolzano", "University of the Free State", conf);

		System.out.println("result = " + result);
		assertEquals(0.0, result);
	}

	@Test
	public void testJaroWinklerNormalizedName2() {

		final JaroWinklerNormalizedName jaroWinklerNormalizedName = new JaroWinklerNormalizedName(params);
		double result = jaroWinklerNormalizedName.distance("University of New York", "Università di New York", conf);

		assertEquals(1.0, result);
	}

	@Test
    public void testJaroWinklerNormalizedName3() {

        final JaroWinklerNormalizedName jaroWinklerNormalizedName = new JaroWinklerNormalizedName(params);
        double result = jaroWinklerNormalizedName.distance("Biblioteca dell'Universita di Bologna", "Università di Bologna", conf);

        System.out.println("result = " + result);
        assertEquals(0.0, result);
    }

    @Test
    public void testJaroWinklerNormalizedName4() {

        final JaroWinklerNormalizedName jaroWinklerNormalizedName = new JaroWinklerNormalizedName(params);
        double result = jaroWinklerNormalizedName.distance("Universita degli studi di Pisa", "Universita di Pisa", conf);

        System.out.println("result = " + result);
        assertEquals(1.0, result);
    }

    @Test
    public void testJaroWinklerNormalizedName5() {

        final JaroWinklerNormalizedName jaroWinklerNormalizedName = new JaroWinklerNormalizedName(params);
        double result = jaroWinklerNormalizedName.distance("RESEARCH PROMOTION FOUNDATION", "IDRYMA PROOTHISIS EREVNAS", conf);

        System.out.println("result = " + result);
        assertEquals(1.0, result);
    }

    @Test
    public void testJaroWinklerNormalizedName6() {

        final JaroWinklerNormalizedName jaroWinklerNormalizedName = new JaroWinklerNormalizedName(params);
        double result = jaroWinklerNormalizedName.distance("Fonds zur Förderung der wissenschaftlichen Forschung (Austrian Science Fund)", "Fonds zur Förderung der wissenschaftlichen Forschung", conf);

        System.out.println("result = " + result);
        assertTrue(result > 0.9);

    }

    @Test
	public void testJaroWinklerNormalizedName7() {

		final JaroWinklerNormalizedName jaroWinklerNormalizedName = new JaroWinklerNormalizedName(params);
		double result = jaroWinklerNormalizedName.distance("Polytechnic University of Turin", "POLITECNICO DI TORINO", conf);

		System.out.println("result = " + result);
		assertTrue(result > 0.9);
	}

	@Test
	public void testJaroWinklerNormalizedName8() {
		final JaroWinklerNormalizedName jaroWinklerNormalizedName = new JaroWinklerNormalizedName(params);

		double result = jaroWinklerNormalizedName.distance("Politechniki Warszawskiej (Warsaw University of Technology)", "Warsaw University of Technology", conf);

		System.out.println("result = " + result);
	}

	@Test
	public void testJaroWinklerNormalizedName9() {
		final JaroWinklerNormalizedName jaroWinklerNormalizedName = new JaroWinklerNormalizedName(params);

		double result = jaroWinklerNormalizedName.distance("Istanbul Commerce University", "İstanbul Ticarət Universiteti", conf);

		System.out.println("result = " + result);
	}

	@Test
	public void testJaroWinklerNormalizedName10(){

		final JaroWinklerNormalizedName jaroWinklerNormalizedName = new JaroWinklerNormalizedName(params);

		double result = jaroWinklerNormalizedName.distance("Firenze University Press", "University of Florence", conf);

		System.out.println("result = " + result);
	}

	@Test
	public void cityMatchTest() {
		final CityMatch cityMatch = new CityMatch(params);

		//both names with no cities
		assertEquals(1.0, cityMatch.distance("Università", "Centro di ricerca", conf));

		//one of the two names with no cities
		assertEquals(-1.0, cityMatch.distance("Università di Bologna", "Centro di ricerca", conf));

		//both names with cities (same)
		assertEquals(1.0, cityMatch.distance("Universita di Bologna", "Biblioteca di Bologna", conf));

		//both names with cities (different)
		assertEquals(0.0, cityMatch.distance("Universita di Bologna", "Universita di Torino", conf));

	}


}
