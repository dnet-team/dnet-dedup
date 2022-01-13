package eu.dnetlib.pace.util;

import eu.dnetlib.pace.model.Person;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UtilTest {

    static Map<String, String> params;

    @BeforeAll
    public static void setUp(){
        params = new HashMap<>();
    }

    @Test
    @Ignore
    public void paceResolverTest() {
        PaceResolver paceResolver = new PaceResolver();
        paceResolver.getComparator("keywordMatch", params);
    }

    @Test
    public void personTest() {
        Person p = new Person("j. f. kennedy", false);

        assertEquals("kennedy", p.getSurnameString());
        assertEquals("j f", p.getNameString());
    }

}
