package eu.dnetlib.pace.distance.algo;

import com.wcohen.ss.AbstractStringDistance;

/**
 * The Class SortedJaroWinkler.
 */
public class SortedLevel2JaroWinkler extends SortedSecondStringDistanceAlgo {

	/**
	 * Instantiates a new sorted jaro winkler.
	 * 
	 * @param weight
	 *            the weight
	 */
	public SortedLevel2JaroWinkler(final double weight) {
		super(weight, new com.wcohen.ss.Level2JaroWinkler());
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
	 * @see eu.dnetlib.pace.distance.SecondStringDistanceAlgo#normalize(double)
	 */
	@Override
	protected double normalize(final double d) {
		return d;
	}

}
