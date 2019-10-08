package eu.dnetlib.pace.distance.algo;

import com.wcohen.ss.AbstractStringDistance;
import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.distance.DistanceClass;
import eu.dnetlib.pace.distance.DistanceScorer;
import eu.dnetlib.pace.distance.SecondStringDistanceAlgo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

@DistanceClass("LevensteinTitle")
public class LevensteinTitle extends SecondStringDistanceAlgo {

	private static final Log log = LogFactory.getLog(LevensteinTitle.class);

	public LevensteinTitle(Map<String,Number> params){
		super(params, new com.wcohen.ss.Levenstein());
	}

	public LevensteinTitle(final double w) {
		super(w, new com.wcohen.ss.Levenstein());
	}

	protected LevensteinTitle(final double w, final AbstractStringDistance ssalgo) {
		super(w, ssalgo);
	}

	@Override
	public double distance(final String a, final String b, final Config conf) {
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
