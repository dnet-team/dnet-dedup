package eu.dnetlib.pace.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class PersonComparatorUtilsNGramsTest {

	@Test
	public void testNormaizePerson_1() {
		verifyGetNgramsForPerson("Artini Michele", 2, "a_michele", "m_artini");
	}

	@Test
	public void testNormaizePerson_2() {
		verifyGetNgramsForPerson("Michele Artini", 2, "a_michele", "m_artini");
	}

	@Test
	public void testNormaizePerson_3() {
		verifyGetNgramsForPerson("Michele ARTINI", 1, "m_artini");
	}

	@Test
	public void testNormaizePerson_4() {
		verifyGetNgramsForPerson("ARTINI Michele", 1, "m_artini");
	}

	@Test
	public void testNormaizePerson_5() {
		verifyGetNgramsForPerson("Michele G. Artini", 2, "m_artini", "g_artini");
	}

	@Test
	public void testNormaizePerson_6() {
		verifyGetNgramsForPerson(" Artini, Michele ", 1, "m_artini");
	}

	@Test
	public void testNormaizePerson_7() {
		verifyGetNgramsForPerson("Artini, Michele (sig.)", 1, "m_artini");
	}

	@Test
	public void testNormaizePerson_8() {
		verifyGetNgramsForPerson("Artini Michele [sig.] ", 2, "a_michele", "m_artini");
	}

	@Test
	public void testNormaizePerson_9() {
		verifyGetNgramsForPerson("Artini, M", 1, "m_artini");
	}

	@Test
	public void testNormaizePerson_10() {
		verifyGetNgramsForPerson("Artini, M.", 1, "m_artini");
	}

	@Test
	public void testNormaizePerson_11() {
		verifyGetNgramsForPerson("Artini, M. (sig.)", 1, "m_artini");
	}

	@Test
	public void testNormaizePerson_12() {
		verifyGetNgramsForPerson("Artini, M[sig.] ", 1, "m_artini");
	}

	@Test
	public void testNormaizePerson_13() {
		verifyGetNgramsForPerson("Artini-SIG, Michele ", 1, "m_artini-sig");
	}

	@Test
	public void testNormaizePerson_14() {
		verifyGetNgramsForPerson("Artini - SIG, Michele ", 1, "m_artini-sig");
	}

	@Test
	public void testNormaizePerson_15() {
		verifyGetNgramsForPerson("Artini {sig.}, M", 1, "m_artini");
	}

	@Test
	public void testNormaizePerson_16() {
		verifyGetNgramsForPerson("Artini, M., sig.", 1, "m_artini");
	}

	@Test
	public void testNormaizePerson_17() {
		verifyGetNgramsForPerson("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA, BBBBBBBBBBBBBBBBBBBBBBBBBBBBB CCCCCCCCCCCCCCCCCCCC", 0);
	}

	@Test
	public void testNormaizePerson_18() {
		verifyGetNgramsForPerson("Dell'amico, Andrea", 1, "a_amico");
	}

	@Test
	public void testNormaizePerson_19() {
		verifyGetNgramsForPerson("Smith, Paul van der", 1, "p_smith");
	}

	@Test
	public void testNormaizePerson_20() {
		verifyGetNgramsForPerson("AAAAAAA, BBBB, CCCC, DDDD, EEEE", 1, "b_aaaaaaa");
	}

	@Test
	public void testNormaizePerson_21() {
		verifyGetNgramsForPerson("Kompetenzzentrum Informelle Bildung (KIB),", 6);
	}

	private void verifyGetNgramsForPerson(String name, int expectedSize, String... expectedTokens) {
		Set<String> list = PersonComparatorUtils.getNgramsForPerson(name);
		System.out.println(list);
		assertEquals(expectedSize, list.size());
		for (String s : expectedTokens) {
			assertTrue(list.contains(s));
		}
	}

}
