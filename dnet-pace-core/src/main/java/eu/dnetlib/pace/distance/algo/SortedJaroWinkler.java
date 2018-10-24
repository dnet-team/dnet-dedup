package eu.dnetlib.pace.distance.algo;

import com.wcohen.ss.AbstractStringDistance;
import eu.dnetlib.pace.distance.DistanceClass;

/**
 * The Class SortedJaroWinkler.
 */
@DistanceClass("SortedJaroWinkler")
public class SortedJaroWinkler extends SortedSecondStringDistanceAlgo {

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
	 * @see eu.dnetlib.pace.distance.SecondStringDistanceAlgo#normalize(double)
	 */
	@Override
	protected double normalize(final double d) {
		return d;
	}

}
