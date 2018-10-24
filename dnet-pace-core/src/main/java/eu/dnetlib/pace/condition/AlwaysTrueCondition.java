package eu.dnetlib.pace.condition;

import java.util.List;
import eu.dnetlib.pace.distance.eval.ConditionEval;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldDef;

/**
 * Default always true condition
 *
 * @author claudio
 */
@ConditionClass("alwaystruecondition")
public class AlwaysTrueCondition extends AbstractCondition {

	public AlwaysTrueCondition(final String cond, final List<FieldDef> fields) {
		super(cond, fields);
	}

	public AlwaysTrueCondition(){
		super();
	}
	@Override
	protected ConditionEval verify(final FieldDef fd, final Field a, final Field b) {
		return new ConditionEval(cond, a, b, 1);
	}

}
