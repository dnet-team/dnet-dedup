package eu.dnetlib.pace.tree.support;

import eu.dnetlib.pace.model.Field;

public interface Comparator {

    /*
    * return : -1 -> can't decide (missing field)
    *          >0 -> similarity degree (depends on the algorithm)
    * */
    public double compare(Field a, Field b);

}
