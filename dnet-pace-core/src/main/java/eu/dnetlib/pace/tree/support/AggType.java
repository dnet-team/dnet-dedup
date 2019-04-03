package eu.dnetlib.pace.tree.support;

import eu.dnetlib.pace.util.PaceException;

public enum AggType {

    AVG,
    SUM,
    MAX,
    MIN;

    public static AggType getEnum(String value) {

        try {
            return AggType.valueOf(value);
        }
        catch (IllegalArgumentException e) {
            throw new PaceException("Undefined aggregation type", e);
        }
    }
}
