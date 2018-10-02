package eu.dnetlib.pace.model.gt;

import java.util.Set;

import com.google.gson.Gson;

public class CoAuthorSetLite {

	private String id;

	private Set<String> coAuthors;

	public CoAuthorSetLite(final String id, final Set<String> coAuthors) {
		super();
		this.id = id;
		this.coAuthors = coAuthors;
	}

	public Set<String> getCoAuthors() {
		return coAuthors;
	}

	public void setCoAuthors(final Set<String> coAuthors) {
		this.coAuthors = coAuthors;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}
