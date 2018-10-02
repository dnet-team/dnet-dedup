package eu.dnetlib.pace.distance;

import org.junit.Before;
import org.junit.Test;

import eu.dnetlib.pace.common.AbstractPaceFunctions;

public class DistanceAlgoTest extends AbstractPaceFunctions {

	private final static String TEST_STRING = "Toshiba NB550D: è un netbook su piattaforma AMD Fusion⁽¹²⁾.";

	@Before
	public void setup() {
		System.out.println("****************************************************************");
		System.out.println("Test String    : " + TEST_STRING);
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

}
