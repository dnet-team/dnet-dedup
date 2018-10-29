package eu.dnetlib.pace.model.gt;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

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
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (IOException e) {
			return e.getStackTrace().toString();
		}
	}

}
