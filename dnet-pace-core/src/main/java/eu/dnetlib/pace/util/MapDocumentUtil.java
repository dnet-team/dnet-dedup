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

public class MapDocumentUtil {


    private static final ObjectMapper mapper = new ObjectMapper();
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
                    stringField.put(fdef.getName(), new FieldValueImpl(fdef.getType(), fdef.getName(), getJPathString(fdef.getPath(), json)));
                    break;
                case URL:
                    String uv = getJPathString(fdef.getPath(), json);
                    if (!urlFilter.test(uv)) uv = "";
                    stringField.put(fdef.getName(), new FieldValueImpl(fdef.getType(), fdef.getName(), uv));
                    break;
                case List:
                case JSON:
                    FieldListImpl fi = new FieldListImpl(fdef.getName(), fdef.getType());
                    getJPathList(fdef.getPath(), json, fdef.getType())
                            .stream()
                            .map(item -> new FieldValueImpl(fdef.getType(), fdef.getName(), item))
                            .forEach(fi::add);
                    stringField.put(fdef.getName(), fi);
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


}
