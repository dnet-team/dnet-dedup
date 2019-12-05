package eu.dnetlib.pace.util;

import com.arakelian.jq.ImmutableJqLibrary;
import com.arakelian.jq.ImmutableJqRequest;
import com.arakelian.jq.JqLibrary;
import com.arakelian.jq.JqResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import eu.dnetlib.pace.config.DedupConfig;
import eu.dnetlib.pace.config.Type;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldListImpl;
import eu.dnetlib.pace.model.FieldValueImpl;
import eu.dnetlib.pace.model.MapDocument;
import net.minidev.json.JSONArray;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MapDocumentUtil {

    private static final JqLibrary library = ImmutableJqLibrary.of();
    private static final ObjectMapper mapper = new ObjectMapper();
    public static final String URL_REGEX = "^(http|https|ftp)\\://.*";
    public static Predicate<String> urlFilter = s -> s.trim().matches(URL_REGEX);

    public static MapDocument asMapDocument(DedupConfig conf, final String json) {

        MapDocument m = new MapDocument();

        final ImmutableJqRequest.Builder requestBuilder = ImmutableJqRequest.builder() //
                .lib(library) //
                .input(json);
        m.setIdentifier(getStringValue(conf.getWf().getIdPath(), requestBuilder));
        Map<String, Field> stringField = new HashMap<>();
        conf.getPace().getModel().forEach(fdef -> {
            switch (fdef.getType()) {
                case String:
                case Int:

                    stringField.put(fdef.getName(), new FieldValueImpl(fdef.getType(), fdef.getName(), getStringValue(fdef.getPath(), requestBuilder)));
                    break;
                case URL:
                    String uv = getStringValue(fdef.getPath(), requestBuilder);
                    if (!urlFilter.test(uv)) uv = "";
                    stringField.put(fdef.getName(), new FieldValueImpl(fdef.getType(), fdef.getName(), uv));
                    break;
                case List:
                case JSON:
                    FieldListImpl fi = new FieldListImpl(fdef.getName(), fdef.getType());
                    getListValue(fdef.getPath(), requestBuilder)
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

    private static List<String> getJPathList(String path, String json, Type type) {
        if (type == Type.List)
            return JsonPath.read(json, path);
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


    private static String getJPathString(final String jsonPath, final String json) {
        Object o = JsonPath.read(json, jsonPath);

        if (o instanceof String)
            return (String)o;
        if (o instanceof  JSONArray && ((JSONArray)o).size()>0)
            return (String)((JSONArray)o).get(0);
        return "";
    }

    private static String getStringValue(final String jqPath, final ImmutableJqRequest.Builder requestBuilder) {
        final JqResponse response = requestBuilder
                .filter(jqPath)
                .build()
                .execute();
        String output = response.getOutput();
        if (StringUtils.isNotBlank(output)) {
            output = output.replaceAll("\"", "");
        }
        return output;

    }

    private static List<String> getListValue(final String jqPath, final ImmutableJqRequest.Builder requestBuilder) {


        final JqResponse response = requestBuilder
                .filter(jqPath)
                .build()
                .execute();
//        if (response.hasErrors())
//            throw new PaceException(String.format("Error on getting jqPath, xpath:%s, error : %s", jqPath, response.getErrors().toString()));

        List<String> result = new ArrayList<>();

        final JsonNode root;
        try {
            root = mapper.readTree(response.getOutput());
        } catch (IOException e) {
            throw new PaceException("Error on parsing json", e);
        }
        final Iterator<JsonNode> elements = root.elements();
        while (elements.hasNext()) {
            result.add(elements.next().toString());
        }
        return result;
    }


}
