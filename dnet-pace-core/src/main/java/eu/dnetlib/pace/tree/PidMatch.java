package eu.dnetlib.pace.tree;


import com.google.common.collect.Sets;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldList;
import eu.dnetlib.pace.model.adaptor.Pid;
import eu.dnetlib.pace.tree.support.AbstractCondition;
import eu.dnetlib.pace.tree.support.ComparatorClass;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ComparatorClass("pidMatch")
public class PidMatch extends AbstractCondition {
    private static final Log log = LogFactory.getLog(PidMatch.class);

    public PidMatch(final Map<String, Number> params) {
        super(params);
    }

    @Override
    public double compare(final Field a, final Field b) {

        final List<String> sa = ((FieldList) a).stringList();
        final List<String> sb = ((FieldList) b).stringList();

        final List<Pid> pal = Pid.fromOafJson(sa);
        final List<Pid> pbl = Pid.fromOafJson(sb);

        if (pal.isEmpty() || pbl.isEmpty())
            return -1;

        final Set<String> pidAset = toHashSet(pal);
        final Set<String> pidBset = toHashSet(pbl);

        int incommon = Sets.intersection(pidAset, pidBset).size();
        int simDiff = Sets.symmetricDifference(pidAset, pidBset).size();

        return incommon / (incommon + simDiff) > 0.5 ? 1 : 0;

    }

    private Set<String> toHashSet(List<Pid> pbl) {
        return pbl.stream()
                .map(pid -> pid.getType() + pid.getValue())
                .collect(Collectors.toCollection(HashSet::new));
    }

}
