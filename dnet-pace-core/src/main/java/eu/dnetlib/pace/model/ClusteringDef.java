package eu.dnetlib.pace.model;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import eu.dnetlib.pace.clustering.*;

public class ClusteringDef implements Serializable {

	private String name;

	private List<String> fields;

	private Map<String, Integer> params;

	private ClusteringResolver clusteringResolver = new ClusteringResolver();

	public ClusteringDef() {}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public ClusteringFunction getClusteringFunction() {

		try {
			return clusteringResolver.resolve(getName(), params);
		} catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
			e.printStackTrace();
			return new RandomClusteringFunction(getParams());
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
		return new Gson().toJson(this);
	}

}
