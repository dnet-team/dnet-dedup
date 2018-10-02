package eu.dnetlib.pace.model.gt;

import com.google.gson.Gson;

public class ScoredResult extends Result {

	private double meanDistance;

	public ScoredResult() {
		super();
	}

	public double getMeanDistance() {
		return meanDistance;
	}

	public void setMeanDistance(final double meanDistance) {
		this.meanDistance = meanDistance;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}
