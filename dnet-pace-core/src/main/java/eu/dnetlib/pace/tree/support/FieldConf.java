package eu.dnetlib.pace.tree.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dnetlib.pace.util.PaceException;


import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

/**
 * The class that defines the configuration of each field in the decision tree.
 * */
public class FieldConf implements Serializable {

    private String field;               //name of the field on which apply the comparator
    private String comparator;          //comparator name
    private double weight = 1.0;        //weight for the field (to be used in the aggregation)
    private Map<String,String> params;  //parameters

    private boolean countIfUndefined;

    public boolean isCountIfUndefined() {
        return countIfUndefined;
    }

    public void setCountIfUndefined(boolean countIfUndefined) {
        this.countIfUndefined = countIfUndefined;
    }

    public FieldConf() {
    }

    public FieldConf(String field, String comparator, double weight, Map<String, String> params, boolean countIfUndefined) {
        this.field = field;
        this.comparator = comparator;
        this.weight = weight;
        this.params = params;
        this.countIfUndefined = countIfUndefined;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getComparator() {
        return comparator;
    }

    public void setComparator(String comparator) {
        this.comparator = comparator;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (IOException e) {
            throw new PaceException("Impossible to convert to JSON: ", e);
        }
    }
}