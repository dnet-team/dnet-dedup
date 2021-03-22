package eu.dnetlib.pace.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import eu.dnetlib.pace.config.Type;

import java.io.Serializable;
import java.util.List;

/**
 * The schema is composed by field definitions (FieldDef). Each field has a type, a name, and an associated compare algorithm.
 */
public class FieldDef implements Serializable {

	public final static String PATH_SEPARATOR = "/";

	private String name;

	private String path;

	private Type type;

	private boolean overrideMatch;

	/**
	 * Sets maximum size for the repeatable fields in the model. -1 for unbounded size.
	 */
	private int size = -1;

	/**
	 * Sets maximum length for field values in the model. -1 for unbounded length.
	 */
	private int length = -1;

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

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String toString() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (JsonProcessingException e) {
			return null;
		}
	}

}
