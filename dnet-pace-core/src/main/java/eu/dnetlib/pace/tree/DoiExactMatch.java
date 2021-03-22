package eu.dnetlib.pace.tree;

import java.util.Map;

import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.tree.support.ComparatorClass;

/**
 * The Class ExactMatch.
 *
 * @author claudio
 */
@ComparatorClass("doiExactMatch")
public class DoiExactMatch extends ExactMatchIgnoreCase {

    public final String PREFIX = "(http:\\/\\/dx\\.doi\\.org\\/)|(doi:)";

    public DoiExactMatch(final Map<String, String> params) {
        super(params);
    }

    @Override
    protected String getValue(final Field f) {
        return super.getValue(f).replaceAll(PREFIX, "");
    }

}
