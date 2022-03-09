package eu.dnetlib.pace.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import eu.dnetlib.pace.config.DedupConfig;
import eu.dnetlib.pace.config.Type;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldListImpl;
import eu.dnetlib.pace.model.FieldValueImpl;
import eu.dnetlib.pace.model.MapDocument;
import net.minidev.json.JSONArray;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MapDocumentUtil {

    public static final String URL_REGEX = "^(http|https|ftp)\\://.*";
    public static Predicate<String> urlFilter = s -> s.trim().matches(URL_REGEX);

    public static MapDocument asMapDocumentWithJPath(DedupConfig conf, final String json) {
        MapDocument m = new MapDocument();
        m.setIdentifier(getJPathString(conf.getWf().getIdPath(), json));
        Map<String, Field> stringField = new HashMap<>();
        conf.getPace().getModel().forEach(fdef -> {
            switch (fdef.getType()) {
                case String:
                case Int:
                    stringField.put(fdef.getName(), new FieldValueImpl(fdef.getType(), fdef.getName(), truncateValue(getJPathString(fdef.getPath(), json), fdef.getLength())));
                    break;
                case URL:
                    String uv = getJPathString(fdef.getPath(), json);
                    if (!urlFilter.test(uv)) uv = "";
                    stringField.put(fdef.getName(), new FieldValueImpl(fdef.getType(), fdef.getName(), uv));
                    break;
                case List:
                case JSON:
                    FieldListImpl fi = new FieldListImpl(fdef.getName(), fdef.getType());
                    truncateList(getJPathList(fdef.getPath(), json, fdef.getType()), fdef.getSize())
                            .stream()
                            .map(item -> new FieldValueImpl(Type.String, fdef.getName(), item))
                            .forEach(fi::add);
                    stringField.put(fdef.getName(), fi);
                    break;
                case StringConcat:
                    String[] jpaths = fdef.getPath().split("\\|\\|\\|");
                    stringField.put(
                            fdef.getName(),
                            new FieldValueImpl(Type.String,
                                    fdef.getName(),
                                    truncateValue(Arrays.stream(jpaths).map(jpath -> getJPathString(jpath, json)).collect(Collectors.joining(" ")),
                                            fdef.getLength())
                            )
                    );
                    break;
            }
        });
        m.setFieldMap(stringField);
        return m;
    }

    public static List<String> getJPathList(String path, String json, Type type) {
        if (type == Type.List)
            return JsonPath.using(Configuration.defaultConfiguration().addOptions(Option.ALWAYS_RETURN_LIST, Option.SUPPRESS_EXCEPTIONS)).parse(json).read(path);
        Object jresult;
        List<String> result = new ArrayList<>();
        try {
            jresult = JsonPath.read(json, path);
        } catch (Throwable e) {
            return result;
        }
        if (jresult instanceof JSONArray) {

            ((JSONArray) jresult).forEach(it -> {

                        try {
                            result.add(new ObjectMapper().writeValueAsString(it));
                        } catch (JsonProcessingException e) {

                        }
                    }
            );
            return result;
        }

        if (jresult instanceof LinkedHashMap) {
            try {
                result.add(new ObjectMapper().writeValueAsString(jresult));
            } catch (JsonProcessingException e) {

            }
            return result;
        }
        if (jresult instanceof String) {
            result.add((String) jresult);
        }
        return result;
    }


    public static String getJPathString(final String jsonPath, final String json) {
        try {
            Object o = JsonPath.read(json, jsonPath);
            if (o instanceof String)
                return (String)o;
            if (o instanceof  JSONArray && ((JSONArray)o).size()>0)
                return (String)((JSONArray)o).get(0);
            return "";
        } catch (Exception e) {
            return "";
        }
    }


    public static String truncateValue(String value, int length) {
        if (value == null)
            return "";

        if (length == -1 || length > value.length())
            return value;

        return value.substring(0, length);
    }

    public static List<String> truncateList(List<String> list, int size) {
        if (size == -1 || size > list.size())
            return list;

        return list.subList(0, size);
    }

}
