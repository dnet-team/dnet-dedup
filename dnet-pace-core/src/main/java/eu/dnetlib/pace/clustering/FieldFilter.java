package eu.dnetlib.pace.clustering;

import java.util.List;
import java.util.Map;

import com.google.common.base.Predicate;

import eu.dnetlib.pace.model.Field;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FieldFilter implements Predicate<Field> {

	private static final Log log = LogFactory.getLog(FieldFilter.class);

	private Map<String, List<String>> blacklists;

	private String filedName;

	public FieldFilter(final String fieldName, final Map<String, List<String>> blacklists) {
		this.filedName = fieldName;
		this.blacklists = blacklists;
	}

	@Override
	public boolean apply(final Field f) {
		return !regexMatches(filedName, f.stringValue(), blacklists);
	}

	/**
	 * Tries to match the fields in the regex blacklist.
	 *
	 * @param fieldName
	 * @param value
	 * @return true if the field matches, false otherwise
	 */
	protected boolean regexMatches(final String fieldName, final String value, final Map<String, List<String>> blacklists) {
		if (blacklists.containsKey(fieldName)) {
			final Iterable<String> regexes = blacklists.get(fieldName);
			for (final String regex : regexes) {
				if (StringUtils.isBlank(regex)) return false;
				if (value.matches(regex)) return true;
			}
		}
		return false;
	}
}
