package eu.dnetlib.pace.model;

import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import eu.dnetlib.pace.clustering.*;

public class ClusteringDef {

	private Clustering name;

	private List<String> fields;

	private Map<String, Integer> params;

	public ClusteringDef() {}

	public Clustering getName() {
		return name;
	}

	public void setName(final Clustering name) {
		this.name = name;
	}

	public ClusteringFunction getClusteringFunction() {
		switch (getName()) {
		case acronyms:
			return new Acronyms(getParams());
		case ngrams:
			return new Ngrams(getParams());
		case ngrampairs:
			return new NgramPairs(getParams());
		case sortedngrampairs:
			return new SortedNgramPairs(getParams());
		case suffixprefix:
			return new SuffixPrefix(getParams());
		case spacetrimmingfieldvalue:
			return new SpaceTrimmingFieldValue(getParams());
		case immutablefieldvalue:
			return new ImmutableFieldValue(getParams());
		case personhash:
			return new PersonHash(getParams());
		case personclustering:
			return new PersonClustering(getParams());
		case lowercase:
			return new LowercaseClustering(getParams());
		case urlclustering:
			return new UrlClustering(getParams());
		default:
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
