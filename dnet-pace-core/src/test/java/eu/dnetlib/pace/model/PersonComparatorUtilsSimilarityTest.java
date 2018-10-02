package eu.dnetlib.pace.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PersonComparatorUtilsSimilarityTest {

	@Test
	public void testSimilarity_0() {
		assertTrue(PersonComparatorUtils.areSimilar("Artini Michele", "Michele Artini"));
	}

	@Test
	public void testSimilarity_1() {
		assertTrue(PersonComparatorUtils.areSimilar("ARTINI Michele", "Artini, Michele"));
	}

	@Test
	public void testSimilarity_2() {
		assertTrue(PersonComparatorUtils.areSimilar("Artini, M.", "Artini Michele"));
	}

	@Test
	public void testSimilarity_3() {
		assertTrue(PersonComparatorUtils.areSimilar("Artini, M.G.", "Artini, Michele"));
	}

	@Test
	public void testSimilarity_4() {
		assertTrue(PersonComparatorUtils.areSimilar("Artini, M.", "Artini, M.G."));
	}

	@Test
	public void testSimilarity_5() {
		assertTrue(PersonComparatorUtils.areSimilar("Artini, M. (sig.)", "Artini, Michele"));
	}

	@Test
	public void testSimilarity_6() {
		assertFalse(PersonComparatorUtils.areSimilar("Artini, M.", "Artini, G."));
	}

	@Test
	public void testSimilarity_7() {
		assertFalse(PersonComparatorUtils.areSimilar("Artini, M.G.", "Artini, M.A."));
	}

	@Test
	public void testSimilarity_8() {
		assertFalse(PersonComparatorUtils.areSimilar("Artini, M.", "Artini, Giuseppe"));
	}

	@Test
	public void testSimilarity_9() {
		assertFalse(PersonComparatorUtils.areSimilar("Manghi, Paolo", "Artini, Michele"));
	}

	@Test
	public void testSimilarity_10() {
		assertTrue(PersonComparatorUtils.areSimilar("Artini, Michele", "Artini, Michele Giovanni"));
	}

	@Test
	public void testSimilarity_11() {
		assertFalse(PersonComparatorUtils.areSimilar("Artini, M.A.G.", "Artini, M.B.G."));
	}

	@Test
	public void testSimilarity_12() {
		assertFalse(PersonComparatorUtils.areSimilar("Artini Manghi, M.", "Artini, Michele"));
	}

	@Test
	public void testSimilarity_13() {
		assertTrue(PersonComparatorUtils.areSimilar("Artini Manghi, M.", "Artini Manghi Michele"));
	}

	@Test
	public void testSimilarity_14() {
		assertFalse(PersonComparatorUtils.areSimilar("Artini, Michele", "Michele, Artini"));
	}

	@Test
	public void testSimilarity_15() {
		assertTrue(PersonComparatorUtils.areSimilar("Artini, M.", "Michele ARTINI"));
	}
}