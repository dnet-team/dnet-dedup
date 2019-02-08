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

        Field a = new FieldValueImpl(Type.String, "doi", "10.1000/0000000000");
        Field b = new FieldValueImpl(Type.String, "doi", "10.1033/0000000000");
        Field c = new FieldValueImpl(Type.String, "doi", "");

        double result1 = exactMatch.compare(a,a);
        double result2 = exactMatch.compare(a,b);
        double result3 = exactMatch.compare(a,c);

        System.out.println("a = " + a);
        System.out.println("b = " + b);
        System.out.println("c = " + c);

        System.out.println("a vs a = " + result1);
        System.out.println("a vs b = " + result2);
        System.out.println("a vs c = " + result3);

        assertEquals(result1, 1.0);
        assertEquals(result2, 0.0);
        assertEquals(result3, -1.0);

    }

    @Test
    public void testSimilarMatch() {

        final SimilarMatch similarMatch = new SimilarMatch(params);

        Field a = new FieldValueImpl(Type.String, "firstname", "sandro");
        Field b = new FieldValueImpl(Type.String, "firstname", "s.");
        Field c = new FieldValueImpl(Type.String, "firstname", "stefano");

        double result1 = similarMatch.compare(a,b);
        double result2 = similarMatch.compare(a,c);
        double result3 = similarMatch.compare(b,c);

        System.out.println("a = " + a);
        System.out.println("b = " + b);
        System.out.println("c = " + c);

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

        final UndefinedNode undefinedNode = new UndefinedNode();
        double result = undefinedNode.compare(new FieldListImpl(),new FieldListImpl());

        assertEquals(result, 0.0);
    }


}
