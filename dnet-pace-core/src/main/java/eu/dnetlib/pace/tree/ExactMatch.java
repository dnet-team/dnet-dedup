package eu.dnetlib.pace.tree;

import eu.dnetlib.pace.model.Field;

import java.util.Map;

@ComparatorClass("exactMatch")
public class ExactMatch extends AbstractComparator {

    public ExactMatch(Map<String, Number> params) {
        super(params);
    }

    @Override
    public double compare(Field a, Field b) {

        if (a.stringValue().isEmpty() || b.stringValue().isEmpty())
            return -1;
        else if (a.stringValue().equals(b.stringValue()))
            return 1;
        else
            return 0;
    }

}
