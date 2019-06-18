package eu.dnetlib;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import eu.dnetlib.pace.model.MapDocument;
import eu.dnetlib.pace.util.PaceException;

import java.io.IOException;
import java.io.Serializable;
import java.util.Set;

public class DocumentsBlock implements Serializable {

    String key;
    Set<MapDocument> documents;

    public DocumentsBlock(String key, Set<MapDocument> documents) {
        this.key = key;
        this.documents = documents;
    }

    public DocumentsBlock(String key, Iterable<MapDocument> documents) {
        this.key = key;
        this.documents = Sets.newHashSet(documents);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Iterable<MapDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(Set<MapDocument> documents) {
        this.documents = documents;
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
