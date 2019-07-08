package eu.dnetlib.pace.condition;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import eu.dnetlib.pace.distance.eval.ConditionEval;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldDef;
import eu.dnetlib.pace.model.FieldList;
import eu.dnetlib.pace.model.adaptor.Pid;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The Class PidMatch.
 *
 * @author claudio
 */
@ConditionClass("pidMatch")
public class PidMatch extends AbstractCondition {

	private static final Log log = LogFactory.getLog(PidMatch.class);

	public PidMatch(final String cond, final List<FieldDef> fields) {
		super(cond, fields);
	}

	@Override
	protected ConditionEval verify(final FieldDef fd, final Field a, final Field b) {

		final List<String> sa = ((FieldList) a).stringList();
		final List<String> sb = ((FieldList) b).stringList();

		final List<Pid> pal = Pid.fromOafJson(sa);
		final List<Pid> pbl = Pid.fromOafJson(sb);

		final Set<String> pidAset = toHashSet(pal);
		final Set<String> pidBset = toHashSet(pbl);

		int incommon = Sets.intersection(pidAset, pidBset).size();
		int simDiff = Sets.symmetricDifference(pidAset, pidBset).size();

		if (incommon + simDiff == 0) {
			return new ConditionEval(cond, a, b, 0);
		}

		int result = incommon / (incommon + simDiff) > 0.5 ? 1 : -1;

		return new ConditionEval(cond, a, b, result);
	}

	//lowercase + normalization of the pid before adding it to the set
	private Set<String> toHashSet(List<Pid> pbl) {

		return pbl.stream()
					.map(pid -> pid.getType() + normalizePid(pid.getValue()))
					.collect(Collectors.toCollection(HashSet::new));
	}

}
