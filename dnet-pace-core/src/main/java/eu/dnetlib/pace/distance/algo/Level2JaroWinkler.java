package eu.dnetlib.pace.distance.algo;

import com.wcohen.ss.AbstractStringDistance;
import eu.dnetlib.pace.distance.DistanceClass;
import eu.dnetlib.pace.distance.SecondStringDistanceAlgo;

@DistanceClass("Level2JaroWinkler")
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
