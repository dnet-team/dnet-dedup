package eu.dnetlib.pace.tree;

import com.wcohen.ss.AbstractStringDistance;
import eu.dnetlib.pace.tree.support.AbstractComparator;
import eu.dnetlib.pace.tree.support.ComparatorClass;

import eu.dnetlib.pace.config.Config;

import java.util.Map;

//case class JaroWinkler(w: Double) extends SecondStringDistanceAlgo(w, new com.wcohen.ss.JaroWinkler())
@ComparatorClass("jaroWinklerTitle")
public class JaroWinklerTitle extends AbstractComparator {

	public JaroWinklerTitle(Map<String, String> params){
		super(params, new com.wcohen.ss.JaroWinkler());
	}

	public JaroWinklerTitle(double weight) {
		super(weight, new com.wcohen.ss.JaroWinkler());
	}

	protected JaroWinklerTitle(double weight, AbstractStringDistance ssalgo) {
		super(weight, ssalgo);
	}
	
	@Override
	public double distance(String a, String b, final Config conf) {
		String ca = cleanup(a);
		String cb = cleanup(b);

		boolean check = checkNumbers(ca, cb);
		return check ? 0.5 : normalize(ssalgo.score(ca, cb));
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
