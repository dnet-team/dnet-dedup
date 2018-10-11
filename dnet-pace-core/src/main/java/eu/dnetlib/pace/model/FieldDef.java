package eu.dnetlib.pace.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import eu.dnetlib.pace.config.Algo;
import eu.dnetlib.pace.config.Type;
import eu.dnetlib.pace.distance.*;
import eu.dnetlib.pace.distance.algo.*;

/**
 * The schema is composed by field definitions (FieldDef). Each field has a type, a name, and an associated distance algorithm.
 */
public class FieldDef implements Serializable {

	public final static String PATH_SEPARATOR = "/";

	private Algo algo;

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
		switch (getAlgo()) {
		case JaroWinkler:
			return new JaroWinkler(getWeight());
		case JaroWinklerTitle:
			return new JaroWinklerTitle(getWeight());
		case Level2JaroWinkler:
			return new Level2JaroWinkler(getWeight());
		case Level2JaroWinklerTitle:
			return new Level2JaroWinklerTitle(getWeight());
		case Level2Levenstein:
			return new Level2Levenstein(getWeight());
		case Levenstein:
			return new Levenstein(getWeight());
		case LevensteinTitle:
			return new LevensteinTitle(getWeight());
		case SubStringLevenstein:
			return new SubStringLevenstein(getWeight(), getLimit());
		case YearLevenstein:
			return new YearLevenstein(getWeight(), getLimit());
		case SortedJaroWinkler:
			return new SortedJaroWinkler(getWeight());
		case SortedLevel2JaroWinkler:
			return new SortedLevel2JaroWinkler(getWeight());
		case urlMatcher:
			return new UrlMatcher(getWeight(), getParams());
		case ExactMatch:
			return new ExactMatch(getWeight());
		case MustBeDifferent:
			return new MustBeDifferent(getWeight());
		case AlwaysMatch:
			return new AlwaysMatch(getWeight());
		case Null:
			return new NullDistanceAlgo();
		default:
			return new NullDistanceAlgo();
		}
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

	public Algo getAlgo() {
		return algo;
	}

	public void setAlgo(final Algo algo) {
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
