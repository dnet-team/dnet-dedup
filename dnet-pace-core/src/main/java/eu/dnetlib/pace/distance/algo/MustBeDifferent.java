package eu.dnetlib.pace.distance.algo;

import com.wcohen.ss.AbstractStringDistance;
import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.distance.DistanceClass;
import eu.dnetlib.pace.distance.SecondStringDistanceAlgo;

import java.util.Map;

@DistanceClass("MustBeDifferent")
public class MustBeDifferent extends SecondStringDistanceAlgo {

	public MustBeDifferent(Map<String,Number> params){
		super(params, new com.wcohen.ss.Levenstein());
	}

	public MustBeDifferent(final double weight) {
		super(weight, new com.wcohen.ss.JaroWinkler());
	}

	protected MustBeDifferent(final double weight, final AbstractStringDistance ssalgo) {
		super(weight, ssalgo);
	}

	@Override
	public double distance(final String a, final String b, final Config conf) {
		return !a.equals(b) ? 1.0 : 0;
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
