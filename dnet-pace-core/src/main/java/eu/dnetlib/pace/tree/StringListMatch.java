package eu.dnetlib.pace.tree;

import com.google.common.collect.Sets;
import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldList;
import eu.dnetlib.pace.tree.support.AbstractComparator;
import eu.dnetlib.pace.tree.support.ComparatorClass;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@ComparatorClass("stringListMatch")
public class StringListMatch extends AbstractComparator {

    private static final Log log = LogFactory.getLog(StringListMatch.class);
    private Map<String, String> params;

    public StringListMatch(final Map<String, String> params) {
        super(params);
        this.params = params;
    }

    @Override
    public double compare(final Field a, final Field b, final Config conf) {

        final Set<String> pa = new HashSet<>(((FieldList) a).stringList());
        final Set<String> pb = new HashSet<>(((FieldList) b).stringList());

        if (pa.isEmpty() || pb.isEmpty()) {
            return -1;  //return undefined if one of the two lists of pids is empty
        }

        int incommon = Sets.intersection(pa, pb).size();
        int simDiff = Sets.symmetricDifference(pa, pb).size();

        if (incommon + simDiff == 0) {
            return 0.0;
        }

        return (double)incommon / (incommon + simDiff) > Double.parseDouble(params.getOrDefault("threshold", "0.5")) ? 1 : 0;

    }
}