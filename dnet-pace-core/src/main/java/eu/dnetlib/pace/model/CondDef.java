package eu.dnetlib.pace.model;

import java.io.Serializable;
import java.util.List;

import com.google.gson.Gson;
import eu.dnetlib.pace.condition.*;

public class CondDef implements Serializable {

	private String name;

	private List<String> fields;

	private ConditionResolver conditionResolver = new ConditionResolver();

	public CondDef() {}

	public ConditionAlgo getConditionAlgo(final List<FieldDef> fields) {

		try {
			ConditionAlgo conditionAlgo = conditionResolver.resolve(getName());
			conditionAlgo.setFields(fields);
			conditionAlgo.setCond(getName());
			return conditionAlgo;
		} catch (IllegalAccessException | InstantiationException e) {
			e.printStackTrace();
			return new AlwaysTrueCondition(getName(), fields);
		}

	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
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
