package eu.dnetlib.pace.model;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import eu.dnetlib.pace.config.PaceConfig;
import eu.dnetlib.pace.config.Type;
import eu.dnetlib.pace.distance.*;
import eu.dnetlib.pace.distance.algo.*;
import eu.dnetlib.pace.util.PaceException;

/**
 * The schema is composed by field definitions (FieldDef). Each field has a type, a name, and an associated distance algorithm.
 */
public class FieldDef implements Serializable {

	public final static String PATH_SEPARATOR = "/";

	private String algo;

	private String name;

	private String path;

	private boolean ignoreMissing;

	private Type type;

	private boolean overrideMatch;

	private double weight;

	private int limit = -1;

	private Map<String, Number> params;

	public FieldDef() {}

	// def apply(s: String): Field[A]
	public Field apply(final Type type, final String s) {
		switch (type) {
		case Int:
			return new FieldValueImpl(type, name, Integer.parseInt(s));
		case String:
			return new FieldValueImpl(type, name, s);
		case List:
			return new FieldListImpl(name, type);
		default:
			throw new IllegalArgumentException("Casting not implemented for type " + type);
		}
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	public List<String> getPathList() {
		return Lists.newArrayList(Splitter.on(PATH_SEPARATOR).split(getPath()));
	}

	public DistanceAlgo getDistanceAlgo() {

		if (params == null) {
			params = new HashMap<>();
		}
		params.put("limit", getLimit());
		params.put("weight", getWeight());
		return PaceConfig.paceResolver.getDistanceAlgo(getAlgo(), params);
	}

	public boolean isIgnoreMissing() {
		return ignoreMissing;
	}

	public Type getType() {
		return type;
	}

	public void setType(final Type type) {
		this.type = type;
	}

	public boolean isOverrideMatch() {
		return overrideMatch;
	}

	public void setOverrideMatch(final boolean overrideMatch) {
		this.overrideMatch = overrideMatch;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(final double weight) {
		this.weight = weight;
	}

	public String getAlgo() {
		return algo;
	}

	public void setAlgo(final String algo) {
		this.algo = algo;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(final int limit) {
		this.limit = limit;
	}

	public Map<String, Number> getParams() {
		return params;
	}

	public void setParams(final Map<String, Number> params) {
		this.params = params;
	}

}
