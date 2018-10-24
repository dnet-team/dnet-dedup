package eu.dnetlib.pace.condition;

import java.io.Serializable;
import java.util.Map;
import java.util.stream.Collectors;

import org.reflections.Reflections;

public class ConditionResolver implements Serializable {
    private final Map<String, Class<ConditionAlgo>> functionMap;

    public ConditionResolver() {

        this.functionMap = new Reflections("eu.dnetlib").getTypesAnnotatedWith(ConditionClass.class).stream()
                .filter(ConditionAlgo.class::isAssignableFrom)
                .collect(Collectors.toMap(cl -> cl.getAnnotation(ConditionClass.class).value(), cl -> (Class<ConditionAlgo>)cl));
    }

    public ConditionAlgo resolve(String name) throws IllegalAccessException, InstantiationException {
        return functionMap.get(name).newInstance();
    }
}
