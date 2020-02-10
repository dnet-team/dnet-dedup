package eu.dnetlib.pace.clustering;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.model.Field;
import org.apache.commons.lang3.StringUtils;

@ClusteringClass("lowercase")
public class LowercaseClustering extends AbstractClusteringFunction {

	public LowercaseClustering(final Map<String, Integer> params) {
		super(params);
	}

	@Override
	public Collection<String> apply(Config conf, List<Field> fields) {
		Collection<String> c = Sets.newLinkedHashSet();
		for(Field f : fields) {
			c.addAll(doApply(conf, f.stringValue()));
		}
		return c;
	}

	@Override
	protected Collection<String> doApply(final Config conf, final String s) {
		if(StringUtils.isBlank(s)) {
			return Lists.newArrayList();
		}
		return Lists.newArrayList(s.toLowerCase().trim());
	}
}
