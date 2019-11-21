package eu.dnetlib.pace.tree;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Iterables;

import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.tree.support.AbstractComparator;
import eu.dnetlib.pace.tree.support.ComparatorClass;

/**
 * Returns true if the number of values in the fields is the same.
 *
 * @author claudio
 */
@ComparatorClass("sizeMatch")
public class SizeMatch extends AbstractComparator {

    /**
     * Instantiates a new size match.
     *
     * @param params
     *            the parameters
     */
    public SizeMatch(final Map<String, String> params) {
        super(params);
    }

    @Override
    public double compare(final Field a, final Field b, final Config conf) {

        if (a.isEmpty() || b.isEmpty())
            return -1;

        return Iterables.size(a) == Iterables.size(b) ? 1 : 0;
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
