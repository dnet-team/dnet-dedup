package eu.dnetlib.pace.distance.algo;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.wcohen.ss.AbstractStringDistance;

import eu.dnetlib.pace.distance.SecondStringDistanceAlgo;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldList;

/**
 * For the rest of the fields delegate the distance measure to the second string library.
 */
public abstract class SortedSecondStringDistanceAlgo extends SecondStringDistanceAlgo {

	/**
	 * Instantiates a new sorted second string distance algo.
	 * 
	 * @param weight
	 *            the weight
	 * @param ssalgo
	 *            the ssalgo
	 */
	protected SortedSecondStringDistanceAlgo(final double weight, final AbstractStringDistance ssalgo) {
		super(weight, ssalgo);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.dnetlib.pace.distance.SecondStringDistanceAlgo#toList(eu.dnetlib.pace.model.Field)
	 */
	@Override
	protected List<String> toList(final Field list) {
		FieldList fl = (FieldList) list;
		List<String> values = Lists.newArrayList(fl.stringList());
		Collections.sort(values);
		return values;
	}

}
