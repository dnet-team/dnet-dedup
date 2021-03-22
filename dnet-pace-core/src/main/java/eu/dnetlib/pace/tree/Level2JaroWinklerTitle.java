package eu.dnetlib.pace.tree;

import com.wcohen.ss.AbstractStringDistance;
import eu.dnetlib.pace.tree.support.AbstractComparator;
import eu.dnetlib.pace.tree.support.ComparatorClass;
import eu.dnetlib.pace.config.Config;

import java.util.Map;

@ComparatorClass("level2JaroWinklerTitle")
public class Level2JaroWinklerTitle extends AbstractComparator {

	public Level2JaroWinklerTitle(Map<String,String> params){
		super(params, new com.wcohen.ss.Level2JaroWinkler());
	}

	public Level2JaroWinklerTitle(final double w) {
		super(w, new com.wcohen.ss.Level2JaroWinkler());
	}

	protected Level2JaroWinklerTitle(final double w, final AbstractStringDistance ssalgo) {
		super(w, ssalgo);
	}

	@Override
	public double distance(final String a, final String b, final Config conf) {
		final String ca = cleanup(a);
		final String cb = cleanup(b);

		final boolean check = checkNumbers(ca, cb);

		if (check) return 0.5;

		return ssalgo.score(ca, cb);
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
