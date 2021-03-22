package eu.dnetlib.pace.tree.support;

import eu.dnetlib.pace.util.PaceException;

public enum AggType {

    W_MEAN, //weighted mean
    AVG,    //average
    SUM,
    MAX,
    MIN,
    AND,    //used for necessary conditions
    OR;     //used for sufficient conditions

    public static AggType getEnum(String value) {

        try {
            return AggType.valueOf(value);
        }
        catch (IllegalArgumentException e) {
            throw new PaceException("Undefined aggregation type", e);
        }
    }
}
