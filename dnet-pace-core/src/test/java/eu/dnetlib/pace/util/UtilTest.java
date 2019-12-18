package eu.dnetlib.pace.util;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class UtilTest {

    Map<String, String> params;

    @Before
    public void setUp(){
        params = new HashMap<String, String>();
    }

    @Test
    public void paceResolverTest() {
        PaceResolver paceResolver = new PaceResolver();
        paceResolver.getComparator("keywordMatch", params);
    }

}
