package eu.dnetlib.pace.clustering;

import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import eu.dnetlib.pace.AbstractPaceTest;
import eu.dnetlib.pace.common.AbstractPaceFunctions;
import eu.dnetlib.pace.model.Field;
import org.junit.Before;
import org.junit.Test;

public class ClusteringFunctionTest extends AbstractPaceTest {

	private Map<String, Integer> params;

	@Before
	public void setUp() throws Exception {
		params = Maps.newHashMap();
	}

	@Test
	public void testUrlClustering() {

		final ClusteringFunction urlClustering = new UrlClustering(params);

		final String s = "http://www.test.it/path/to/resource";
		System.out.println(s);
		System.out.println(urlClustering.apply(Lists.newArrayList(url(s))));
	}

	@Test
	public void testNgram() {
		params.put("ngramLen", 3);
		params.put("max", 8);
		params.put("maxPerToken", 2);
		params.put("minNgramLen", 1);

		final ClusteringFunction ngram = new Ngrams(params);

		final String s = "Search for the Standard Model Higgs Boson";
		System.out.println(s);
		System.out.println(ngram.apply(Lists.newArrayList(title(s))));
	}

	@Test
	public void testNgramPairs() {
		params.put("ngramLen", 3);
		params.put("max", 3);

		final ClusteringFunction np = new NgramPairs(params);

		final String s = "Search for the Standard Model Higgs Boson";
		System.out.println(s);
		System.out.println(np.apply(Lists.newArrayList(title(s))));
	}

	@Test
	public void testSortedNgramPairs() {
		params.put("ngramLen", 3);
		params.put("max", 1);

		final ClusteringFunction np = new SortedNgramPairs(params);

		final String s1 = "University of Pisa";
		System.out.println(s1);
		System.out.println(np.apply(Lists.newArrayList(title(s1))));

		final String s2 = "Pisa University";
		System.out.println(s2);
		System.out.println(np.apply(Lists.newArrayList(title(s2))));
	}

	@Test
	public void testAcronym() {
		params.put("max", 4);
		params.put("minLen", 1);
		params.put("maxLen", 3);

		final ClusteringFunction acro = new Acronyms(params);

		final String s = "Search for the Standard Model Higgs Boson";
		System.out.println(s);
		System.out.println(acro.apply(Lists.newArrayList(title(s))));
	}

	@Test
	public void testSuffixPrefix() {
		params.put("len", 3);
		params.put("max", 4);

		final ClusteringFunction sp = new SuffixPrefix(params);

		final String s = "Search for the Standard Model Higgs Boson";
		System.out.println(s);
		System.out.println(sp.apply(Lists.newArrayList(title(s))));
	}

	@Test
	public void testFieldValue() {

		params.put("randomLength", 5);

		final ClusteringFunction sp = new SpaceTrimmingFieldValue(params);

		final String s = "Search for the Standard Model Higgs Boson";
		System.out.println(s);
		System.out.println(sp.apply(Lists.newArrayList(title(s))));
	}

	@Test
	public void testPersonClustering2() {
		final ClusteringFunction cf = new PersonClustering(params);

		final String s = readFromClasspath("gt.author.json");
		System.out.println(s);
		System.out.println(cf.apply(Lists.newArrayList(person(s))));
	}

	@Test
	public void testKeywordsClustering() {

		final ClusteringFunction cf = new KeywordsClustering(params);
		final String s = "Polytechnic University of Turin";
		System.out.println(s);
		System.out.println(cf.apply(Lists.newArrayList(title(s))));

		final String s1 = "POLITECNICO DI TORINO";
		System.out.println(s1);
		System.out.println(cf.apply(Lists.newArrayList(title(s1))));

		final String s2 = "Universita farmaceutica culturale di milano bergamo";
		System.out.println("s2 = " + s2);
		System.out.println(cf.apply(Lists.newArrayList(title(s2))));

		final String s3 = "universita universita milano milano";
		System.out.println("s3 = " + s3);
		System.out.println(cf.apply(Lists.newArrayList(title(s3))));

		final String s4 = "Politechniki Warszawskiej (Warsaw University of Technology)";
		System.out.println("s4 = " + s4);
		System.out.println(cf.apply(Lists.newArrayList(title(s4))));

	}

}
