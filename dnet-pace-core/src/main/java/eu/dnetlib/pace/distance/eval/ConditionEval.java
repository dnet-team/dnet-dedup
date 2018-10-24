package eu.dnetlib.pace.distance.eval;

import eu.dnetlib.pace.model.Field;

/**
 * Created by claudio on 09/03/16.
 */
public class ConditionEval {

	private String cond;

	private Field a;

	private Field b;

	private int result;

	public ConditionEval(final String cond, final Field a, final Field b, final int result) {
		this.cond = cond;
		this.a = a;
		this.b = b;
		this.result = result;
	}

	public Field getA() {
		return a;
	}

	public void setA(final Field a) {
		this.a = a;
	}

	public Field getB() {
		return b;
	}

	public void setB(final Field b) {
		this.b = b;
	}

	public int getResult() {
		return result;
	}

	public void setResult(final int result) {
		this.result = result;
	}

	public String getCond() {
		return cond;
	}

	public void setCond(final String cond) {
		this.cond = cond;
	}
}
