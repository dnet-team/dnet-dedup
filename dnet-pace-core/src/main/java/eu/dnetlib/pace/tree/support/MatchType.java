package eu.dnetlib.pace.tree.support;

import eu.dnetlib.pace.util.PaceException;

public enum MatchType {

    ORCID_MATCH,
    COAUTHORS_MATCH,
    TOPICS_MATCH,
    NO_MATCH;

    public static MatchType getEnum(String value) {

        try {
            return MatchType.valueOf(value);
        }
        catch (IllegalArgumentException e) {
            throw new PaceException("The match type is not valid");
        }
    }
}
