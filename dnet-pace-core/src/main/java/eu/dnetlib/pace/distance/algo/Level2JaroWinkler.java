package eu.dnetlib.pace.distance.algo;

import com.wcohen.ss.AbstractStringDistance;
import eu.dnetlib.pace.distance.SecondStringDistanceAlgo;

public class Level2JaroWinkler extends SecondStringDistanceAlgo {

	public Level2JaroWinkler(double w) {
		super(w, new com.wcohen.ss.Level2JaroWinkler());
	}

	protected Level2JaroWinkler(double w, AbstractStringDistance ssalgo) {
		super(w, ssalgo);
	}

	@Override
	public double getWeight() {
		return super.weight;
	}

	@Override
	protected double normalize(double d) {
		return d;
	}

}
