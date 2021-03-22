package eu.dnetlib.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.common.collect.Lists;

import eu.dnetlib.pace.model.MapDocument;

public class Block implements Serializable {

    private String key;

    private List<MapDocument> documents;

    public Block() {
        super();
    }

    public Block(String key, Iterable<MapDocument> documents) {
        this.key = key;
        this.documents = Lists.newArrayList(documents);
    }

    public static Block from(String key, MapDocument doc) {
        Block block = new Block();
        block.setKey(key);
        block.setDocuments(Lists.newArrayList(doc));
        return block;
    }

    public static Block from(String key, Iterator<Block> blocks, String orderField, int maxSize) {
        Block block = new Block();
        block.setKey(key);

        Iterable<Block> it = () -> blocks;

        block
                .setDocuments(
                        StreamSupport
                                .stream(it.spliterator(), false)
                                .flatMap(b -> b.getDocuments().stream())
                                .sorted(Comparator.comparing(a -> a.getFieldMap().get(orderField).stringValue()))
                                .limit(maxSize)
                                .collect(Collectors.toCollection(ArrayList::new)));
        return block;
    }

    public static Block from(Block b1, Block b2, String orderField, int maxSize) {
        Block block = new Block();
        block.setKey(b1.getKey());
        block
                .setDocuments(
                        Stream
                                .concat(b1.getDocuments().stream(), b2.getDocuments().stream())
                                .sorted(Comparator.comparing(a -> a.getFieldMap().get(orderField).stringValue()))
                                .limit(maxSize)
                                .collect(Collectors.toCollection(ArrayList::new)));
        return block;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<MapDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(List<MapDocument> documents) {
        this.documents = documents;
    }

    public int comparisons() {
        return (documents.size()*(documents.size()-1))/2;
    }

    public int elements() {
        return documents.size();
    }

    @Override
    public String toString() {
        return "Block{" +
                "key='" + key + '\'' +
                ", size=" + documents.size() + '\'' +
                ", names=" + documents.stream().map(d -> d.getFieldMap().get("country").stringValue()).collect(Collectors.toList()) + '\'' +
                '}';
    }
}

