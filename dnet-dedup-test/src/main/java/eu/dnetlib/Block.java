package eu.dnetlib;

import eu.dnetlib.pace.model.MapDocument;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Block implements Serializable {

    String key;
    List<MapDocument> elements;

    public Block(String key, Iterable<MapDocument> elements){
        this.key = key;
        this.elements = StreamSupport.stream(elements.spliterator(), false).collect(Collectors.toList());
    }

    public Block(String key, List<MapDocument> elements){
        this.key = key;
        this.elements = elements;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<MapDocument> getElements() {
        return elements;
    }

    public void setElements(List<MapDocument> elements) {
        this.elements = elements;
    }

    public int comparisons(){
        int size = elements.size();
        return (size*(size-1)/2);
    }

    public int elements(){
        return elements.size();
    }
}
