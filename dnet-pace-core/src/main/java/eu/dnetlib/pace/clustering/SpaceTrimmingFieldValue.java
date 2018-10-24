package eu.dnetlib.pace.clustering;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;

@ClusteringClass("spacetrimmingfieldvalue")
public class SpaceTrimmingFieldValue extends AbstractClusteringFunction {

	public SpaceTrimmingFieldValue(final Map<String, Integer> params) {
		super(params);
	}

	public SpaceTrimmingFieldValue(){
		super();
	}

	@Override
	protected Collection<String> doApply(final String s) {
		final List<String> res = Lists.newArrayList();

		res.add(StringUtils.isBlank(s) ? RandomStringUtils.random(getParams().get("randomLength")) : s.toLowerCase().replaceAll("\\s+", ""));

		return res;
	}

}
