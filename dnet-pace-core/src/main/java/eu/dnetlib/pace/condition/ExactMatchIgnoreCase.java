package eu.dnetlib.pace.condition;

import java.util.List;

import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.distance.eval.ConditionEval;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldDef;
import org.apache.commons.lang.StringUtils;

/**
 * The Class ExactMatch.
 *
 * @author claudio
 */
@ConditionClass("exactMatchIgnoreCase")
public class ExactMatchIgnoreCase extends AbstractCondition {

	public ExactMatchIgnoreCase(final String cond, final List<FieldDef> fields) {
		super(cond, fields);
	}

	@Override
	protected ConditionEval verify(final FieldDef fd, final Field a, final Field b, final Config conf) {

		final String fa = getValue(a);
		final String fb = getValue(b);

		int res;

		if (StringUtils.isBlank(fa) || StringUtils.isBlank(fb)) {
			res = 0;
		} else {
			res = fa.equalsIgnoreCase(fb) ? 1 : -1;
		}

		return new ConditionEval(cond, a, b, res);
	}

	protected String getValue(final Field f) {
		return getFirstValue(f);
	}

}
