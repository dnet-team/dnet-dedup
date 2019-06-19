package eu.dnetlib.pace.util;

import eu.dnetlib.pace.clustering.ClusteringClass;
import eu.dnetlib.pace.clustering.ClusteringFunction;
import eu.dnetlib.pace.condition.ConditionAlgo;
import eu.dnetlib.pace.condition.ConditionClass;
import eu.dnetlib.pace.distance.DistanceAlgo;
import eu.dnetlib.pace.distance.DistanceClass;
import eu.dnetlib.pace.model.FieldDef;
import org.reflections.Reflections;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PaceResolver implements Serializable {

    public static final Reflections CLUSTERING_RESOLVER = new Reflections("eu.dnetlib.pace.clustering");
    public static final Reflections CONDITION_RESOLVER = new Reflections("eu.dnetlib.pace.condition");
    public static final Reflections DISTANCE_RESOLVER = new Reflections("eu.dnetlib.pace.distance.algo");

    private final Map<String, Class<ClusteringFunction>> clusteringFunctions;
    private final Map<String, Class<ConditionAlgo>> conditionAlgos;
    private final Map<String, Class<DistanceAlgo>> distanceAlgos;

    public PaceResolver() {

        this.clusteringFunctions = CLUSTERING_RESOLVER.getTypesAnnotatedWith(ClusteringClass.class).stream()
                .filter(ClusteringFunction.class::isAssignableFrom)
                .collect(Collectors.toMap(cl -> cl.getAnnotation(ClusteringClass.class).value(), cl -> (Class<ClusteringFunction>)cl));

        this.conditionAlgos = CONDITION_RESOLVER.getTypesAnnotatedWith(ConditionClass.class).stream()
                .filter(ConditionAlgo.class::isAssignableFrom)
                .collect(Collectors.toMap(cl -> cl.getAnnotation(ConditionClass.class).value(), cl -> (Class<ConditionAlgo>)cl));

        this.distanceAlgos = DISTANCE_RESOLVER.getTypesAnnotatedWith(DistanceClass.class).stream()
                .filter(DistanceAlgo.class::isAssignableFrom)
                .collect(Collectors.toMap(cl -> cl.getAnnotation(DistanceClass.class).value(), cl -> (Class<DistanceAlgo>)cl));
    }

    public ClusteringFunction getClusteringFunction(String name, Map<String, Integer> params) throws PaceException {
        try {
            return clusteringFunctions.get(name).getDeclaredConstructor(Map.class).newInstance(params);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new PaceException(name + " not found ", e);
        }
    }

    public DistanceAlgo getDistanceAlgo(String name, Map<String, Number> params) throws PaceException {
        try {
            return distanceAlgos.get(name).getDeclaredConstructor(Map.class).newInstance(params);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new PaceException(name + " not found ", e);
        }
    }

    public ConditionAlgo getConditionAlgo(String name, List<FieldDef> fields) throws PaceException {
        try {
            return conditionAlgos.get(name).getDeclaredConstructor(String.class, List.class).newInstance(name, fields);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new PaceException(name + " not found ", e);
        }
    }

}
