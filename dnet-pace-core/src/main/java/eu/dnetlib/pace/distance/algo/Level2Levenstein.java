package eu.dnetlib.pace.distance.algo;

import com.wcohen.ss.AbstractStringDistance;
import eu.dnetlib.pace.distance.DistanceClass;
import eu.dnetlib.pace.distance.SecondStringDistanceAlgo;

@DistanceClass("Level2Levenstein")
public class Level2Levenstein extends SecondStringDistanceAlgo {

	public Level2Levenstein(double w) {
		super(w, new com.wcohen.ss.Level2Levenstein());
	}

	protected Level2Levenstein(double w, AbstractStringDistance ssalgo) {
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
