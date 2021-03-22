package eu.dnetlib.pace.tree;

import java.util.List;
import java.util.Map;

import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.tree.support.AbstractComparator;
import eu.dnetlib.pace.tree.support.ComparatorClass;

/**
 * Returns true if the titles in the given documents contains the same numbers, false otherwise.
 *
 * @author claudio
 *
 */
@ComparatorClass("titleVersionMatch")
public class TitleVersionMatch extends AbstractComparator {

    public TitleVersionMatch(final Map<String, String> params) {
        super(params);
    }

    @Override
    public double compare(final Field a, final Field b, final Config conf) {
        final String valueA = getFirstValue(a);
        final String valueB = getFirstValue(b);

        if (valueA.isEmpty() || valueB.isEmpty())
            return -1;

        return notNull(valueA) && notNull(valueB) && !checkNumbers(valueA, valueB) ? 1 : 0;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + super.toString();
    }

}