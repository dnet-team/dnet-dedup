package eu.dnetlib.pace.distance.algo;

import com.wcohen.ss.AbstractStringDistance;
import eu.dnetlib.pace.distance.DistanceClass;
import eu.dnetlib.pace.distance.SecondStringDistanceAlgo;

@DistanceClass("Level2JaroWinklerTitle")
public class Level2JaroWinklerTitle extends SecondStringDistanceAlgo {

	public Level2JaroWinklerTitle(final double w) {
		super(w, new com.wcohen.ss.Level2JaroWinkler());
	}

	protected Level2JaroWinklerTitle(final double w, final AbstractStringDistance ssalgo) {
		super(w, ssalgo);
	}

	@Override
	public double distance(final String a, final String b) {
		final String ca = cleanup(a);
		final String cb = cleanup(b);

		final boolean check = checkNumbers(ca, cb);

		if (check) return 0.5;

		final String cca = finalCleanup(ca);
		final String ccb = finalCleanup(cb);

		return ssalgo.score(cca, ccb);
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
