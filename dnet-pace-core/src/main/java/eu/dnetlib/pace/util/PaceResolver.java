package eu.dnetlib.pace.util;

import eu.dnetlib.pace.clustering.ClusteringClass;
import eu.dnetlib.pace.clustering.ClusteringFunction;
import eu.dnetlib.pace.tree.support.Comparator;
import eu.dnetlib.pace.tree.support.ComparatorClass;
import org.reflections.Reflections;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.stream.Collectors;

public class PaceResolver implements Serializable {

    public static final Reflections CLUSTERING_RESOLVER = new Reflections("eu.dnetlib.pace.clustering");
    public static final Reflections COMPARATOR_RESOLVER = new Reflections("eu.dnetlib.pace.tree");

    private final Map<String, Class<ClusteringFunction>> clusteringFunctions;
    private final Map<String, Class<Comparator>> comparators;

    public PaceResolver() {

        this.clusteringFunctions = CLUSTERING_RESOLVER.getTypesAnnotatedWith(ClusteringClass.class).stream()
                .filter(ClusteringFunction.class::isAssignableFrom)
                .collect(Collectors.toMap(cl -> cl.getAnnotation(ClusteringClass.class).value(), cl -> (Class<ClusteringFunction>)cl));

        this.comparators = COMPARATOR_RESOLVER.getTypesAnnotatedWith(ComparatorClass.class).stream()
                .filter(Comparator.class::isAssignableFrom)
                .collect(Collectors.toMap(cl -> cl.getAnnotation(ComparatorClass.class).value(), cl -> (Class<Comparator>)cl));
    }

    public ClusteringFunction getClusteringFunction(String name, Map<String, Integer> params) throws PaceException {
        try {
            return clusteringFunctions.get(name).getDeclaredConstructor(Map.class).newInstance(params);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new PaceException(name + " not found ", e);
        }
    }

    public Comparator getComparator(String name, Map<String, String> params) throws PaceException {
        try {
            return comparators.get(name).getDeclaredConstructor(Map.class).newInstance(params);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | NullPointerException e) {
            throw new PaceException(name + " not found ", e);
        }
    }

}
