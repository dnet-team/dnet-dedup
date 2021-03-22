package eu.dnetlib.pace.tree;

import com.wcohen.ss.AbstractStringDistance;
import eu.dnetlib.pace.tree.support.AbstractComparator;
import eu.dnetlib.pace.tree.support.ComparatorClass;
import eu.dnetlib.pace.config.Config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

@ComparatorClass("levensteinTitle")
public class LevensteinTitle extends AbstractComparator {

	private static final Log log = LogFactory.getLog(LevensteinTitle.class);

	public LevensteinTitle(Map<String,String> params){
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

		return normalize(ssalgo.score(ca, cb), ca.length(), cb.length());
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
