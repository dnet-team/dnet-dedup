package eu.dnetlib.pace.distance.algo;

import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.distance.DistanceAlgo;
import eu.dnetlib.pace.distance.DistanceClass;
import eu.dnetlib.pace.model.Field;

import java.util.Map;

/**
 * Not all fields of a document need to partecipate in the distance measure. We model those fields as having a
 * NullDistanceAlgo.
 */
@DistanceClass("Null")
public class NullDistanceAlgo implements DistanceAlgo {

	public NullDistanceAlgo(Map<String, Number> params){
	}

	@Override
	public double distance(Field a, Field b, final Config conf) {
		return 0.0;
	}

	@Override
	public double getWeight() {
		return 0.0;
	}

}
