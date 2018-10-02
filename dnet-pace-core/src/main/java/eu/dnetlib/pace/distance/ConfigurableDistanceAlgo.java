package eu.dnetlib.pace.distance;

import java.util.Map;

import eu.dnetlib.pace.common.AbstractPaceFunctions;

public abstract class ConfigurableDistanceAlgo extends AbstractPaceFunctions {

	private Map<String, String> params;

	private double weigth;

	public ConfigurableDistanceAlgo(final Map<String, String> params, final double weight) {
		this.params = params;
		this.weigth = weight;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public double getWeigth() {
		return weigth;
	}

}
