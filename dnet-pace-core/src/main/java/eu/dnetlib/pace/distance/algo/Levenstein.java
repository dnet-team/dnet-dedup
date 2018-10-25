package eu.dnetlib.pace.distance.algo;

import com.wcohen.ss.AbstractStringDistance;
import eu.dnetlib.pace.distance.DistanceClass;
import eu.dnetlib.pace.distance.SecondStringDistanceAlgo;

import java.util.Map;

@DistanceClass("Levenstein")
public class Levenstein extends SecondStringDistanceAlgo {

	public Levenstein(Map<String,Number> params){
		super(params, new com.wcohen.ss.Levenstein());
	}

	public Levenstein(double w) {
		super(w, new com.wcohen.ss.Levenstein());
	}

	protected Levenstein(double w, AbstractStringDistance ssalgo) {
		super(w, ssalgo);
	}

	@Override
	public double getWeight() {
		return super.weight;
	}

	@Override
	protected double normalize(double d) {
		return 1 / Math.pow(Math.abs(d) + 1, 0.1);
	}

}
