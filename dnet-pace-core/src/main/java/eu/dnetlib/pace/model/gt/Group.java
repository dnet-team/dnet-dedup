package eu.dnetlib.pace.model.gt;

import java.util.List;

import com.google.gson.Gson;

public class Group {

	private String id;
	private int size;
	private List<Result> results;

	public Group() {}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public int getSize() {
		return size;
	}

	public void setSize(final int size) {
		this.size = size;
	}

	public List<Result> getResults() {
		return results;
	}

	public void setResults(final List<Result> results) {
		this.results = results;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}
