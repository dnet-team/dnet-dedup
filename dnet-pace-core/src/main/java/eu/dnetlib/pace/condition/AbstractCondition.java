package eu.dnetlib.pace.condition;

import java.util.List;
import eu.dnetlib.pace.common.AbstractPaceFunctions;
import eu.dnetlib.pace.distance.eval.ConditionEval;
import eu.dnetlib.pace.distance.eval.ConditionEvalMap;
import eu.dnetlib.pace.model.Document;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldDef;

/**
 * Abstract necessaryConditions needs a list of field names.
 *
 * @author claudio
 *
 */
public abstract class AbstractCondition extends AbstractPaceFunctions implements ConditionAlgo {

	protected String cond;

	protected List<FieldDef> fields;

	public AbstractCondition(final String cond, final List<FieldDef> fields) {
		this.cond = cond;
		this.fields = fields;
	}

	protected abstract ConditionEval verify(FieldDef fd, Field a, Field b);

	@Override
	public ConditionEvalMap verify(final Document a, final Document b) {
		final ConditionEvalMap res = new ConditionEvalMap();
		for (final FieldDef fd : getFields()) {

			final Field va = a.values(fd.getName());
			final Field vb = b.values(fd.getName());

			if (fd.isIgnoreMissing()) {
				res.put(fd.getName(), verify(fd, va, vb));
			} else {
				if (va.isEmpty() || vb.isEmpty()) {
					res.put(fd.getName(), new ConditionEval(cond, va, vb, -1));
				} else {
					res.put(fd.getName(), verify(fd, va, vb));
				}
			}
		}
		return res;
	}

	public List<FieldDef> getFields() {
		return fields;
	}

}
