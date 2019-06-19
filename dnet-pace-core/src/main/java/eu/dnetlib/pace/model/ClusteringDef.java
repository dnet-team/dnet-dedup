package eu.dnetlib.pace.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.dnetlib.pace.clustering.*;
import eu.dnetlib.pace.config.PaceConfig;
import eu.dnetlib.pace.util.PaceException;
import eu.dnetlib.pace.util.PaceResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

	public ClusteringFunction clusteringFunction() {
		return PaceConfig.resolver.getClusteringFunction(getName(), params);
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
			throw new PaceException("unable to serialise " + this.getClass().getName(), e);
		}
	}

}
