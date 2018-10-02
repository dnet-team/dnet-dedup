package eu.dnetlib.pace.model.gt;

import com.google.gson.Gson;

public class AuthorSet {

	private String id;
	private Authors authors;

	public AuthorSet(final String id, final Authors authors) {
		super();
		this.id = id;
		this.authors = authors;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public Authors getAuthors() {
		return authors;
	}

	public void setAuthors(final Authors authors) {
		this.authors = authors;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}
