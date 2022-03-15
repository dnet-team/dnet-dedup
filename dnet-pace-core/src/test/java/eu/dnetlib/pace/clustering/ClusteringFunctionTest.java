package eu.dnetlib.pace.clustering;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import eu.dnetlib.pace.AbstractPaceTest;
import eu.dnetlib.pace.common.AbstractPaceFunctions;
import eu.dnetlib.pace.config.DedupConfig;
import org.junit.jupiter.api.*;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ClusteringFunctionTest extends AbstractPaceTest {

	private static Map<String, Integer> params;
	private static DedupConfig conf;

	@BeforeAll
	public static void setUp() throws Exception {
		params = Maps.newHashMap();
        conf = DedupConfig.load(AbstractPaceFunctions.readFromClasspath("/eu/dnetlib/pace/config/organization.current.conf.json", ClusteringFunctionTest.class));
	}

	@Test
	public void testUrlClustering() {

		final ClusteringFunction urlClustering = new UrlClustering(params);

		final String s = "http://www.test.it/path/to/resource";
		System.out.println(s);
		System.out.println(urlClustering.apply(conf, Lists.newArrayList(url(s))));
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
		System.out.println(ngram.apply(conf, Lists.newArrayList(title(s))));
	}

	@Test
	public void testNgramPairs() {
		params.put("ngramLen", 3);
		params.put("max", 2);

		final ClusteringFunction np = new NgramPairs(params);

		final String s = "Search for the Standard Model Higgs Boson";
		System.out.println(s);
		System.out.println(np.apply(conf, Lists.newArrayList(title(s))));
	}

	@Test
	public void testSortedNgramPairs() {
		params.put("ngramLen", 3);
		params.put("max", 2);

		final ClusteringFunction np = new SortedNgramPairs(params);

		final String s1 = "University of Pisa";
		System.out.println(s1);
		System.out.println(np.apply(conf, Lists.newArrayList(title(s1))));

		final String s2 = "Pisa University";
		System.out.println(s2);
		System.out.println(np.apply(conf, Lists.newArrayList(title(s2))));

		final String s3 = "Parco Tecnologico Agroalimentare Umbria";
		System.out.println(s3);
		System.out.println(np.apply(conf, Lists.newArrayList(title(s3))));

	}

	@Test
	public void testAcronym() {
		params.put("max", 4);
		params.put("minLen", 1);
		params.put("maxLen", 3);

		final ClusteringFunction acro = new Acronyms(params);

		final String s = "Search for the Standard Model Higgs Boson";
		System.out.println(s);
		System.out.println(acro.apply(conf, Lists.newArrayList(title(s))));
	}

	@Test
	public void testSuffixPrefix() {
		params.put("len", 3);
		params.put("max", 4);

		final ClusteringFunction sp = new SuffixPrefix(params);

		final String s = "Search for the Standard Model Higgs Boson";
		System.out.println(s);
		System.out.println(sp.apply(conf, Lists.newArrayList(title(s))));
	}

	@Test
	public void testWordsSuffixPrefix() {

		params.put("len", 3);
		params.put("max", 4);

		final ClusteringFunction sp = new WordsSuffixPrefix(params);

		final String s = "Search for the Standard Model Higgs Boson";
		System.out.println(s);
		System.out.println(sp.apply(conf, Lists.newArrayList(title(s))));
	}

	@Test
	public void testWordsStatsSuffixPrefix() {
		params.put("mod", 10);

		final ClusteringFunction sp = new WordsStatsSuffixPrefixChain(params);

		String s = "Search for the Standard Model Higgs Boson";
		System.out.println(s);
		System.out.println(sp.apply(conf, Lists.newArrayList(title(s))));

		s = "A Physical Education Teacher Is Like...: Examining Turkish Students  Perceptions of Physical Education Teachers Through Metaphor Analysis";
		System.out.println(s);
		System.out.println(sp.apply(conf, Lists.newArrayList(title(s))));

		s = "Structure of a Eukaryotic Nonribosomal Peptide Synthetase Adenylation Domain That Activates a Large Hydroxamate Amino Acid in Siderophore Biosynthesis";
		System.out.println(s);
		System.out.println(sp.apply(conf, Lists.newArrayList(title(s))));

		s = "Performance Evaluation";
		System.out.println(s);
		System.out.println(sp.apply(conf, Lists.newArrayList(title(s))));

		s = "JRC Open Power Plants Database (JRC-PPDB-OPEN)";
		System.out.println(s);
		System.out.println(sp.apply(conf, Lists.newArrayList(title(s))));

		s = "JRC Open Power Plants Database";
		System.out.println(s);
		System.out.println(sp.apply(conf, Lists.newArrayList(title(s))));

	}

	@Test
	public void testFieldValue() {

		params.put("randomLength", 5);

		final ClusteringFunction sp = new SpaceTrimmingFieldValue(params);

		final String s = "Search for the Standard Model Higgs Boson";
		System.out.println(s);
		System.out.println(sp.apply(conf, Lists.newArrayList(title(s))));
	}

	@Test
	public void testKeywordsClustering() {

		final ClusteringFunction cf = new KeywordsClustering(params);
		final String s = "Polytechnic University of Turin";
		System.out.println(s);
		System.out.println(cf.apply(conf, Lists.newArrayList(title(s))));

		final String s1 = "POLITECNICO DI TORINO";
		System.out.println(s1);
		System.out.println(cf.apply(conf, Lists.newArrayList(title(s1))));

		final String s2 = "Universita farmaceutica culturale di milano bergamo";
		System.out.println("s2 = " + s2);
		System.out.println(cf.apply(conf, Lists.newArrayList(title(s2))));

		final String s3 = "universita universita milano milano";
		System.out.println("s3 = " + s3);
		System.out.println(cf.apply(conf, Lists.newArrayList(title(s3))));

		final String s4 = "Politechniki Warszawskiej (Warsaw University of Technology)";
		System.out.println("s4 = " + s4);
		System.out.println(cf.apply(conf, Lists.newArrayList(title(s4))));

		final String s5 = "İstanbul Ticarət Universiteti";
		System.out.println("s5 = " + s5);
		System.out.println(cf.apply(conf, Lists.newArrayList(title(s5))));

		final String s6 = "National and Kapodistrian University of Athens";
		System.out.println("s6 = " + s6);
		System.out.println(cf.apply(conf, Lists.newArrayList(title(s6))));

		final String s7 = "Εθνικό και Καποδιστριακό Πανεπιστήμιο Αθηνών";
		System.out.println("s7 = " + s7);
		System.out.println(cf.apply(conf, Lists.newArrayList(title(s7))));

	}

}
