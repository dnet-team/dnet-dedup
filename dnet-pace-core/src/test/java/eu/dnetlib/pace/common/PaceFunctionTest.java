package eu.dnetlib.pace.common;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PaceFunctionTest extends AbstractPaceFunctions {

    private final static String TEST_STRING = "Toshiba NB550D: è un netbook su piattaforma AMD Fusion⁽¹²⁾.";

    @Test
    public void normalizePidTest(){

        assertEquals("identifier", normalizePid("IdentifIer"));
        assertEquals("10.1109/tns.2015.2493347", normalizePid("10.1109/TNS.2015.2493347"));
        assertEquals("10.0001/testdoi", normalizePid("http://dx.doi.org/10.0001/testDOI"));
        assertEquals("10.0001/testdoi", normalizePid("https://dx.doi.org/10.0001/testDOI"));
    }

    @Test
    public void filterAllStopwordsTest(){

        assertEquals("universita politecnica marche", filterAllStopWords("universita politecnica delle marche"));
    }

    @Test
    public void normalizeTest() {
        assertEquals("universitat", normalize("Universität"));

        System.out.println(normalize("İstanbul Ticarət Universiteti"));
    }

    @Test
    public void cleanupTest() {
        assertEquals("istanbul ticaret universiteti", cleanup("İstanbul Ticarət Universiteti"));


        System.out.println("cleaned up     : " + cleanup(TEST_STRING));
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

}
