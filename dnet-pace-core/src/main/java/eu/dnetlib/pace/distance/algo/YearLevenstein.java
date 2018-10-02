package eu.dnetlib.pace.distance.algo;


public class YearLevenstein extends SubStringLevenstein {

	public YearLevenstein(double w) {
		super(w);
	}

	public YearLevenstein(double w, int limit) {
		super(w, limit);	
	}
	
	@Override
	public double distance(String a, String b) {
		boolean check = checkLength(a) && checkLength(b);
		if (check) {
			if (a.equals(b)) {
				return 1.0;
			} else {
				return 0.5;
			}
		} else {
			return 1.0;
		}
	}
	
	protected boolean checkLength(String s) {
		return getNumbers(s).length() == limit;
	}
	
	@Override
	public double getWeight() {
		return super.weight;
	}

}
