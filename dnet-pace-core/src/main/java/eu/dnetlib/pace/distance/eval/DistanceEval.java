package eu.dnetlib.pace.distance.eval;

import eu.dnetlib.pace.config.Algo;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldDef;

/**
 * Created by claudio on 09/03/16.
 */
public class DistanceEval {

	private FieldDef fieldDef;

	private Field a;

	private Field b;

	private double distance = 0.0;

	public DistanceEval(final FieldDef fieldDef, final Field a, final Field b) {
		this.fieldDef = fieldDef;
		this.a = a;
		this.b = b;
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

	public FieldDef getFieldDef() {
		return fieldDef;
	}

	public void setFieldDef(final FieldDef fieldDef) {
		this.fieldDef = fieldDef;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(final double distance) {
		this.distance = distance;
	}
}
