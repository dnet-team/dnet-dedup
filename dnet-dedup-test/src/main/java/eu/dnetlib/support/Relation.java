package eu.dnetlib.support;

import eu.dnetlib.Deduper;
import org.apache.spark.graphx.Edge;

import java.io.Serializable;

public class Relation implements Serializable {

    String source;
    String target;
    String type;

    public Relation() {
    }

    public Relation(String source, String target, String type) {
        this.source = source;
        this.target = target;
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Edge<String> toEdgeRdd(){
        return new Edge<>(Deduper.hash(source), Deduper.hash(target), type);
    }

    @Override
    public String toString() {
        return "Relation{" +
                "source='" + source + '\'' +
                ", target='" + target + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
