package eu.dnetlib.pace.tree;

import eu.dnetlib.pace.AbstractPaceTest;
import eu.dnetlib.pace.config.Type;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldListImpl;
import eu.dnetlib.pace.model.FieldValueImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

//test class for comparators (to be used into the tree nodes)
public class ComparatorTest extends AbstractPaceTest {

    private Map<String, Number> params;

    @Before
    public void setup() {
        params = new HashMap<>();
        //to put all the needed parameters
        params.put("minCoauthors", 5);
        params.put("maxCoauthors", 200);

    }

    @Test
    public void testCoauthorsMatch() {

        final CoauthorsMatch coauthorsMatch = new CoauthorsMatch(params);

        Field a = createFieldList(Arrays.asList("la bruzzo, sandro", "atzori, claudio", "artini, michele", "de bonis, michele", "bardi, alessia", "dell'amico, andrea", "baglioni, miriam"), "coauthors");
        Field b = createFieldList(Arrays.asList("la bruzzo, sandro"), "coauthors");

        double result1 = coauthorsMatch.compare(a, b);
        double result2 = coauthorsMatch.compare(a, a);

        System.out.println("a = " + a);
        System.out.println("b = " + b);

        System.out.println("a vs b = " + result1);
        System.out.println("a vs a = " + result2);

        assertEquals(result1, -1.0);
        assertEquals(result2, 7.0);
    }

    @Test
    public void testExactMatch() {

        final ExactMatch exactMatch = new ExactMatch(params);

        Field a = new FieldValueImpl(Type.String, "example", "example");
        Field b = new FieldValueImpl(Type.String, "example", "Example");
        Field c = new FieldValueImpl(Type.String, "example", "");

        double result1 = exactMatch.compare(a,a);
        double result2 = exactMatch.compare(a,b);
        double result3 = exactMatch.compare(a,c);

        System.out.println("a = " + a.stringValue());
        System.out.println("b = " + b.stringValue());
        System.out.println("c = " + c.stringValue());

        System.out.println("a vs a = " + result1);
        System.out.println("a vs b = " + result2);
        System.out.println("a vs c = " + result3);

        assertEquals(result1, 1.0);
        assertEquals(result2, 0.0);
        assertEquals(result3, -1.0);

    }

    @Test
    public void testSimilarFirstname() {

        final SimilarFirstname similarFirstname = new SimilarFirstname(params);

        Field a = new FieldValueImpl(Type.String, "firstname", "sandro");
        Field b = new FieldValueImpl(Type.String, "firstname", "s.");
        Field c = new FieldValueImpl(Type.String, "firstname", "stefano");

        double result1 = similarFirstname.compare(a,b);
        double result2 = similarFirstname.compare(a,c);
        double result3 = similarFirstname.compare(b,c);

        System.out.println("a = " + a.stringValue());
        System.out.println("b = " + b.stringValue());
        System.out.println("c = " + c.stringValue());

        System.out.println("a vs b = " + result1);
        System.out.println("a vs c = " + result2);
        System.out.println("b vs c = " + result3);

        assertEquals(result1, 1.0);
        assertEquals(result3, 1.0);
        assertTrue(result2<0.7);

    }

    @Test
    public void testTopicsMatch() {

        final TopicsMatch topicsMatch = new TopicsMatch(params);

        Field a = createFieldList(Arrays.asList("0.0", "1.0", "0.0"), "topics");
        Field b = createFieldList(Arrays.asList("0.0", "0.0", "1.0"), "topics");
        Field c = createFieldList(Arrays.asList("0.5", "0.5", "0.0"), "topics");

        double result1 = topicsMatch.compare(a,a);
        double result2 = topicsMatch.compare(a,c);
        double result3 = topicsMatch.compare(b,c);

        System.out.println("a = " + a);
        System.out.println("b = " + b);
        System.out.println("c = " + c);

        System.out.println("a vs a = " + result1);
        System.out.println("a vs c = " + result2);
        System.out.println("b vs c = " + result3);

        assertEquals(result1, 1.0);
        assertEquals(result2, 0.5);
        assertEquals(result3, 0.0);

    }

    @Test
    public void testUndefinedNode() {

        final UndefinedNode undefinedNode = new UndefinedNode(params);
        double result = undefinedNode.compare(new FieldListImpl(),new FieldListImpl());

        assertEquals(result, -1.0);
    }

    @Test
    public void testMatchNode() {

        final MatchNode matchNode = new MatchNode(params);
        double result = matchNode.compare(new FieldListImpl(),new FieldListImpl());

        assertEquals(result, 1.0);
    }

    @Test
    public void testNoMatchNode() {

        final NoMatchNode noMatchNode = new NoMatchNode(params);
        double result = noMatchNode.compare(new FieldListImpl(),new FieldListImpl());

        assertEquals(result, 0.0);
    }

    @Test
    public void testDoiExactMatch() {

        final DoiExactMatch doiExactMatch = new DoiExactMatch(params);
        Field a = new FieldValueImpl(Type.String, "doi", "doi:10.1002/0470841559.ch1");
        Field b = new FieldValueImpl(Type.String, "doi", "10.1002/0470841559.ch1");
        Field c = new FieldValueImpl(Type.String, "doi", "10.1002/000000001232");
        Field d = new FieldValueImpl(Type.String, "doi", "");

        double result1 = doiExactMatch.compare(a,b);
        double result2 = doiExactMatch.compare(a,c);
        double result3 = doiExactMatch.compare(b,c);
        double result4 = doiExactMatch.compare(a,a);
        double result5 = doiExactMatch.compare(a,d);

        System.out.println("a = " + a.stringValue());
        System.out.println("b = " + b.stringValue());
        System.out.println("c = " + c.stringValue());
        System.out.println("d = " + d.stringValue());

        System.out.println("a vs b = " + result1);
        System.out.println("a vs c = " + result2);
        System.out.println("b vs c = " + result3);
        System.out.println("a vs a = " + result4);
        System.out.println("a vs d = " + result5);

        assertEquals(result1, 1.0);
        assertEquals(result2, 0.0);
        assertEquals(result3, 0.0);
        assertEquals(result4, 1.0);
        assertEquals(result5, -1.0);

    }

    @Test
    public void testDomainExactMatch() {
        final DomainExactMatch domainExactMatch = new DomainExactMatch(params);
        Field a = new FieldValueImpl(Type.String, "websiteurl", "https://www.doi.org/demos.html");
        Field b = new FieldValueImpl(Type.String, "websiteurl", "https://www.doi.org/index.html");
        Field c = new FieldValueImpl(Type.String, "websiteurl", "http://www.google.it/images.php");
        Field d = new FieldValueImpl(Type.String, "websiteurl", "");

        double result1 = domainExactMatch.compare(a,b);
        double result2 = domainExactMatch.compare(a,c);
        double result3 = domainExactMatch.compare(b,c);
        double result4 = domainExactMatch.compare(a,a);
        double result5 = domainExactMatch.compare(a,d);

        System.out.println("a = " + a.stringValue());
        System.out.println("b = " + b.stringValue());
        System.out.println("c = " + c.stringValue());
        System.out.println("d = " + d.stringValue());

        System.out.println("a vs b = " + result1);
        System.out.println("a vs c = " + result2);
        System.out.println("b vs c = " + result3);
        System.out.println("a vs a = " + result4);
        System.out.println("a vs d = " + result5);

        assertEquals(result1, 1.0);
        assertEquals(result2, 0.0);
        assertEquals(result3, 0.0);
        assertEquals(result4, 1.0);
        assertEquals(result5, -1.0);

    }

    @Test
    public void testExactMatchIgnoreCase() {
        final ExactMatchIgnoreCase exactMatchIgnoreCase = new ExactMatchIgnoreCase(params);

        Field a = new FieldValueImpl(Type.String, "example", "example");
        Field b = new FieldValueImpl(Type.String, "example", "Example");
        Field c = new FieldValueImpl(Type.String, "example", "");
        Field d = new FieldValueImpl(Type.String, "example", "exampla");

        double result1 = exactMatchIgnoreCase.compare(a,a);
        double result2 = exactMatchIgnoreCase.compare(a,b);
        double result3 = exactMatchIgnoreCase.compare(a,c);
        double result4 = exactMatchIgnoreCase.compare(a,d);

        System.out.println("a = " + a.stringValue());
        System.out.println("b = " + b.stringValue());
        System.out.println("c = " + c.stringValue());
        System.out.println("d = " + d.stringValue());

        System.out.println("a vs a = " + result1);
        System.out.println("a vs b = " + result2);
        System.out.println("a vs c = " + result3);
        System.out.println("a vs d = " + result4);

        assertEquals(result1, 1.0);
        assertEquals(result2, 1.0);
        assertEquals(result3, -1.0);
        assertEquals(result4, 0.0);

    }

    @Test
    public void testMustBeDifferent() {
        final MustBeDifferent mustBeDifferent = new MustBeDifferent(params);

        Field a = new FieldValueImpl(Type.String, "example", "string1");
        Field b = new FieldValueImpl(Type.String, "example", "string2");
        Field c = new FieldValueImpl(Type.String, "example", "");

        double result1 = mustBeDifferent.compare(a,a);
        double result2 = mustBeDifferent.compare(a,b);
        double result3 = mustBeDifferent.compare(a,c);

        System.out.println("a = " + a.stringValue());
        System.out.println("b = " + b.stringValue());
        System.out.println("c = " + c.stringValue());

        System.out.println("a vs a = " + result1);
        System.out.println("a vs b = " + result2);
        System.out.println("a vs c = " + result3);

        assertEquals(result1, 0.0);
        assertEquals(result2, 1.0);
        assertEquals(result3, -1.0);
    }

    @Test
    public void testJaroWinklerNormalizedName() {
        final JaroWinklerNormalizedName jaroWinklerNormalizedName = new JaroWinklerNormalizedName(params);

        Field a = new FieldValueImpl(Type.String, "name", "Universita di Pisa");
        Field b = new FieldValueImpl(Type.String, "name", "Universita di Parma");
        Field c = new FieldValueImpl(Type.String, "name", "University of New York");
        Field d = new FieldValueImpl(Type.String, "name", "Università di New York");

        double result1 = jaroWinklerNormalizedName.compare(a, b);
        double result2 = jaroWinklerNormalizedName.compare(c, d);

        assertEquals(result1, 0.0);
        assertEquals(result2, 1.0);
    }

    //TODO add missing tests: JaroWinkler, JaroWinklerTitle, Level2JaroWinkler, Level2JaroWinklerTitle, Level2Levenshtein, Levenshtein, LevenshteinTitle, PidMatch, SizeMatch, SortedJaroWinkler, SortedLevel2JaroWinkler, SubstringLevenshtein, TitleVersionMatch, UrlMatcher, YearMatch
}
