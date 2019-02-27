package eu.dnetlib.pace.tree;

import com.wcohen.ss.AbstractStringDistance;
import eu.dnetlib.pace.tree.support.ComparatorClass;
import eu.dnetlib.pace.tree.support.AbstractSortedDistanceAlgo;

import java.util.Map;

@ComparatorClass("SortedJaroWinkler")
public class SortedJaroWinkler extends AbstractSortedDistanceAlgo {

    public SortedJaroWinkler(Map<String,Number> params){
        super(params, new com.wcohen.ss.Levenstein());
    }

    /**
     * Instantiates a new sorted jaro winkler.
     *
     * @param weight
     *            the weight
     */
    public SortedJaroWinkler(final double weight) {
        super(weight, new com.wcohen.ss.JaroWinkler());
    }

    /**
     * Instantiates a new sorted jaro winkler.
     *
     * @param weight
     *            the weight
     * @param ssalgo
     *            the ssalgo
     */
    protected SortedJaroWinkler(final double weight, final AbstractStringDistance ssalgo) {
        super(weight, ssalgo);
    }

    /*
     * (non-Javadoc)
     *
     * @see eu.dnetlib.pace.distance.DistanceAlgo#getWeight()
     */
    @Override
    public double getWeight() {
        return super.weight;
    }

    /*
     * (non-Javadoc)
     *
     * @see eu.dnetlib.pace.distance.AbstractDistanceAlgo#normalize(double)
     */
    @Override
    protected double normalize(final double d) {
        return d;
    }

}

