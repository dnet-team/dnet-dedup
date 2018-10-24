package eu.dnetlib.pace.distance;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.stream.Collectors;

import org.reflections.Reflections;

public class DistanceResolver implements Serializable {
    private final Map<String, Class<DistanceAlgo>> functionMap;

    public DistanceResolver() {

        this.functionMap = new Reflections("eu.dnetlib").getTypesAnnotatedWith(DistanceClass.class).stream()
                .filter(DistanceAlgo.class::isAssignableFrom)
                .collect(Collectors.toMap(cl -> cl.getAnnotation(DistanceClass.class).value(), cl -> (Class<DistanceAlgo>)cl));
    }

    public DistanceAlgo resolve(String algo) throws IllegalAccessException, InstantiationException {

        return functionMap.get(algo).newInstance();
    }
}