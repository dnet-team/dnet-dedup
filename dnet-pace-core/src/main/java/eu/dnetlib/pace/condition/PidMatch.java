package eu.dnetlib.pace.condition;

import java.util.List;

import eu.dnetlib.pace.config.Cond;
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
public class PidMatch extends AbstractCondition {

	private static final Log log = LogFactory.getLog(PidMatch.class);

	public PidMatch(final Cond cond, final List<FieldDef> fields) {
		super(cond, fields);
	}

	@Override
	protected ConditionEval verify(final FieldDef fd, final Field a, final Field b) {

		final List<String> sa = ((FieldList) a).stringList();
		final List<String> sb = ((FieldList) b).stringList();

		final List<Pid> pal = Pid.fromOafJson(sa);
		final List<Pid> pbl = Pid.fromOafJson(sb);

		int result = 0;
		for(Pid pa : pal) {
			final String ta = pa.getType();

			for(Pid pb : pbl) {
				final String tb = pb.getType();

				if (tb.equalsIgnoreCase(ta)) {
					result += pa.getValue().equalsIgnoreCase(pb.getValue()) ? 1 : -1;
				}
			}
		}

		return new ConditionEval(cond, a, b, result);
	}

}
