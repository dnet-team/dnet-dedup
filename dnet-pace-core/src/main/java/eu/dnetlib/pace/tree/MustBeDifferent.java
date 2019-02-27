package eu.dnetlib.pace.tree;

import com.google.common.collect.Iterables;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.tree.support.AbstractCondition;
import eu.dnetlib.pace.tree.support.ComparatorClass;

import java.util.Map;

@ComparatorClass("mustBeDifferent")
public class MustBeDifferent extends AbstractCondition {

    public MustBeDifferent(final Map<String, Number> params) {
        super(params);
    }

    @Override
    public double compare(final Field a, final Field b) {

        final String fa = getValue(a);
        final String fb = getValue(b);

        if (fa.isEmpty() || fb.isEmpty())
            return -1;

        return fa.equals(fb)? 0:1;
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
