package eu.dnetlib.pace.tree;

import com.wcohen.ss.AbstractStringDistance;
import eu.dnetlib.pace.tree.support.AbstractComparator;
import eu.dnetlib.pace.tree.support.ComparatorClass;

import java.util.Map;

@ComparatorClass("level2JaroWinkler")
public class Level2JaroWinkler extends AbstractComparator {

	public Level2JaroWinkler(Map<String, String> params){
		super(params, new com.wcohen.ss.Level2JaroWinkler());
	}

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
