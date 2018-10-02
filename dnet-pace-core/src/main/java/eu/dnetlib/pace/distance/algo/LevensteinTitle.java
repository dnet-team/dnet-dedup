package eu.dnetlib.pace.distance.algo;

import com.wcohen.ss.AbstractStringDistance;
import eu.dnetlib.pace.distance.SecondStringDistanceAlgo;

public class LevensteinTitle extends SecondStringDistanceAlgo {

	public LevensteinTitle(final double w) {
		super(w, new com.wcohen.ss.Levenstein());
	}

	protected LevensteinTitle(final double w, final AbstractStringDistance ssalgo) {
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

		return normalize(ssalgo.score(cca, ccb), cca.length(), ccb.length());
	}

	private double normalize(final double score, final int la, final int lb) {
		return 1 - (Math.abs(score) / Math.max(la, lb));
	}

	@Override
	public double getWeight() {
		return super.weight;
	}

	@Override
	protected double normalize(final double d) {
		return 1 / Math.pow(Math.abs(d) + 1, 0.1);
	}

}
