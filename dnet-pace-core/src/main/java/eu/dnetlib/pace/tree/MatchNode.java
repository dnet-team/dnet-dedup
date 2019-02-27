package eu.dnetlib.pace.tree;

import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.tree.support.AbstractCondition;
import eu.dnetlib.pace.tree.support.ComparatorClass;

import java.util.Map;

@ComparatorClass("matchNode")
public class MatchNode extends AbstractCondition {

    public MatchNode(final Map<String,Number> params) {
        super(params);
    }

    @Override
    public double compare(final Field a, final Field b) {
        return 1.0;
    }

}
