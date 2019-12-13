package eu.dnetlib.pace.tree.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.util.PaceException;


import java.io.IOException;
import java.io.Serializable;

public class FieldStats implements Serializable {

    private double weight;    //weight for the field (to be used in the aggregation)
    private double result;    //the result of the comparison
    private Field a;
    private Field b;

    private boolean countIfUndefined;

    public FieldStats(double weight, double result, boolean countIfUndefined, Field a, Field b) {
        this.weight = weight;
        this.result = result;
        this.countIfUndefined = countIfUndefined;
        this.a = a;
        this.b = b;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getResult() {
        return result;
    }

    public void setResult(double result) {
        this.result = result;
    }

    public boolean isCountIfUndefined() {
        return countIfUndefined;
    }

    public void setCountIfUndefined(boolean countIfUndefined) {
        this.countIfUndefined = countIfUndefined;
    }

    public Field getA() {
        return a;
    }

    public void setA(Field a) {
        this.a = a;
    }

    public Field getB() {
        return b;
    }

    public void setB(Field b) {
        this.b = b;
    }

    @Override
    public String toString(){
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (IOException e) {
            throw new PaceException("Impossible to convert to JSON: ", e);
        }
    }
}
