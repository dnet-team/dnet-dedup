package eu.dnetlib.pace.tree;

import com.wcohen.ss.AbstractStringDistance;
import eu.dnetlib.pace.distance.DistanceClass;
import eu.dnetlib.pace.distance.SecondStringDistanceAlgo;
import eu.dnetlib.pace.tree.support.AbstractComparator;
import eu.dnetlib.pace.tree.support.ComparatorClass;

import java.util.Map;

@ComparatorClass("alwaysMatch")
public class AlwaysMatch extends AbstractComparator {

    public AlwaysMatch(final Map<String, Number> params){
        super(params, new com.wcohen.ss.JaroWinkler());
    }

    public AlwaysMatch(final double weight) {
        super(weight, new com.wcohen.ss.JaroWinkler());
    }

    protected AlwaysMatch(final double weight, final AbstractStringDistance ssalgo) {
        super(weight, ssalgo);
    }

    @Override
    public double distance(final String a, final String b) {
        return 1.0;
    }

    @Override
    public double getWeight() {
        return super.weight;
    }

    @Override
    protected double normalize(final double d) {
        return d;
    }

}

