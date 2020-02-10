package eu.dnetlib.pace.tree;

import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.tree.support.AbstractComparator;
import eu.dnetlib.pace.tree.support.ComparatorClass;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Returns true if the year of the date field in the given documents are the same, false when any of the two is invalid or it's missing.
 *
 * @author claudio
 */
@ComparatorClass("yearMatch")
public class YearMatch extends AbstractComparator {

    private int limit = 4;

    public YearMatch(final Map<String, String> params) {
        super(params);
    }

    @Override
    public double compare(final Field a, final Field b, final Config conf) {
        final String valueA = getNumbers(getFirstValue(a));
        final String valueB = getNumbers(getFirstValue(b));

        if (valueA.isEmpty() || valueB.isEmpty())
            return -1;

        final boolean lengthMatch = checkLength(valueA) && checkLength(valueB);
        final boolean onemissing = valueA.isEmpty() || valueB.isEmpty();

        return lengthMatch && valueA.equals(valueB) || onemissing ? 1 : 0;
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