package eu.dnetlib.pace.clustering;

import eu.dnetlib.pace.common.AbstractPaceFunctions;
import eu.dnetlib.pace.model.Field;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractClusteringFunction extends AbstractPaceFunctions implements ClusteringFunction {

	protected Map<String, Integer> params;
	
	public AbstractClusteringFunction(final Map<String, Integer> params) {
		this.params = params;
	}

	protected abstract Collection<String> doApply(String s);
	
	@Override
	public Collection<String> apply(List<Field> fields) {
		return fields.stream().filter(f -> !f.isEmpty())
				.map(Field::stringValue)
				.map(this::normalize)
				.map(s -> filterAllStopWords(s))
				.map(this::doApply)
				.map(c -> filterBlacklisted(c, ngramBlacklist))
				.flatMap(c -> c.stream())
				.filter(StringUtils::isNotBlank)
				.collect(Collectors.toCollection(HashSet::new));
	}

	public Map<String, Integer> getParams() {
		return params;
	}
	
	protected Integer param(String name) {
		return params.get(name);
	}
}
