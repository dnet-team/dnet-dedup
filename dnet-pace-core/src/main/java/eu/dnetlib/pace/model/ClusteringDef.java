package eu.dnetlib.pace.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import eu.dnetlib.pace.clustering.*;
import eu.dnetlib.pace.config.PaceConfig;
import eu.dnetlib.pace.util.PaceException;
import org.codehaus.jackson.map.ObjectMapper;

public class ClusteringDef implements Serializable {

	private String name;

	private List<String> fields;

	private Map<String, Integer> params;

	public ClusteringDef() {}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public ClusteringFunction getClusteringFunction() {
		try {
			return PaceConfig.paceResolver.getClusteringFunction(getName(), params);
		} catch (PaceException e) {
			e.printStackTrace();
			return null;
		}
	}

	public List<String> getFields() {
		return fields;
	}

	public void setFields(final List<String> fields) {
		this.fields = fields;
	}

	public Map<String, Integer> getParams() {
		return params;
	}

	public void setParams(final Map<String, Integer> params) {
		this.params = params;
	}

	@Override
	public String toString() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (IOException e) {
			return e.getStackTrace().toString();
		}
	}

}
