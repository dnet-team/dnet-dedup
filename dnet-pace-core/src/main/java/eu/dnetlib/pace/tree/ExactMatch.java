package eu.dnetlib.pace.tree;

import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.tree.support.AbstractCondition;
import eu.dnetlib.pace.tree.support.ComparatorClass;

import java.util.Map;

@ComparatorClass("exactMatch")
public class ExactMatch extends AbstractCondition {

    public ExactMatch(Map<String, Number> params) {
        super(params);
    }

    @Override
    public double compare(Field a, Field b) {

        if (a.stringValue().isEmpty() || b.stringValue().isEmpty())
            return -1;

        return (a.stringValue().equals(b.stringValue()))? 1:0;
    }

}
