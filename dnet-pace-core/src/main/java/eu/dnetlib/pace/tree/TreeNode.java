package eu.dnetlib.pace.tree;

import eu.dnetlib.pace.model.Field;

public interface TreeNode {

    //compare two fields and returns: +1 if match, 0 if undefined, -1 if do not match
    public int compare(Field a, Field b);

}
