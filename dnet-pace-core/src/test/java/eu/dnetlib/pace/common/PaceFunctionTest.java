package eu.dnetlib.pace.common;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class PaceFunctionTest extends AbstractPaceFunctions {

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
}
