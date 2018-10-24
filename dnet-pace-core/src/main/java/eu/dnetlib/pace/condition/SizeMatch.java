package eu.dnetlib.pace.condition;

import java.util.List;

import com.google.common.collect.Iterables;

import eu.dnetlib.pace.distance.eval.ConditionEval;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldDef;

/**
 * Returns true if the number of values in the fields is the same.
 *
 * @author claudio
 */
@ConditionClass("sizeMatch")
public class SizeMatch extends AbstractCondition {

	/**
	 * Instantiates a new size match.
	 *
	 * @param fields
	 *            the fields
	 */
	public SizeMatch(final String cond, final List<FieldDef> fields) {
		super(cond, fields);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see eu.dnetlib.pace.condition.AbstractCondition#verify(eu.dnetlib.pace.model.FieldDef, java.util.List, java.util.List)
	 */
	@Override
	protected ConditionEval verify(final FieldDef fd, final Field a, final Field b) {

		// if (a.isEmpty() & b.isEmpty()) return 1;
		//
		// if (a.isEmpty()) return -1;
		// if (b.isEmpty()) return -1;

		return new ConditionEval(cond, a, b, Iterables.size(a) == Iterables.size(b) ? 1 : -1);
	}

	/**
	 * Checks if is empty.
	 *
	 * @param a
	 *            the a
	 * @return true, if is empty
	 */
	protected boolean isEmpty(final Iterable<?> a) {
		return (a == null) || Iterables.isEmpty(a);
	}

}
