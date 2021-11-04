package eu.dnetlib.pace.comparators;

import eu.dnetlib.pace.AbstractPaceTest;
import eu.dnetlib.pace.clustering.NGramUtils;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldListImpl;
import eu.dnetlib.pace.model.FieldValueImpl;
import eu.dnetlib.pace.model.MapDocument;
import eu.dnetlib.pace.tree.*;
import eu.dnetlib.pace.config.DedupConfig;
import eu.dnetlib.pace.util.MapDocumentUtil;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import eu.dnetlib.pace.common.AbstractPaceFunctions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ComparatorTest extends AbstractPaceTest {

	private Map<String, String> params;
	private DedupConfig conf;

	@BeforeAll
	public void setup() {
		params = new HashMap<>();
		params.put("weight", "1.0");
		conf = DedupConfig.load(readFromClasspath("/eu/dnetlib/pace/config/organization.current.conf.json", ComparatorTest.class));

	}

	@Test
	public void testCleanForSorting() {
		NGramUtils utils = new NGramUtils();
		System.out.println(utils.cleanupForOrdering("University of Pisa"));
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
		assertEquals(0.0, cityMatch.distance("Franklin College", "Concordia College", conf));

		//particular cases
		assertEquals(1.0, cityMatch.distance("Free University of Bozen-Bolzano", "Università di Bolzano", conf));
		assertEquals(1.0, cityMatch.distance("Politechniki Warszawskiej (Warsaw University of Technology)", "Warsaw University of Technology", conf));
		assertEquals(-1.0, cityMatch.distance("Allen (United States)", "United States Military Academy", conf));
	}

	@Test
	public void keywordMatchTest(){
		params.put("threshold", "0.5");

		final KeywordMatch keywordMatch = new KeywordMatch(params);

		assertEquals(0.5, keywordMatch.distance("Biblioteca dell'Universita di Bologna", "Università di Bologna", conf));
		assertEquals(1.0, keywordMatch.distance("Universita degli studi di Pisa", "Universita di Pisa", conf));
		assertEquals(1.0, keywordMatch.distance("Polytechnic University of Turin", "POLITECNICO DI TORINO", conf));
		assertEquals(1.0, keywordMatch.distance("Istanbul Commerce University", "İstanbul Ticarət Universiteti", conf));
		assertEquals(1.0, keywordMatch.distance("Franklin College", "Concordia College", conf));
		assertEquals(0.5, keywordMatch.distance("University of Georgia", "Georgia State University", conf));
		assertEquals(0.5, keywordMatch.distance("University College London", "University of London", conf));
		assertEquals(0.5, keywordMatch.distance("Washington State University", "University of Washington", conf));
		assertEquals(-1.0, keywordMatch.distance("Allen (United States)", "United States Military Academy", conf));

	}

	@Test
	public void containsMatchTest(){

		params.put("string", "openorgs");
		params.put("bool", "XOR");
		params.put("caseSensitive", "false");

		final ContainsMatch containsMatch = new ContainsMatch(params);

		assertEquals(0.0, containsMatch.distance("openorgs", "openorgs", conf));
	}

	@Test
	public void numbersMatchTest(){
		final NumbersMatch numbersMatch = new NumbersMatch(params);

		assertEquals(0.0, numbersMatch.distance("University of Rennes 2", "Universita di Rennes 7", conf));
		assertEquals(1.0, numbersMatch.distance("Universit<C9><U3> de Rennes 2", "Universita di Rennes 2", conf));
	}

	@Test
	public void romansMatchTest(){

		final RomansMatch romansMatch = new RomansMatch(params);

		assertEquals(-1.0, romansMatch.distance("University of Paris X", "Universita di Parigi", conf));
		assertEquals(0.0, romansMatch.distance("University of Paris IX", "University of Paris X", conf));
		assertEquals(1.0, romansMatch.distance("University of Paris VII", "University of Paris VII", conf));
	}

	@Test
	public void jaroWinklerNormalizedNameTest() {

		final JaroWinklerNormalizedName jaroWinklerNormalizedName = new JaroWinklerNormalizedName(params);

        double result = jaroWinklerNormalizedName.distance("AT&T (United States)", "United States Military Academy", conf);
        System.out.println("result = " + result);

		result = jaroWinklerNormalizedName.distance("NOAA - Servicio Meteorol\\u00f3gico Nacional", "NOAA - NWS", conf);
		System.out.println("result = " + result);


	}

	@Test
	public void jaroWinklerTest() {

		final JaroWinkler jaroWinkler = new JaroWinkler(params);

		double result = jaroWinkler.distance("Sofia", "Sofìa", conf);
		System.out.println("result = " + result);

		result = jaroWinkler.distance("University of Victoria Dataverse", "University of Windsor Dataverse", conf);
		System.out.println("result = " + result);

		result = jaroWinkler.distance("Victoria Dataverse", "Windsor Dataverse", conf);
		System.out.println("result = " + result);

		final Levenstein levenstein = new Levenstein(params);

		result = levenstein.distance("Victoria", "Windsor", conf);
		System.out.println("result = " + result);

	}

	@Test
	public void levensteinTitleTest() {

		final LevensteinTitle levensteinTitle = new LevensteinTitle(params);
		double result = levensteinTitle.distance("JRC: Open Power Plants Database", "JRC Open Power Plants Database (JRC-PPDB-OPEN)", conf);

		System.out.println("result = " + result);
	}

	@Test
	public void instanceTypeMatchTest() {

		final InstanceTypeMatch instanceTypeMatch = new InstanceTypeMatch(params);

		Field a = createFieldList(Arrays.asList("Article", "Article", "Article"), "instanceType");
		Field b = createFieldList(Arrays.asList("Article", "Article", "Article"), "instanceType");
		double result = instanceTypeMatch.compare(a, b, conf);

		assertEquals(1.0, result);

		Field c = createFieldList(Arrays.asList("Conference object", "Conference object", "Conference object"), "instanceType");
		result = instanceTypeMatch.compare(c, b, conf);

		assertEquals(1.0, result);

		Field d = createFieldList(Arrays.asList("Master thesis", "Master thesis", "Master thesis"), "instanceType");
		Field e = createFieldList(Arrays.asList("Bachelor thesis", "Bachelor thesis", "Bachelor thesis"), "instanceType");
		result = instanceTypeMatch.compare(d, e, conf);

		assertEquals(1.0, result);

		Field g = createFieldList(Arrays.asList("Software Paper", "Software Paper"), "instanceType");
		result = instanceTypeMatch.compare(e, g, conf);

		assertEquals(0.0, result);
	}


}
