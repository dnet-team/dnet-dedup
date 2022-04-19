package eu.dnetlib.pace.clustering;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.model.Person;

@ClusteringClass("personHash")
public class PersonHash extends AbstractClusteringFunction {

	private boolean DEFAULT_AGGRESSIVE = false;

	public PersonHash(final Map<String, Integer> params) {
		super(params);
	}

	@Override
	protected Collection<String> doApply(final Config conf, final String s) {
		final List<String> res = Lists.newArrayList();

		final boolean aggressive = (Boolean) (getParams().containsKey("aggressive") ? getParams().get("aggressive") : DEFAULT_AGGRESSIVE);

		res.add(new Person(s, aggressive).hash());

		return res;
	}

}
