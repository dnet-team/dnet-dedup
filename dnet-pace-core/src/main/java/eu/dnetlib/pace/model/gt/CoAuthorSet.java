package eu.dnetlib.pace.model.gt;

import com.google.gson.Gson;

public class CoAuthorSet {

	private Author author;
	private Authors coAuthors;

	public CoAuthorSet(final Author author, final Authors coAuthors) {
		super();
		this.author = author;
		this.coAuthors = coAuthors;
	}

	public Author getAuthor() {
		return author;
	}

	public void setAuthor(final Author author) {
		this.author = author;
	}

	public Authors getCoAuthors() {
		return coAuthors;
	}

	public void setCoAuthors(final Authors coAuthors) {
		this.coAuthors = coAuthors;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}
}
