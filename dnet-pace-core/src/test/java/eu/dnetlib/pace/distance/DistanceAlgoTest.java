package eu.dnetlib.pace.distance;

import eu.dnetlib.pace.distance.algo.JaroWinklerNormalizedName;
import org.junit.Before;
import org.junit.Test;

import eu.dnetlib.pace.common.AbstractPaceFunctions;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

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
		double result = jaroWinklerNormalizedName.distance("Universita di Pisa", "Universita di Parma");

		assertEquals(result, 0.0);
	}

	@Test
	public void testJaroWinklerNormalizedName2() {

		final JaroWinklerNormalizedName jaroWinklerNormalizedName = new JaroWinklerNormalizedName(params);
		double result = jaroWinklerNormalizedName.distance("University of New York", "Università di New York");

		assertEquals(result, 1.0);
	}


}
