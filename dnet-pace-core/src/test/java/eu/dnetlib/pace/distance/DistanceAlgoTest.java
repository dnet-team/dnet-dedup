package eu.dnetlib.pace.distance;

import eu.dnetlib.pace.clustering.NGramUtils;
import eu.dnetlib.pace.distance.algo.JaroWinklerNormalizedName;
import org.junit.Before;
import org.junit.Test;

import eu.dnetlib.pace.common.AbstractPaceFunctions;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class DistanceAlgoTest extends AbstractPaceFunctions {

	private final static String TEST_STRING = "Toshiba NB550D: è un netbook su piattaforma AMD Fusion⁽¹²⁾.";
	private Map<String, Number> params;

	@Before
	public void setup() {
		System.out.println("****************************************************************");
		System.out.println("Test String    : " + TEST_STRING);
		params = new HashMap<>();
		params.put("weight", 1.0);
	}

	@Test
	public void testCleanForSorting() {
		NGramUtils utils = new NGramUtils();
		System.out.println("utils = " + utils.cleanupForOrdering("University of Pisa"));
	}

	@Test
	public void testGetNumbers() {
		System.out.println("Numbers        : " + getNumbers(TEST_STRING));
	}

	@Test
	public void testRemoveSymbols() {
		System.out.println("Without symbols: " + removeSymbols(TEST_STRING));
	}

	@Test
	public void testFixAliases() {
		System.out.println("Fixed aliases  : " + fixAliases(TEST_STRING));
	}

	@Test
	public void testCleanup() {
		System.out.println("cleaned up     : " + cleanup(TEST_STRING));
	}

	@Test
	public void testJaroWinklerNormalizedName() {
		final JaroWinklerNormalizedName jaroWinklerNormalizedName = new JaroWinklerNormalizedName(params);
		double result = jaroWinklerNormalizedName.distance("Free University of Bozen-Bolzano", "University of the Free State");

		System.out.println("result = " + result);
		assertEquals(0.0, result);
	}

	@Test
	public void testJaroWinklerNormalizedName2() {

		final JaroWinklerNormalizedName jaroWinklerNormalizedName = new JaroWinklerNormalizedName(params);
		double result = jaroWinklerNormalizedName.distance("University of New York", "Università di New York");

		assertEquals(result, 1.0);
	}

	@Test
    public void testJaroWinklerNormalizedName3() {

        final JaroWinklerNormalizedName jaroWinklerNormalizedName = new JaroWinklerNormalizedName(params);
        double result = jaroWinklerNormalizedName.distance("Biblioteca dell'Universita di Bologna", "Università di Bologna");

        System.out.println("result = " + result);
        assertEquals(result, 0.0);
    }

    @Test
    public void testJaroWinklerNormalizedName4() {

        final JaroWinklerNormalizedName jaroWinklerNormalizedName = new JaroWinklerNormalizedName(params);
        double result = jaroWinklerNormalizedName.distance("Universita degli studi di Pisa", "Universita di Pisa");

        System.out.println("result = " + result);
        assertEquals(result, 1.0);
    }

    @Test
    public void testJaroWinklerNormalizedName5() {

        final JaroWinklerNormalizedName jaroWinklerNormalizedName = new JaroWinklerNormalizedName(params);
        double result = jaroWinklerNormalizedName.distance("RESEARCH PROMOTION FOUNDATION", "IDRYMA PROOTHISIS EREVNAS");

        System.out.println("result = " + result);
        assertEquals(result, 1.0);
    }

    @Test
    public void testJaroWinklerNormalizedName6() {

        final JaroWinklerNormalizedName jaroWinklerNormalizedName = new JaroWinklerNormalizedName(params);
        double result = jaroWinklerNormalizedName.distance("Fonds zur Förderung der wissenschaftlichen Forschung (Austrian Science Fund)", "Fonds zur Förderung der wissenschaftlichen Forschung");

        System.out.println("result = " + result);
        assertTrue(result> 0.9);

    }

    @Test
	public void testJaroWinklerNormalizedName7() {

		final JaroWinklerNormalizedName jaroWinklerNormalizedName = new JaroWinklerNormalizedName(params);
		double result = jaroWinklerNormalizedName.distance("Polytechnic University of Turin", "POLITECNICO DI TORINO");

		System.out.println("result = " + result);
		assertTrue(result> 0.9);
	}

	@Test
	public void testJaroWinklerNormalizedName8() {
		final JaroWinklerNormalizedName jaroWinklerNormalizedName = new JaroWinklerNormalizedName(params);

		double result = jaroWinklerNormalizedName.distance("Politechniki Warszawskiej (Warsaw University of Technology)", "Warsaw University of Technology");

		System.out.println("result = " + result);
	}

	@Test
	public void testJaroWinklerNormalizedName9() {
		final JaroWinklerNormalizedName jaroWinklerNormalizedName = new JaroWinklerNormalizedName(params);

		double result = jaroWinklerNormalizedName.distance("Istanbul Commerce University", "İstanbul Ticarət Universiteti");

		System.out.println("result = " + result);
	}

	@Test
	public void testJaroWinklerNormalizedName10(){

		final JaroWinklerNormalizedName jaroWinklerNormalizedName = new JaroWinklerNormalizedName(params);

		double result = jaroWinklerNormalizedName.distance("Firenze University Press", "University of Florence");

		System.out.println("result = " + result);
	}
}
