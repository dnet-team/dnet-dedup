package eu.dnetlib.pace.tree;

import com.wcohen.ss.AbstractStringDistance;
import eu.dnetlib.pace.tree.support.AbstractSortedComparator;
import eu.dnetlib.pace.tree.support.ComparatorClass;

import java.util.Map;

/**
 * The Class SortedJaroWinkler.
 */
@ComparatorClass("sortedLevel2JaroWinkler")
public class SortedLevel2JaroWinkler extends AbstractSortedComparator {

	/**
	 * Instantiates a new sorted jaro winkler.
	 * 
	 * @param weight
	 *            the weight
	 */
	public SortedLevel2JaroWinkler(final double weight) {
		super(weight, new com.wcohen.ss.Level2JaroWinkler());
	}

	public SortedLevel2JaroWinkler(final Map<String, String> params){
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
	 * @see eu.dnetlib.pace.compare.DistanceAlgo#getWeight()
	 */
	@Override
	public double getWeight() {
		return super.weight;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.dnetlib.pace.compare.SecondStringDistanceAlgo#normalize(double)
	 */
	@Override
	protected double normalize(final double d) {
		return d;
	}

}
