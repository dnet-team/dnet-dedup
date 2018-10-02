package eu.dnetlib.pace.distance.algo;


public class LevensteinDate extends Levenstein {


	public LevensteinDate(double w) {
		super(w);
	}

	
	@Override
	public double distance(String a, String b) {

		return 1.0;
	}
	

	
	@Override
	public double getWeight() {
		return super.weight;
	}

}
