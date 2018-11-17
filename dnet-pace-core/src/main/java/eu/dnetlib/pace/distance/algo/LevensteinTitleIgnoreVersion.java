package eu.dnetlib.pace.distance.algo;

import com.wcohen.ss.AbstractStringDistance;
import eu.dnetlib.pace.distance.DistanceClass;
import eu.dnetlib.pace.distance.SecondStringDistanceAlgo;

import java.util.Map;

/**
 * Compared distance between two titles, ignoring version numbers. Suitable for Software entities.
 */
@DistanceClass("LevensteinTitleIgnoreVersion")
public class LevensteinTitleIgnoreVersion extends SecondStringDistanceAlgo {

	public LevensteinTitleIgnoreVersion(Map<String,Number> params){
		super(params, new com.wcohen.ss.Levenstein());
	}

	public LevensteinTitleIgnoreVersion(final double w) {
		super(w, new com.wcohen.ss.Levenstein());
	}

	protected LevensteinTitleIgnoreVersion(final double w, final AbstractStringDistance ssalgo) {
		super(w, ssalgo);
	}

	@Override
	public double distance(final String a, final String b) {
		String ca = cleanup(a);
		String cb = cleanup(b);

		ca = ca.replaceAll("\\d", "").replaceAll(getRomans(ca), "").trim();
		cb = cb.replaceAll("\\d", "").replaceAll(getRomans(cb), "").trim();

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
