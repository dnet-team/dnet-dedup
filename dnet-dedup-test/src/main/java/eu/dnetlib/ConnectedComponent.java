package eu.dnetlib;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dnetlib.pace.model.MapDocument;
import eu.dnetlib.pace.util.PaceException;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ConnectedComponent implements Serializable {

    private Set<MapDocument> docs;
    private String id;

    public ConnectedComponent() {
    }

    public ConnectedComponent(String id, Set<MapDocument> docs) {
        this.id = id;
        this.docs = docs;
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

    public void initializeID() {
        if (docs.size() > 1) {
            String ccID = getMin(docs.stream().map(doc -> doc.getIdentifier()).collect(Collectors.toList()));
            String prefix = ccID.split("\\|")[0];
            String id = ccID.split("::")[1];
            this.id = prefix + "|dedup_______::" + id;
        } else {
            this.id = docs.iterator().next().getIdentifier();
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
}
