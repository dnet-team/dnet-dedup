package eu.dnetlib.pace.model;

import static org.junit.Assert.assertEquals;

import java.text.Normalizer;
import java.util.Queue;

import org.junit.Test;

import com.google.common.collect.Lists;

public class PersonTest {

	@Test
	public void test_1() {
		check("Atzori, Claudio", "Atzori, Claudio");
	}

	@Test
	public void test_2() {
		check("Atzori, Claudio A.", "Atzori, Claudio A.");
	}

	@Test
	public void test_3() {
		check("Claudio ATZORI", "Atzori, Claudio");
	}

	@Test
	public void test_4() {
		check("ATZORI, Claudio", "Atzori, Claudio");
	}

	@Test
	public void test_5() {
		check("Claudio Atzori", "Claudio Atzori");
	}

	@Test
	public void test_6() {
		check(" Manghi ,  Paolo", "Manghi, Paolo");
	}

	@Test
	public void test_7() {
		check("ATZORI, CLAUDIO", "Atzori, Claudio");
	}

	@Test
	public void test_8() {
		check("ATZORI, CLAUDIO A", "Atzori, Claudio A.");
	}

	@Test
	public void test_9() {
		check("Bølviken, B.", "Bølviken, B.");
	}

	@Test
	public void test_10() {
		check("Bñlviken, B.", "B" + Normalizer.normalize("ñ", Normalizer.Form.NFD) + "lviken, B.");
	}

	@Test
	public void test_11() {
		check("aáeéiíoóöőuúüű AÁEÉIÍOÓÖŐUÚÜŰ ø", "Aaeeiioooouuuu, Aaeeiioooouuuu Ø.", true);
	}

	@Test
	public void test_12() {
		check("aáeéiíoóöőuúüű AÁEÉIÍOÓÖŐUÚÜŰz ø", Normalizer.normalize("aáeéiíoóöőuúüű AÁEÉIÍOÓÖŐUÚÜŰz ø", Normalizer.Form.NFD), false);
	}

	@Test
	public void test_13() {
		check("Tkačíková, Daniela", Normalizer.normalize("Tkačíková, Daniela", Normalizer.Form.NFD), false);
	}

	@Test
	public void test_hashes() {
		checkHash(" Claudio  ATZORI ", "ATZORI Claudio", "Atzori , Claudio", "ATZORI, Claudio");
	}

	private void checkHash(String... ss) {
		Queue<String> q = Lists.newLinkedList(Lists.newArrayList(ss));
		String h1 = new Person(q.remove(), false).hash();
		while (!q.isEmpty()) {
			assertEquals(h1, new Person(q.remove(), false).hash());
		}
	}

	private void check(String s, String expectedFullName) {
		check(s, expectedFullName, false);
	}

	private void check(String s, String expectedFullName, boolean aggressive) {
		Person p = new Person(s, aggressive);

		System.out.println("original:   " + p.getOriginal());
		System.out.println("accurate:   " + p.isAccurate());
		System.out.println("normalised: '" + p.getNormalisedFullname() + "'");
		if (p.isAccurate()) {
			System.out.println("name:     " + p.getNormalisedFirstName());
			System.out.println("surname:  " + p.getNormalisedSurname());
		}
		System.out.println("hash: " + p.hash());
		System.out.println("");
		assertEquals(expectedFullName, p.getNormalisedFullname());
	}

}
