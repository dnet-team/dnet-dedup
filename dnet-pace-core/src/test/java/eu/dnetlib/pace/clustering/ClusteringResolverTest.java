package eu.dnetlib.pace.clustering;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ClusteringResolverTest {

    private ClusteringResolver clusteringResolver;
    private Map<String,Integer> params = new HashMap<String, Integer>();

    @Before
    public void setUp(){
        clusteringResolver = new ClusteringResolver();
    }

    @Test
    public void testResolve() throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        ClusteringFunction ngrams = clusteringResolver.resolve("ngrams", params);

        assertEquals(ngrams.getClass(), Ngrams.class);
    }

}
