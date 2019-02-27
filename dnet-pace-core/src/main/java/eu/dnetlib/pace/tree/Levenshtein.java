package eu.dnetlib.pace.tree;

import com.wcohen.ss.AbstractStringDistance;
import eu.dnetlib.pace.tree.support.AbstractDistanceAlgo;
import eu.dnetlib.pace.tree.support.ComparatorClass;

import java.util.Map;

@ComparatorClass("levenshtein")
public class Levenshtein extends AbstractDistanceAlgo {
    public Levenshtein(Map<String,Number> params){
        super(params, new com.wcohen.ss.Levenstein());
    }

    public Levenshtein(double w) {
        super(w, new com.wcohen.ss.Levenstein());
    }

    protected Levenshtein(double w, AbstractStringDistance ssalgo) {
        super(w, ssalgo);
    }

    @Override
    public double getWeight() {
        return super.weight;
    }

    @Override
    protected double normalize(double d) {
        return 1 / Math.pow(Math.abs(d) + 1, 0.1);
    }

}