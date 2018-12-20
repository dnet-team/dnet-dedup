package eu.dnetlib.pace.tree;

import eu.dnetlib.pace.model.Field;

public interface Comparator {

    //compare two fields and returns: the distace measure, -1 if undefined
    public double compare(Field a, Field b);

}
