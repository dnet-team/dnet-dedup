package eu.dnetlib.pace.condition;

import java.util.List;

import eu.dnetlib.pace.config.Cond;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldDef;

/**
 * The Class ExactMatch.
 *
 * @author claudio
 */
public class DoiExactMatch extends ExactMatchIgnoreCase {

	public final String PREFIX = "(http:\\/\\/dx\\.doi\\.org\\/)|(doi:)";

	public DoiExactMatch(final Cond cond, final List<FieldDef> fields) {
		super(cond, fields);
	}

	@Override
	protected String getValue(final Field f) {
		return super.getValue(f).replaceAll(PREFIX, "");
	}

}
