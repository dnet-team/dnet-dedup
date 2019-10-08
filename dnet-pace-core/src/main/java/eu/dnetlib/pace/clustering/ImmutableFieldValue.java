package eu.dnetlib.pace.clustering;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import eu.dnetlib.pace.config.Config;

@ClusteringClass("immutablefieldvalue")
public class ImmutableFieldValue extends AbstractClusteringFunction {

	public ImmutableFieldValue(final Map<String, Integer> params) {
		super(params);
	}

	@Override
	protected Collection<String> doApply(final Config conf, final String s) {
		final List<String> res = Lists.newArrayList();

		res.add(s);

		return res;
	}

}
