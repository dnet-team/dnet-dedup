package eu.dnetlib.pace.clustering;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

public class ImmutableFieldValue extends AbstractClusteringFunction {

	public ImmutableFieldValue(final Map<String, Integer> params) {
		super(params);
	}

	@Override
	protected Collection<String> doApply(final String s) {
		final List<String> res = Lists.newArrayList();

		res.add(s);

		return res;
	}

}
