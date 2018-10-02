package eu.dnetlib.pace.clustering;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.dnetlib.pace.model.FieldList;
import eu.dnetlib.pace.model.FieldValue;
import org.apache.commons.lang.StringUtils;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import eu.dnetlib.pace.common.AbstractPaceFunctions;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.gt.Author;
import eu.dnetlib.pace.model.gt.GTAuthor;

public class PersonClustering extends AbstractPaceFunctions implements ClusteringFunction {

	private Map<String, Integer> params;

	private static final int MAX_TOKENS = 5;

	public PersonClustering(final Map<String, Integer> params) {
		this.params = params;
	}

	@Override
	public Collection<String> apply(final List<Field> fields) {
		final Set<String> hashes = Sets.newHashSet();

		for (final Field f : fields) {

			final GTAuthor gta = GTAuthor.fromOafJson(f.stringValue());

			final Author a = gta.getAuthor();
			if (a.isWellFormed()) {
				hashes.add(firstLC(a.getFirstname()) + a.getSecondnames().toLowerCase());
			} else {
				for (final String token1 : tokens(a.getFullname())) {
					for (final String token2 : tokens(a.getFullname())) {
						if (!token1.equals(token2)) {
							hashes.add(firstLC(token1) + token2);
						}
					}
				}
			}
		}

		return hashes;
	}

	private String firstLC(final String s) {
		return StringUtils.substring(s, 0, 1).toLowerCase();
	}

	private Iterable<String> tokens(final String s) {
		return Iterables.limit(Splitter.on(" ").omitEmptyStrings().trimResults().split(s), MAX_TOKENS);
	}

	@Override
	public Map<String, Integer> getParams() {
		return params;
	}

}
