package eu.dnetlib.pace.model;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import eu.dnetlib.pace.config.Type;

import java.io.Serializable;
import java.util.List;

/**
 * The schema is composed by field definitions (FieldDef). Each field has a type, a name, and an associated distance algorithm.
 */
public class FieldDef implements Serializable {

	public final static String PATH_SEPARATOR = "/";

	private String name;

	private String path;

	private Type type;

	private boolean overrideMatch;

	private int limit = -1;

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

	public int getLimit() {
		return limit;
	}

	public void setLimit(final int limit) {
		this.limit = limit;
	}

}
