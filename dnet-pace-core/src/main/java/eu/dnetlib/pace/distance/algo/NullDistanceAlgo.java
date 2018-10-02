package eu.dnetlib.pace.distance.algo;

import eu.dnetlib.pace.distance.DistanceAlgo;
import eu.dnetlib.pace.model.Field;

/**
 * Not all fields of a document need to partecipate in the distance measure. We model those fields as having a
 * NullDistanceAlgo.
 */
public class NullDistanceAlgo implements DistanceAlgo {

	@Override
	public double distance(Field a, Field b) {
		return 0.0;
	}

	@Override
	public double getWeight() {
		return 0.0;
	}

}
