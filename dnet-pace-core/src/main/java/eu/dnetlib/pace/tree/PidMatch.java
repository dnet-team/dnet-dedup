package eu.dnetlib.pace.tree;

import com.google.common.collect.Sets;
import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldList;
import eu.dnetlib.pace.model.adaptor.Pid;
import eu.dnetlib.pace.tree.support.AbstractComparator;
import eu.dnetlib.pace.tree.support.ComparatorClass;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ComparatorClass("pidMatch")
public class PidMatch extends AbstractComparator {

    private static final Log log = LogFactory.getLog(PidMatch.class);
    private Map<String, Number> params;

    public PidMatch(final Map<String, Number> params) {
        super(params);
        this.params = params;
    }

    @Override
    public double compare(final Field a, final Field b, final Config conf) {

        final List<String> sa = ((FieldList) a).stringList();
        final List<String> sb = ((FieldList) b).stringList();

        final List<Pid> pal = Pid.fromOafJson(sa);
        final List<Pid> pbl = Pid.fromOafJson(sb);

        if (pal.isEmpty() || pbl.isEmpty()) {
            return -1;
        }

        final Set<String> pidAset = toHashSet(pal);
        final Set<String> pidBset = toHashSet(pbl);

        int incommon = Sets.intersection(pidAset, pidBset).size();
        int simDiff = Sets.symmetricDifference(pidAset, pidBset).size();

        if (incommon + simDiff == 0) {
            return 0.0;
        }

        return (double)incommon / (incommon + simDiff) > params.getOrDefault("threshold", 0.5).doubleValue() ? 1 : 0;

    }

    //lowercase + normalization of the pid before adding it to the set
    private Set<String> toHashSet(List<Pid> pbl) {

        return pbl.stream()
                .map(pid -> pid.getType() + normalizePid(pid.getValue()))
                .collect(Collectors.toCollection(HashSet::new));
    }
}
