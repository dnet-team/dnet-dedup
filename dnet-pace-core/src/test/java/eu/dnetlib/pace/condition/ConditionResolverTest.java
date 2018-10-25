package eu.dnetlib.pace.condition;

import eu.dnetlib.pace.clustering.ClusteringFunction;
import eu.dnetlib.pace.clustering.ClusteringResolver;
import eu.dnetlib.pace.clustering.Ngrams;
import eu.dnetlib.pace.model.FieldDef;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ConditionResolverTest {

    private ConditionResolver conditionResolver;
    private List<FieldDef> fields;
    private String name;

    @Before
    public void setUp(){
        conditionResolver = new ConditionResolver();
    }

    @Test
    public void testResolve() throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        ConditionAlgo sizeMatch = conditionResolver.resolve("sizeMatch", fields);

        assertEquals(sizeMatch.getClass(), SizeMatch.class);
    }
}
