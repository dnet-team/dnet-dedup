package eu.dnetlib.pace.tree;

import com.wcohen.ss.AbstractStringDistance;
import eu.dnetlib.pace.distance.DistanceClass;
import eu.dnetlib.pace.distance.SecondStringDistanceAlgo;
import eu.dnetlib.pace.tree.support.AbstractComparator;
import eu.dnetlib.pace.tree.support.ComparatorClass;

import java.util.Map;

/**
 * Compared compare between two titles, ignoring version numbers. Suitable for Software entities.
 */
@ComparatorClass("levensteinTitleIgnoreVersion")
public class LevensteinTitleIgnoreVersion extends AbstractComparator {

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

		ca = filterAllStopWords(ca);
		cb = filterAllStopWords(cb);

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
