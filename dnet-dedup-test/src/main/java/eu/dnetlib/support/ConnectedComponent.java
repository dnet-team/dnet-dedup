package eu.dnetlib.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.MapDocument;
import eu.dnetlib.pace.util.PaceException;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ConnectedComponent implements Serializable {

    private Set<MapDocument> docs;
    private String id;
    private Map<String, Field> fieldMap;

    public ConnectedComponent() {
    }

    public ConnectedComponent(Set<MapDocument> docs) {
        this.docs = docs;
        this.id = createID(docs);
        this.fieldMap = chooseFields(docs);
    }

    public Set<MapDocument> getDocs() {
        return docs;
    }

    public void setDocs(Set<MapDocument> docs) {
        this.docs = docs;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Field> chooseFields(Set<MapDocument> docs) {

        int maxLength = 0;
        Map<String, Field> maxFieldMap = new HashMap<>();
        for (MapDocument doc : docs) {
            if (doc.toString().length()>maxLength){
                maxFieldMap = doc.getFieldMap();
                maxLength = doc.toString().length();
            }
        }

        return maxFieldMap;
    }

    public String createID(Set<MapDocument> docs) {
        if (docs.size() > 1) {
            String ccID = getMin(docs.stream().map(doc -> doc.getIdentifier()).collect(Collectors.toList()));
            String prefix = ccID.split("\\|")[0];
            String id = ccID.split("::")[1];
            return prefix + "|dedup_______::" + id;
        } else {
            return docs.iterator().next().getIdentifier();
        }
    }

    @JsonIgnore
    public String getMin(List<String> ids){

        String min = ids.get(0);
        for(String id: ids)
            if (min.compareTo(id) > 0) {
                min = id;
            }

        return min;
    }

    @Override
    public String toString(){
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (IOException e) {
            throw new PaceException("Failed to create Json: ", e);
        }
    }

    public Map<String, Field> getFieldMap() {
        return fieldMap;
    }

    public void setFieldMap(Map<String, Field> fieldMap) {
        this.fieldMap = fieldMap;
    }
}
