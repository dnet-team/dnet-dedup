package eu.dnetlib.pace.model.gt;

import java.util.List;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.google.gson.Gson;

public class Result implements Comparable<Result> {

	private String id;
	private String originalId;
	private String title;
	private List<Author> authors;

	private double meanDistance;

	public Result() {}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getOriginalId() {
		return originalId;
	}

	public void setOriginalId(final String originalId) {
		this.originalId = originalId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public List<Author> getAuthors() {
		return authors;
	}

	public void setAuthors(final List<Author> authors) {
		this.authors = authors;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

	@Override
	public int compareTo(final Result o) {
		return ComparisonChain.start()
				.compare(this.getAuthors().size(), o.getAuthors().size(), Ordering.natural().nullsLast())
				.result();
	}

	public double getMeanDistance() {
		return meanDistance;
	}

	public void setMeanDistance(final double meanDistance) {
		this.meanDistance = meanDistance;
	}

}
