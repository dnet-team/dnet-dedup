package eu.dnetlib.pace.util;

import eu.dnetlib.pace.clustering.ClusteringClass;
import eu.dnetlib.pace.clustering.ClusteringFunction;
import eu.dnetlib.pace.condition.ConditionAlgo;
import eu.dnetlib.pace.condition.ConditionClass;
import eu.dnetlib.pace.distance.DistanceAlgo;
import eu.dnetlib.pace.distance.DistanceClass;
import eu.dnetlib.pace.model.FieldDef;
import eu.dnetlib.pace.tree.TreeNode;
import eu.dnetlib.pace.tree.TreeNodeClass;
import org.reflections.Reflections;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PaceResolver implements Serializable {

    private final Map<String, Class<ClusteringFunction>> clusteringFunctions;
    private final Map<String, Class<ConditionAlgo>> conditionAlgos;
    private final Map<String, Class<DistanceAlgo>> distanceAlgos;
    private final Map<String, Class<TreeNode>> treeNodes;

    public PaceResolver() {

        this.clusteringFunctions = new Reflections("eu.dnetlib").getTypesAnnotatedWith(ClusteringClass.class).stream()
                .filter(ClusteringFunction.class::isAssignableFrom)
                .collect(Collectors.toMap(cl -> cl.getAnnotation(ClusteringClass.class).value(), cl -> (Class<ClusteringFunction>)cl));

        this.conditionAlgos = new Reflections("eu.dnetlib").getTypesAnnotatedWith(ConditionClass.class).stream()
                .filter(ConditionAlgo.class::isAssignableFrom)
                .collect(Collectors.toMap(cl -> cl.getAnnotation(ConditionClass.class).value(), cl -> (Class<ConditionAlgo>)cl));

        this.distanceAlgos = new Reflections("eu.dnetlib").getTypesAnnotatedWith(DistanceClass.class).stream()
                .filter(DistanceAlgo.class::isAssignableFrom)
                .collect(Collectors.toMap(cl -> cl.getAnnotation(DistanceClass.class).value(), cl -> (Class<DistanceAlgo>)cl));

        this.treeNodes = new Reflections("eu.dnetlib").getTypesAnnotatedWith(TreeNodeClass.class).stream()
                .filter(TreeNode.class::isAssignableFrom)
                .collect(Collectors.toMap(cl -> cl.getAnnotation(TreeNodeClass.class).value(), cl -> (Class<TreeNode>) cl));
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

    public TreeNode getTreeNode(String name, Map<String, Number> params) throws PaceException {
        try {
            return treeNodes.get(name).getDeclaredConstructor(Map.class).newInstance(params);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | NullPointerException e) {
            throw new PaceException(name + " not found ", e);
        }
    }

}
