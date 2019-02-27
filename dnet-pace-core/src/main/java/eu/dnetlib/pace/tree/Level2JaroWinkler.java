package eu.dnetlib.pace.tree;

import com.wcohen.ss.AbstractStringDistance;
import eu.dnetlib.pace.tree.support.ComparatorClass;
import eu.dnetlib.pace.tree.support.AbstractDistanceAlgo;

import java.util.Map;

@ComparatorClass("Level2JaroWinkler")
public class Level2JaroWinkler extends AbstractDistanceAlgo {

    public Level2JaroWinkler(Map<String, Number> params){
        super(params, new com.wcohen.ss.Level2JaroWinkler());
    }

    public Level2JaroWinkler(double w) {
        super(w, new com.wcohen.ss.Level2JaroWinkler());
    }

    protected Level2JaroWinkler(double w, AbstractStringDistance ssalgo) {
        super(w, ssalgo);
    }

    @Override
    public double getWeight() {
        return super.weight;
    }

    @Override
    protected double normalize(double d) {
        return d;
    }

}
