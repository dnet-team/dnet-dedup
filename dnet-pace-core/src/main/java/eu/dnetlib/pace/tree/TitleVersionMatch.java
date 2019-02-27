package eu.dnetlib.pace.tree;

import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.tree.support.AbstractCondition;
import eu.dnetlib.pace.tree.support.ComparatorClass;

import java.util.Map;

@ComparatorClass("titleVersionMatch")
public class TitleVersionMatch extends AbstractCondition {

    public TitleVersionMatch(final Map<String, Number> params) {
        super(params);
    }

    @Override
    public double compare(final Field a, final Field b) {
        final String valueA = getFirstValue(a);
        final String valueB = getFirstValue(b);

        return notNull(valueA) && notNull(valueB) && !checkNumbers(valueA, valueB) ? 1:-1;
        //if no numbers in the title return 0?

    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + super.toString();
    }

}
