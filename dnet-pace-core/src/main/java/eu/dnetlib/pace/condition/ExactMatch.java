package eu.dnetlib.pace.condition;

import java.util.List;

import eu.dnetlib.pace.config.Cond;
import eu.dnetlib.pace.distance.eval.ConditionEval;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldDef;
import org.apache.commons.lang.StringUtils;

/**
 * The Class ExactMatch.
 *
 * @author claudio
 */
public class ExactMatch extends AbstractCondition {

	public ExactMatch(final Cond cond, final List<FieldDef> fields) {
		super(cond, fields);
	}

	@Override
	protected ConditionEval verify(final FieldDef fd, final Field a, final Field b) {

		final String fa = getValue(a);
		final String fb = getValue(b);

		int res;

		if (StringUtils.isBlank(fa) && StringUtils.isBlank(fb)) {
			res = 0;
		} else {
			res = fa.equals(fb) ? 1 : -1;
		}

		return new ConditionEval(cond, a, b, res);
	}

	protected String getValue(final Field f) {
		return getFirstValue(f);
	}

}
