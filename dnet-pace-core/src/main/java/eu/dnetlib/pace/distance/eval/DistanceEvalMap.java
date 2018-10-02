package eu.dnetlib.pace.distance.eval;

import java.util.HashMap;

/**
 * Created by claudio on 10/03/16.
 */
public class DistanceEvalMap extends HashMap<String, DistanceEval> {

	private double sumWeights;

	private double sumDistances = 0.0;

	public DistanceEvalMap(final double sumWeights) {
		this.sumWeights = sumWeights;
	}

	public void updateDistance(final DistanceEval d) {

		put(d.getFieldDef().getName(), d);
		if (d.getDistance() >= 0) {
			sumDistances += d.getDistance();
		} else {
			sumWeights -= d.getFieldDef().getWeight();
		}
	}

	public double distance() {
		return sumWeights == 0 ? 0 : sumDistances / sumWeights;
	}

}
