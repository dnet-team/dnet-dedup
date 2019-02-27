package eu.dnetlib.pace.tree;

import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.tree.support.AbstractCondition;
import eu.dnetlib.pace.tree.support.ComparatorClass;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

@ComparatorClass("yearMatch")
public class YearMatch extends AbstractCondition {

    private int limit = 4;

    public YearMatch(final Map<String, Number> params) {
        super(params);
    }

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
    public double compare(final Field a, final Field b) {
        final String valueA = getNumbers(getFirstValue(a));
        final String valueB = getNumbers(getFirstValue(b));

        final boolean lengthMatch = checkLength(valueA) && checkLength(valueB);
        final boolean onemissing = valueA.isEmpty() || valueB.isEmpty();

        return lengthMatch && valueA.equals(valueB) || onemissing? 1:0;
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
