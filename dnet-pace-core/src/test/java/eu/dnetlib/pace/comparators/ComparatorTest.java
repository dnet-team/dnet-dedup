package eu.dnetlib.pace.comparators;

import eu.dnetlib.pace.AbstractPaceTest;
import eu.dnetlib.pace.clustering.NGramUtils;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.tree.*;
import eu.dnetlib.pace.config.DedupConfig;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
		params.put("surname_th", "0.99");
		params.put("name_th", "0.95");
		params.put("jpath_value", "$.value");
		params.put("jpath_classid", "$.qualifier.classid");
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
	public void listContainsMatchTest(){

		Field a = createFieldList(Arrays.asList("Article", "Publication", "ORP"), "instanceType");
		Field b = createFieldList(Arrays.asList("Publication", "Article", "ORP"), "instanceType");

		params.put("string", "Article");
		params.put("bool", "XOR");
		params.put("caseSensitive", "false");

		ListContainsMatch listContainsMatch = new ListContainsMatch(params);

		assertEquals(0.0, listContainsMatch.compare(a, b, conf));

		params.put("string", "Article");
		params.put("bool", "AND");
		params.put("caseSensitive", "false");

		listContainsMatch = new ListContainsMatch(params);

		assertEquals(1.0, listContainsMatch.compare(a, b, conf));
	}

	@Test
	public void stringContainsMatchTest(){

		params.put("string", "openorgs");
		params.put("bool", "XOR");
		params.put("caseSensitive", "false");

		StringContainsMatch stringContainsMatch = new StringContainsMatch(params);

		assertEquals(0.0, stringContainsMatch.distance("openorgs", "openorgs", conf));

		params.put("string", "openorgs");
		params.put("bool", "AND");
		params.put("caseSensitive", "false");

		stringContainsMatch = new StringContainsMatch(params);

		assertEquals(1.0, stringContainsMatch.distance("openorgs", "openorgs", conf));
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

	}

	@Test
	public void levensteinTitleTest() {

		final LevensteinTitle levensteinTitle = new LevensteinTitle(params);

		double result = levensteinTitle.distance("Degradation of lignin β‐aryl ether units in Arabidopsis thaliana expressing LigD, LigF and LigG from Sphingomonas paucimobilis SYK‐6", "Degradation of lignin β-aryl ether units in <i>Arabidopsis thaliana</i> expressing <i>LigD</i>, <i>LigF</i> and <i>LigG</i> from <i>Sphingomonas paucimobilis</i> SYK-6", conf);

		System.out.println("result = " + result);
	}

	@Test
	public void levensteinTest() {
		final Levenstein levenstein = new Levenstein(params);

		double result = levenstein.distance("la bruzzo", "la bruzzo", conf);
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

		Field h = createFieldList(Arrays.asList("Other literature type", "Article"), "instanceType");
		result = instanceTypeMatch.compare(a, h, conf);

		assertEquals(1.0, result);
	}

	@Test
	public void authorsMatchTest() {

		AuthorsMatch authorsMatch = new AuthorsMatch(params);

		Field a = createFieldList(Arrays.asList("La Bruzzo, Sandro", "Atzori, Claudio", "De Bonis, Michele"), "authors");
		Field b = createFieldList(Arrays.asList("Atzori, C.", "La Bruzzo, S.", "De Bonis, M."), "authors");
		double result = authorsMatch.compare(a, b, conf);

		assertEquals(1.0, result);

		Field c = createFieldList(Arrays.asList("Manghi, Paolo"), "authors");
		Field d = createFieldList(Arrays.asList("Manghi, Pasquale"), "authors");
		result = authorsMatch.compare(c, d, conf);

		assertEquals(0.0, result) ;

		params.put("mode", "surname");
		authorsMatch = new AuthorsMatch(params);
		result = authorsMatch.compare(c, d, conf);

		assertEquals(1.0, result);

		Field e = createFieldList(Arrays.asList("Manghi, Paolo", "Atzori, Claudio"), "authors");
		result = authorsMatch.compare(a, e, conf);

		assertEquals(0.25, result);

	}

	@Test
	public void jsonListMatch() {

		JsonListMatch jsonListMatch = new JsonListMatch(params);

		Field a = createFieldList(Arrays.asList("{\"datainfo\":{\"deletedbyinference\":false,\"inferenceprovenance\":null,\"inferred\":false,\"invisible\":false,\"provenanceaction\":{\"classid\":\"sysimport:actionset\",\"classname\":\"Harvested\",\"schemeid\":\"dnet:provenanceActions\",\"schemename\":\"dnet:provenanceActions\"},\"trust\":\"0.9\"},\"qualifier\":{\"classid\":\"doi\",\"classname\":\"Digital Object Identifier\",\"schemeid\":\"dnet:pid_types\",\"schemename\":\"dnet:pid_types\"},\"value\":\"10.1111/pbi.12655\"}"), "authors");
		Field b = createFieldList(Arrays.asList("{\"datainfo\":{\"deletedbyinference\":false,\"inferenceprovenance\":\"\",\"inferred\":false,\"invisible\":false,\"provenanceaction\":{\"classid\":\"sysimport:crosswalk:repository\",\"classname\":\"Harvested\",\"schemeid\":\"dnet:provenanceActions\",\"schemename\":\"dnet:provenanceActions\"},\"trust\":\"0.9\"},\"qualifier\":{\"classid\":\"pmc\",\"classname\":\"PubMed Central ID\",\"schemeid\":\"dnet:pid_types\",\"schemename\":\"dnet:pid_types\"},\"value\":\"PMC5399005\"}","{\"datainfo\":{\"deletedbyinference\":false,\"inferenceprovenance\":\"\",\"inferred\":false,\"invisible\":false,\"provenanceaction\":{\"classid\":\"sysimport:crosswalk:repository\",\"classname\":\"Harvested\",\"schemeid\":\"dnet:provenanceActions\",\"schemename\":\"dnet:provenanceActions\"},\"trust\":\"0.9\"},\"qualifier\":{\"classid\":\"pmid\",\"classname\":\"PubMed ID\",\"schemeid\":\"dnet:pid_types\",\"schemename\":\"dnet:pid_types\"},\"value\":\"27775869\"}","{\"datainfo\":{\"deletedbyinference\":false,\"inferenceprovenance\":\"\",\"inferred\":false,\"invisible\":false,\"provenanceaction\":{\"classid\":\"user:claim\",\"classname\":\"Linked by user\",\"schemeid\":\"dnet:provenanceActions\",\"schemename\":\"dnet:provenanceActions\"},\"trust\":\"0.9\"},\"qualifier\":{\"classid\":\"doi\",\"classname\":\"Digital Object Identifier\",\"schemeid\":\"dnet:pid_types\",\"schemename\":\"dnet:pid_types\"},\"value\":\"10.1111/pbi.12655\"}","{\"datainfo\":{\"deletedbyinference\":false,\"inferenceprovenance\":\"\",\"inferred\":false,\"invisible\":false,\"provenanceaction\":{\"classid\":\"sysimport:crosswalk:repository\",\"classname\":\"Harvested\",\"schemeid\":\"dnet:provenanceActions\",\"schemename\":\"dnet:provenanceActions\"},\"trust\":\"0.9\"},\"qualifier\":{\"classid\":\"handle\",\"classname\":\"Handle\",\"schemeid\":\"dnet:pid_types\",\"schemename\":\"dnet:pid_types\"},\"value\":\"1854/LU-8523529\"}"), "authors");

		double result = jsonListMatch.compare(a, b, conf);

		assertEquals(0.25, result);

		params.put("mode", "count");
		jsonListMatch = new JsonListMatch(params);
		result = jsonListMatch.compare(a, b, conf);

		assertEquals(1.0, result);
	}


}
