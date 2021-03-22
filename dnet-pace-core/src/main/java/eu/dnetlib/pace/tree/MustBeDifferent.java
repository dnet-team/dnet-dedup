package eu.dnetlib.pace.tree;

import com.wcohen.ss.AbstractStringDistance;
import eu.dnetlib.pace.tree.support.AbstractComparator;
import eu.dnetlib.pace.tree.support.ComparatorClass;
import eu.dnetlib.pace.config.Config;

import java.util.Map;

@ComparatorClass("mustBeDifferent")
public class MustBeDifferent extends AbstractComparator {

	public MustBeDifferent(Map<String,String> params){
		super(params, new com.wcohen.ss.Levenstein());
	}

	public MustBeDifferent(final double weight) {
		super(weight, new com.wcohen.ss.JaroWinkler());
	}

	protected MustBeDifferent(final double weight, final AbstractStringDistance ssalgo) {
		super(weight, ssalgo);
	}

	@Override
	public double distance(final String a, final String b, final Config conf) {
		return !a.equals(b) ? 1.0 : 0;
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
