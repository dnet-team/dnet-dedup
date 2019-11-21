package eu.dnetlib.pace.tree;

import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.tree.support.AbstractComparator;
import eu.dnetlib.pace.tree.support.ComparatorClass;

import java.util.Map;

@ComparatorClass("exactMatchIgnoreCase")
public class ExactMatchIgnoreCase extends AbstractComparator {

    public ExactMatchIgnoreCase(Map<String, String> params) {
        super(params);
    }

    @Override
    public double compare(Field a, Field b, final Config conf) {

        final String fa = getValue(a);
        final String fb = getValue(b);

        if (fa.isEmpty() || fb.isEmpty())
            return -1;

        return fa.equalsIgnoreCase(fb) ? 1 : 0;
    }

    protected String getValue(final Field f) {
        return getFirstValue(f);
    }
}