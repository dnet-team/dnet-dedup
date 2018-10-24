package eu.dnetlib.pace.distance.algo;

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

	@Override
	public double distance(Field a, Field b) {
		return 0.0;
	}

	@Override
	public double getWeight() {
		return 0.0;
	}

	@Override
	public void setWeight(double w){
	}

	@Override
	public Map<String, Number> getParams() {
		return null;
	}

	@Override
	public void setParams(Map<String, Number> params) {
	}
}
