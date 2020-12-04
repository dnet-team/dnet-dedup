package eu.dnetlib.pace.util;

import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

public class UtilTest {

    Map<String, String> params;

    @BeforeAll
    public void setUp(){
        params = new HashMap<String, String>();
    }

    @Test
    public void paceResolverTest() {
        PaceResolver paceResolver = new PaceResolver();
        paceResolver.getComparator("keywordMatch", params);
    }

}
