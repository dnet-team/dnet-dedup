package eu.dnetlib.pace.tree.support;

public enum MatchType {

    ORCID_MATCH,
    COAUTHORS_MATCH,
    TOPICS_MATCH,
    NO_MATCH,
    UNDEFINED;

    public static MatchType getEnum(String value) {

        try {
            return MatchType.valueOf(value);
        }
        catch (IllegalArgumentException e) {
            return MatchType.UNDEFINED;
        }
    }
}
