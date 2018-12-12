package eu.dnetlib.pace.tree;

import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldList;

import java.util.List;
import java.util.Map;

@TreeNodeClass("undefined")
public class UndefinedNode implements TreeNode {

    Map<String, Number> params;

    @Override
    public int compare(Field a, Field b) {

        final List<String> sa = ((FieldList) a).stringList();
        final List<String> sb = ((FieldList) b).stringList();

        System.out.println("sa = " + sa.size());
        System.out.println("sb = " + sb.size());

        return 0;
    }
}
