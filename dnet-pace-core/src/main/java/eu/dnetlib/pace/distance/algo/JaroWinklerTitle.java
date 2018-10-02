package eu.dnetlib.pace.distance.algo;

import com.wcohen.ss.AbstractStringDistance;
import eu.dnetlib.pace.distance.SecondStringDistanceAlgo;

//case class JaroWinkler(w: Double) extends SecondStringDistanceAlgo(w, new com.wcohen.ss.JaroWinkler())
public class JaroWinklerTitle extends SecondStringDistanceAlgo {

	public JaroWinklerTitle(double weight) {
		super(weight, new com.wcohen.ss.JaroWinkler());
	}

	protected JaroWinklerTitle(double weight, AbstractStringDistance ssalgo) {
		super(weight, ssalgo);
	}
	
	@Override
	public double distance(String a, String b) {
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
