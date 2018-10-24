package eu.dnetlib.pace.condition;

import java.util.List;

import eu.dnetlib.pace.distance.eval.ConditionEval;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldDef;

/**
 * Returns true if the titles in the given documents contains the same numbers, false otherwise.
 *
 * @author claudio
 *
 */
@ConditionClass("titleVersionMatch")
public class TitleVersionMatch extends AbstractCondition {

	public TitleVersionMatch(final String cond, final List<FieldDef> fields) {
		super(cond, fields);
	}

	@Override
	protected ConditionEval verify(final FieldDef fd, final Field a, final Field b) {
		final String valueA = getFirstValue(a);
		final String valueB = getFirstValue(b);

		return new ConditionEval(cond, a, b, notNull(valueA) && notNull(valueB) && !checkNumbers(valueA, valueB) ? 1 : -1);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ":" + super.toString();
	}

}
