package eu.dnetlib.pace.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import eu.dnetlib.pace.condition.*;
import eu.dnetlib.pace.config.PaceConfig;
import eu.dnetlib.pace.util.PaceException;
import eu.dnetlib.pace.util.PaceResolver;
import org.codehaus.jackson.map.ObjectMapper;

public class CondDef implements Serializable {

	private String name;

	private List<String> fields;

	PaceResolver paceResolver = new PaceResolver();

	public CondDef() {}

	public ConditionAlgo conditionAlgo(final List<FieldDef> fields){
		return paceResolver.getConditionAlgo(getName(), fields);
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
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (IOException e) {
			throw new PaceException("unable to serialise " + this.getClass().getName(), e);
		}
	}

}
