package eu.dnetlib.pace.condition;

import java.util.List;

import eu.dnetlib.pace.distance.eval.ConditionEval;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldDef;

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
	protected ConditionEval verify(final FieldDef fd, final Field a, final Field b) {

		final String fa = getValue(a);
		final String fb = getValue(b);

		return new ConditionEval(cond, a, b, fa.equalsIgnoreCase(fb) ? 1 : -1);
	}

	protected String getValue(final Field f) {
		return getFirstValue(f);
	}

}
