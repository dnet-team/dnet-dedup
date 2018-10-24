package eu.dnetlib.pace.distance.eval;

import com.google.gson.GsonBuilder;

import java.io.Serializable;

/**
 * Created by claudio on 09/03/16.
 */
public class ScoreResult implements Serializable {

	private ConditionEvalMap strictConditions;

	private ConditionEvalMap conditions;

	private DistanceEvalMap distances;

	public double getScore() {

		if (getStrictConditions().result() > 0) return 1.0;
	//	if (getStrictConditions().result() < 0) return 0.0;
		if (getConditions().result() < 0) return 0.0;

		return getDistances().distance();
	}


	public ConditionEvalMap getStrictConditions() {
		return strictConditions;
	}

	public void setStrictConditions(final ConditionEvalMap strictConditions) {
		this.strictConditions = strictConditions;
	}

	public ConditionEvalMap getConditions() {
		return conditions;
	}

	public void setConditions(final ConditionEvalMap conditions) {
		this.conditions = conditions;
	}

	public DistanceEvalMap getDistances() {
		return distances;
	}

	public void setDistances(final DistanceEvalMap distances) {
		this.distances = distances;
	}

	@Override
	public String toString() {
		//TODO cannot print: why?
//		final GsonBuilder b = new GsonBuilder()
//			.serializeSpecialFloatingPointValues()
//				.serializeNulls();
//
//		return b.setPrettyPrinting().create().toJson(this);
		return "{}";
	}
}
