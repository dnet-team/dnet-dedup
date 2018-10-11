package eu.dnetlib.pace.model;

import java.io.Serializable;
import java.util.List;

import com.google.gson.Gson;
import eu.dnetlib.pace.condition.*;
import eu.dnetlib.pace.config.Cond;

public class CondDef implements Serializable {

	private Cond name;

	private List<String> fields;

	public CondDef() {}

	public ConditionAlgo getConditionAlgo(final List<FieldDef> fields) {
		switch (getName()) {
		case yearMatch:
			return new YearMatch(getName(), fields);
		case titleVersionMatch:
			return new TitleVersionMatch(getName(), fields);
		case sizeMatch:
			return new SizeMatch(getName(), fields);
		case exactMatch:
			return new ExactMatch(getName(), fields);
		case mustBeDifferent:
			return new MustBeDifferent(getName(), fields);
		case exactMatchIgnoreCase:
			return new ExactMatchIgnoreCase(getName(), fields);
		case doiExactMatch:
			return new DoiExactMatch(getName(), fields);
		case pidMatch:
			return new PidMatch(getName(), fields);
		default:
			return new AlwaysTrueCondition(getName(), fields);
		}
	}

	public Cond getName() {
		return name;
	}

	public void setName(final Cond name) {
		this.name = name;
	}

	public List<String> getFields() {
		return fields;
	}

	public void setFields(final List<String> fields) {
		this.fields = fields;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}
