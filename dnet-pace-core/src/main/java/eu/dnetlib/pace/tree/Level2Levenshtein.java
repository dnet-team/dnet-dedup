package eu.dnetlib.pace.tree;


import com.wcohen.ss.AbstractStringDistance;
import eu.dnetlib.pace.tree.support.AbstractDistanceAlgo;
import eu.dnetlib.pace.tree.support.ComparatorClass;

import java.util.Map;

@ComparatorClass("Level2Levenstein")
public class Level2Levenshtein extends AbstractDistanceAlgo {

    public Level2Levenshtein(Map<String,Number> params){
        super(params, new com.wcohen.ss.Level2Levenstein());
    }

    public Level2Levenshtein(double w) {
        super(w, new com.wcohen.ss.Level2Levenstein());
    }

    protected Level2Levenshtein(double w, AbstractStringDistance ssalgo) {
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
