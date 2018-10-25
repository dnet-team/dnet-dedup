package eu.dnetlib.pace.condition;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import eu.dnetlib.pace.model.FieldDef;
import org.reflections.Reflections;

public class ConditionResolver implements Serializable {
    private final Map<String, Class<ConditionAlgo>> functionMap;

    public ConditionResolver() {

        this.functionMap = new Reflections("eu.dnetlib").getTypesAnnotatedWith(ConditionClass.class).stream()
                .filter(ConditionAlgo.class::isAssignableFrom)
                .collect(Collectors.toMap(cl -> cl.getAnnotation(ConditionClass.class).value(), cl -> (Class<ConditionAlgo>)cl));
    }

    public ConditionAlgo resolve(String name, List<FieldDef> fields) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        return functionMap.get(name).getDeclaredConstructor(String.class, List.class).newInstance(name, fields);
    }
}
