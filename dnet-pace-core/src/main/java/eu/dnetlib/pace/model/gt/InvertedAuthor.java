package eu.dnetlib.pace.model.gt;

import java.util.Collection;

import com.google.gson.Gson;

public class InvertedAuthor {

	private Author author;
	private Collection<String> ids;

	public InvertedAuthor() {}

	public InvertedAuthor(final Author author, final Collection<String> ids) {
		super();
		this.author = author;
		this.ids = ids;
	}

	public Author getAuthor() {
		return author;
	}

	public void setAuthor(final Author author) {
		this.author = author;
	}

	public Collection<String> getIds() {
		return ids;
	}

	public void setIds(final Collection<String> ids) {
		this.ids = ids;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}
