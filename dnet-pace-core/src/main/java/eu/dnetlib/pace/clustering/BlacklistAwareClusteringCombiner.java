package eu.dnetlib.pace.clustering;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.model.Document;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldListImpl;
import eu.dnetlib.pace.model.MapDocument;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BlacklistAwareClusteringCombiner extends ClusteringCombiner {

	private static final Log log = LogFactory.getLog(BlacklistAwareClusteringCombiner.class);

	public static Collection<String> filterAndCombine(final MapDocument a, final Config conf, final Map<String, List<String>> blacklists) {

		final Document filtered = new BlacklistAwareClusteringCombiner().filter(a, blacklists);
		return combine(filtered, conf);
	}

	private MapDocument filter(final MapDocument a, final Map<String, List<String>> blacklists) {
		final Map<String, Field> filtered = Maps.newHashMap(a.getFieldMap());
		if (blacklists != null) {
			for (final Entry<String, Field> e : filtered.entrySet()) {

				final FieldListImpl fl = new FieldListImpl();
				fl.addAll(Lists.newArrayList(Iterables.filter(e.getValue(), new FieldFilter(e.getKey(), blacklists))));
				filtered.put(e.getKey(), fl);
			}
		}
		return new MapDocument(a.getIdentifier(), filtered);
	}

	/**
	 * Tries to match the fields in the regex blacklist.
	 *
	 * @param fieldName
	 * @param value
	 * @return true if the field matches, false otherwise
	 */
	protected boolean regexMatches(final String fieldName, final String value, final Map<String, Set<String>> blacklists) {
		if (blacklists.containsKey(fieldName)) {
			for (final String regex : blacklists.get(fieldName)) {
				if (value.matches(regex)) return true;
			}
		}
		return false;
	}
}
