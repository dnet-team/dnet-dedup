package eu.dnetlib.pace.condition;

import java.time.Year;
import java.util.List;

import eu.dnetlib.pace.distance.eval.ConditionEval;
import org.apache.commons.lang.StringUtils;

import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldDef;

/**
 * Returns true if the year of the date field in the given documents are the same, false when any of the two is invalid or it's missing.
 *
 * @author claudio
 */
@ConditionClass("yearMatch")
public class YearMatch extends AbstractCondition {

	private int limit = 4;

	public YearMatch(final String cond, final List<FieldDef> fields) {
		super(cond, fields);
	}

	public YearMatch(){}

	// @Override
	// public boolean verify(final Document a, final Document b) {
	// boolean res = true;
	// for (FieldDef fd : getFields()) {
	//
	// }
	//
	// return res;
	// }

	@Override
	protected ConditionEval verify(final FieldDef fd, final Field a, final Field b) {
		final String valueA = getNumbers(getFirstValue(a));
		final String valueB = getNumbers(getFirstValue(b));

		final boolean lengthMatch = checkLength(valueA) && checkLength(valueB);
		final boolean onemissing = valueA.isEmpty() || valueB.isEmpty();

		return new ConditionEval(cond, a, b, lengthMatch && valueA.equals(valueB) || onemissing ? 1 : -1);
	}

	protected boolean checkLength(final String s) {
		return s.length() == limit;
	}

	protected String getFirstValue(final Field value) {
		return (value != null) && !value.isEmpty() ? StringUtils.left(value.stringValue(), limit) : "";
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ":" + super.toString();
	}

}
