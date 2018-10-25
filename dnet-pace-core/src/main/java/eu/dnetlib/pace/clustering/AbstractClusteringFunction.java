package eu.dnetlib.pace.clustering;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import eu.dnetlib.pace.common.AbstractPaceFunctions;
import eu.dnetlib.pace.model.Field;
import org.apache.commons.lang.StringUtils;

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
				.map(s -> filterStopWords(s, stopwords))
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
