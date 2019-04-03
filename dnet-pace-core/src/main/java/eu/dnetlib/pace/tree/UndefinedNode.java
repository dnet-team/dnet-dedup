package eu.dnetlib.pace.tree;

import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldList;

import java.util.List;
import java.util.Map;

@ComparatorClass("undefined")
public class UndefinedNode implements Comparator {

    Map<String, Number> params;

    @Override
    public double compare(Field a, Field b) {

        final List<String> sa = ((FieldList) a).stringList();
        final List<String> sb = ((FieldList) b).stringList();

        return 0;
    }
}
