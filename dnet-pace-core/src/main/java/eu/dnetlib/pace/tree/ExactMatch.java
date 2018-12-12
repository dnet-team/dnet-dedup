package eu.dnetlib.pace.tree;

import eu.dnetlib.pace.model.Field;

import java.util.Map;

@TreeNodeClass("exactMatch")
public class ExactMatch extends AbstractTreeNode {

    public ExactMatch(Map<String, Number> params) {
        super(params);
    }

    @Override
    public int compare(Field a, Field b) {

        if (a.stringValue().isEmpty() || b.stringValue().isEmpty())
            return 0;
        else if (a.stringValue().equals(b.stringValue()))
            return 1;
        else
            return -1;
    }

}
