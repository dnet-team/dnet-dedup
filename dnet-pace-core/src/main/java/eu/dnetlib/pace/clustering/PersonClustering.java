package eu.dnetlib.pace.clustering;

import com.google.common.collect.Sets;
import eu.dnetlib.pace.common.AbstractPaceFunctions;
import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.Person;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ClusteringClass("personClustering")
public class PersonClustering extends AbstractPaceFunctions implements ClusteringFunction {

	private Map<String, Integer> params;

	private static final int MAX_TOKENS = 5;

	public PersonClustering(final Map<String, Integer> params) {
		this.params = params;
	}

	@Override
	public Collection<String> apply(final Config conf, final List<Field> fields) {
		final Set<String> hashes = Sets.newHashSet();

		for (final Field f : fields) {

			final Person person = new Person(f.stringValue(), false);

			if (StringUtils.isNotBlank(person.getNormalisedFirstName()) && StringUtils.isNotBlank(person.getNormalisedSurname())) {
				hashes.add(firstLC(person.getNormalisedFirstName()) + person.getNormalisedSurname().toLowerCase());
			} else {
				for (final String token1 : tokens(f.stringValue(), MAX_TOKENS)) {
					for (final String token2 : tokens(f.stringValue(), MAX_TOKENS)) {
						if (!token1.equals(token2)) {
							hashes.add(firstLC(token1) + token2);
						}
					}
				}
			}
		}

		return hashes;
	}

//	@Override
//	public Collection<String> apply(final List<Field> fields) {
//		final Set<String> hashes = Sets.newHashSet();
//
//		for (final Field f : fields) {
//
//			final GTAuthor gta = GTAuthor.fromOafJson(f.stringValue());
//
//			final Author a = gta.getAuthor();
//
//			if (StringUtils.isNotBlank(a.getFirstname()) && StringUtils.isNotBlank(a.getSecondnames())) {
//				hashes.add(firstLC(a.getFirstname()) + a.getSecondnames().toLowerCase());
//			} else {
//				for (final String token1 : tokens(f.stringValue(), MAX_TOKENS)) {
//					for (final String token2 : tokens(f.stringValue(), MAX_TOKENS)) {
//						if (!token1.equals(token2)) {
//							hashes.add(firstLC(token1) + token2);
//						}
//					}
//				}
//			}
//		}
//
//		return hashes;
//	}

	@Override
	public Map<String, Integer> getParams() {
		return params;
	}

}
