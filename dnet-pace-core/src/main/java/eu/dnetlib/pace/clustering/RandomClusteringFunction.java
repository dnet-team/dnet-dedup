package eu.dnetlib.pace.clustering;

import eu.dnetlib.pace.config.Config;

import java.util.Collection;
import java.util.Map;

public class RandomClusteringFunction extends AbstractClusteringFunction {

	public RandomClusteringFunction(Map<String, Integer> params) {
		super(params);
	}

	@Override
	protected Collection<String> doApply(final Config conf, String s) {
		return null;
	}

}
