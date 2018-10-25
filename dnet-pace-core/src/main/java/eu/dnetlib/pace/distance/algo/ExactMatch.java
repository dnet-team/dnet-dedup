package eu.dnetlib.pace.distance.algo;

import com.wcohen.ss.AbstractStringDistance;
import eu.dnetlib.pace.distance.DistanceClass;
import eu.dnetlib.pace.distance.SecondStringDistanceAlgo;

import java.util.Map;

@DistanceClass("ExactMatch")
public class ExactMatch extends SecondStringDistanceAlgo {

	public ExactMatch(Map<String, Number> params){
		super(params, new com.wcohen.ss.JaroWinkler());
	}

	public ExactMatch(final double weight) {
		super(weight, new com.wcohen.ss.JaroWinkler());
	}

	protected ExactMatch(final double weight, final AbstractStringDistance ssalgo) {
		super(weight, ssalgo);
	}

	@Override
	public double distance(final String a, final String b) {
		return a.equals(b) ? 1.0 : 0;
	}

	@Override
	public double getWeight() {
		return super.weight;
	}

	@Override
	protected double normalize(final double d) {
		return d;
	}

}
