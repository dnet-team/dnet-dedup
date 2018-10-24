package eu.dnetlib.pace.clustering;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.stream.Collectors;

import org.reflections.Reflections;

public class ClusteringResolver implements Serializable {
    private final Map<String, Class<ClusteringFunction>> functionMap;

    public ClusteringResolver() {

        this.functionMap = new Reflections("eu.dnetlib").getTypesAnnotatedWith(ClusteringClass.class).stream()
                .filter(ClusteringFunction.class::isAssignableFrom)
                .collect(Collectors.toMap(cl -> cl.getAnnotation(ClusteringClass.class).value(), cl -> (Class<ClusteringFunction>)cl));
    }

    public ClusteringFunction resolve(String clusteringFunction) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {

        return functionMap.get(clusteringFunction).newInstance();
    }
}
