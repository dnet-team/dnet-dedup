package eu.dnetlib.pace.condition;

import java.util.List;

import com.google.common.collect.Iterables;
import eu.dnetlib.pace.config.Cond;
import eu.dnetlib.pace.distance.eval.ConditionEval;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldDef;

/**
 * Returns true if the field values are different.
 *
 * @author claudio
 */
public class MustBeDifferent extends AbstractCondition {

	/**
	 * Instantiates a new size match.
	 *
	 * @param fields the fields
	 */
	public MustBeDifferent(final Cond cond, final List<FieldDef> fields) {
		super(cond, fields);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see eu.dnetlib.pace.condition.AbstractCondition#verify(eu.dnetlib.pace.model.FieldDef, java.util.List, java.util.List)
	 */
	@Override
	protected ConditionEval verify(final FieldDef fd, final Field a, final Field b) {

		final String fa = getValue(a);
		final String fb = getValue(b);

		return new ConditionEval(cond, a, b, fa.equals(fb) ? -1 : 1);

	}

	protected String getValue(final Field f) {
		return getFirstValue(f);
	}

	/**
	 * Checks if is empty.
	 *
	 * @param a the a
	 * @return true, if is empty
	 */
	protected boolean isEmpty(final Iterable<?> a) {
		return (a == null) || Iterables.isEmpty(a);
	}

}
