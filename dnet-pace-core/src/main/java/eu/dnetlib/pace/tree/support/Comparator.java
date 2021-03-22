package eu.dnetlib.pace.tree.support;

import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.model.Field;

public interface Comparator {

    /*
    * return : -1 -> can't decide (i.e. missing field)
    *          >0 -> similarity degree (depends on the algorithm)
    * */
    public double compare(Field a, Field b, Config conf);

}
