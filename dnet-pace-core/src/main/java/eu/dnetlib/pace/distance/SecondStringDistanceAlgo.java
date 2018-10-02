package eu.dnetlib.pace.distance;

import java.util.List;

import com.wcohen.ss.AbstractStringDistance;

import eu.dnetlib.pace.common.AbstractPaceFunctions;
import eu.dnetlib.pace.config.Type;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldList;

/**
 * For the rest of the fields delegate the distance measure to the second string library.
 */
public abstract class SecondStringDistanceAlgo extends AbstractPaceFunctions implements DistanceAlgo {

	// val aliases = Map(('â‚' to 'â‚‰') zip ('1' to '9'): _*) ++ Map(('â´' to 'â¹') zip ('4' to '9'): _*) ++ Map('Â¹' -> '1', 'Â²' ->
	// '2', * 'Â³'
	// -> '3')

	/** The ssalgo. */
	protected AbstractStringDistance ssalgo;

	/** The weight. */
	protected double weight = 0.0;

	/**
	 * Instantiates a new second string distance algo.
	 * 
	 * @param weight
	 *            the weight
	 * @param ssalgo
	 *            the ssalgo
	 */
	protected SecondStringDistanceAlgo(final double weight, final AbstractStringDistance ssalgo) {
		this.ssalgo = ssalgo;
		this.weight = weight;
	}

	/**
	 * Normalize.
	 * 
	 * @param d
	 *            the d
	 * @return the double
	 */
	protected abstract double normalize(double d);

	/**
	 * Distance.
	 * 
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the double
	 */
	public double distance(final String a, final String b) {
		double score = ssalgo.score(a, b);
		return normalize(score);
	}

	/**
	 * Distance.
	 * 
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the double
	 */
	protected double distance(final List<String> a, final List<String> b) {
		return distance(concat(a), concat(b));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.dnetlib.pace.distance.DistanceAlgo#distance(eu.dnetlib.pace.model.Field, eu.dnetlib.pace.model.Field)
	 */
	@Override
	public double distance(final Field a, final Field b) {
		if (a.getType().equals(Type.String) && b.getType().equals(Type.String)) return distance(a.stringValue(), b.stringValue());
		if (a.getType().equals(Type.List) && b.getType().equals(Type.List)) return distance(toList(a), toList(b));

		throw new IllegalArgumentException("invalid types\n- A: " + a.toString() + "\n- B: " + b.toString());
	}

	/**
	 * To list.
	 * 
	 * @param list
	 *            the list
	 * @return the list
	 */
	protected List<String> toList(final Field list) {
		return ((FieldList) list).stringList();
	}

}
