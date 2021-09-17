package eu.dnetlib.pace;

import eu.dnetlib.pace.config.Type;
import eu.dnetlib.pace.model.FieldListImpl;
import eu.dnetlib.pace.model.MapDocument;
import eu.dnetlib.support.ConnectedComponent;
import org.apache.commons.io.IOUtils;
import org.apache.spark.api.java.JavaRDD;
import org.codehaus.jackson.map.ObjectMapper;
import scala.Tuple2;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class DedupTestUtils {

    public static String prepareTable(MapDocument doc) {

        String ret = "<table>";

        for(String fieldName: doc.getFieldMap().keySet()) {
            if (doc.getFieldMap().get(fieldName).getType().equals(Type.String)) {
                ret += "<tr><th>" + fieldName + "</th><td>" + doc.getFieldMap().get(fieldName).stringValue() + "</td></tr>";
            }
            else if (doc.getFieldMap().get(fieldName).getType().equals(Type.List)) {
                ret += "<tr><th>" + fieldName + "</th><td>[" + ((FieldListImpl)doc.getFieldMap().get(fieldName)).stringList().stream().collect(Collectors.joining(";")) + "]</td></tr>";
            }
        }

        return ret + "</table>";

    }

    public static void prepareGraphParams(List<String> vertexes, List<Tuple2<String, String>> edgesTuple, String filePath, String templateFilePath, Map<String, MapDocument> mapDocuments) {

        List<Node> nodes = vertexes.stream().map(v -> new Node(v.substring(3, 20).replaceAll("_", ""), vertexes.indexOf(v), prepareTable(mapDocuments.get(v)))).collect(Collectors.toList());
        List<Edge> edges = edgesTuple.stream().map(e -> new Edge(vertexes.indexOf(e._1()), vertexes.indexOf(e._2()))).collect(Collectors.toList());

        try(FileWriter fw = new FileWriter(filePath)) {
            String fullText = IOUtils.toString(new FileReader(templateFilePath));

            String s = fullText
                    .replaceAll("%nodes%", new ObjectMapper().writeValueAsString(nodes))
                    .replaceAll("%edges%", new ObjectMapper().writeValueAsString(edges));

            IOUtils.write(s, fw);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String getOrganizationLegalname(MapDocument mapDocument){
        return mapDocument.getFieldMap().get("legalname").stringValue();
    }

    public static String getJSONEntity(List<String> entities, String id){

        for (String entity: entities) {
            if(entity.contains(id))
                return entity;
        }
        return "";
    }

}

class Node{
    String label;
    int id;
    String title;

    public Node(String label, int id, String title) {
        this.label = label;
        this.id = id;
        this.title = title;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

class Edge{
    int from;
    int to;

    public Edge(int from, int to) {
        this.from = from;
        this.to = to;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }
}
