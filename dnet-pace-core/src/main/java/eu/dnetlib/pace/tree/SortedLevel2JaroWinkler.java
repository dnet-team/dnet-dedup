package eu.dnetlib.pace.tree;

import com.wcohen.ss.AbstractStringDistance;
import eu.dnetlib.pace.tree.support.ComparatorClass;
import eu.dnetlib.pace.tree.support.AbstractSortedDistanceAlgo;

import java.util.Map;

@ComparatorClass("SortedLevel2JaroWinkler")
public class SortedLevel2JaroWinkler extends AbstractSortedDistanceAlgo {

    /**
     * Instantiates a new sorted jaro winkler.
     *
     * @param weight
     *            the weight
     */
    public SortedLevel2JaroWinkler(final double weight) {
        super(weight, new com.wcohen.ss.Level2JaroWinkler());
    }

    public SortedLevel2JaroWinkler(final Map<String, Number> params){
        super(params, new com.wcohen.ss.Level2JaroWinkler());
    }

    /**
     * Instantiates a new sorted jaro winkler.
     *
     * @param weight
     *            the weight
     * @param ssalgo
     *            the ssalgo
     */
    protected SortedLevel2JaroWinkler(final double weight, final AbstractStringDistance ssalgo) {
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

