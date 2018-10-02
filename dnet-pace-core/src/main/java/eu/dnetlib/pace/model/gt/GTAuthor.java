package eu.dnetlib.pace.model.gt;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.dnetlib.pace.model.adaptor.PidOafSerialiser;

public class GTAuthor implements Comparable<GTAuthor> {

	private String id;
	private Author author;
	private Authors merged;
	private CoAuthors coAuthors;
	private boolean anchor;

	public GTAuthor() {}

	public GTAuthor(final String id, final Authors merged, final CoAuthors coAuthors, final boolean anchor) {
		super();

		if ((merged == null) || merged.isEmpty())
			throw new IllegalArgumentException("empty merged author set, id: " + id);

		this.author = pickAuthor(merged);
		this.id = id;
		this.merged = merged;
		this.coAuthors = coAuthors;
		this.anchor = anchor;
	}

	class AuthorFrequency extends Author {

		private Integer frequency = new Integer(1);

		public AuthorFrequency(final Author a) {
			super(a);
		}

		public void increment() {
			setFrequency(getFrequency() + 1);
		}

		public Integer getFrequency() {
			return frequency;
		}

		public void setFrequency(final Integer frequency) {
			this.frequency = frequency;
		}
	}

	private Author pickAuthor(final Authors merged) {
		final List<AuthorFrequency> freq = getFrequencies(merged);
		Collections.sort(freq, Collections.reverseOrder(new Comparator<AuthorFrequency>() {

			@Override
			public int compare(final AuthorFrequency o1, final AuthorFrequency o2) {
				return ComparisonChain.start().compare(o1.getFullname().length(), o2.getFullname().length()).compare(o1.getFrequency(), o2.getFrequency())
						.result();
			}
		}));

		return Iterables.getFirst(freq, null);
	}

	private List<AuthorFrequency> getFrequencies(final Authors merged) {
		final Map<String, Integer> countMap = Maps.newHashMap();
		for (final Author a : merged) {
			final Integer count = countMap.get(a.getFullname());
			if (count == null) {
				countMap.put(a.getFullname(), new Integer(1));
			} else {
				countMap.put(a.getFullname(), count + 1);
			}
		}

		return Lists.newArrayList(Iterables.transform(merged, new Function<Author, AuthorFrequency>() {

			@Override
			public AuthorFrequency apply(final Author a) {
				final AuthorFrequency af = new AuthorFrequency(a);
				final Integer freq = countMap.get(af.getFullname());
				af.setFrequency(freq);
				return af;
			}
		}));
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public Author getAuthor() {
		return author;
	}

	public void setAuthor(final Author author) {
		this.author = author;
	}

	public boolean hasMerged() {
		return (getMerged() != null) && !getMerged().isEmpty();
	}

	public Authors getMerged() {
		return merged;
	}

	public void setMerged(final Authors merged) {
		this.merged = merged;
	}

	public boolean hasCoAuthors() {
		return (getCoAuthors() != null) && !getCoAuthors().isEmpty();
	}

	public CoAuthors getCoAuthors() {
		return coAuthors;
	}

	public void setCoAuthors(final CoAuthors coAuthors) {
		this.coAuthors = coAuthors;
	}

	public boolean isAnchor() {
		return anchor;
	}

	public void setAnchor(final boolean anchor) {
		this.anchor = anchor;
	}

	public static GTAuthor fromJson(final String json) {
		final Gson gson = new Gson();
		return gson.fromJson(json, GTAuthor.class);
	}

	public static List<GTAuthor> fromOafJson(final List<String> json) {

		final GsonBuilder gb = new GsonBuilder();
		gb.registerTypeAdapter(GTAuthor.class, new GTAuthorOafSerialiser());
		final Gson gson = gb.create();

		return Lists.newArrayList(Iterables.transform(json, new Function<String, GTAuthor>() {
			@Override
			public GTAuthor apply(final String s) {
				return gson.fromJson(s, GTAuthor.class);
			}
		}));
	}

	public static GTAuthor fromOafJson(final String json) {

		final GsonBuilder gb = new GsonBuilder();
		gb.registerTypeAdapter(GTAuthor.class, new GTAuthorOafSerialiser());
		final Gson gson = gb.create();

		return gson.fromJson(json, GTAuthor.class);
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

	@Override
	public int hashCode() {
		return getId().hashCode();
	}

	@Override
	public int compareTo(final GTAuthor o) {
		return ComparisonChain.start()
				.compare(this.getId(), o.getId(), Ordering.natural().nullsLast())
				.result();
	}

	@Override
	public boolean equals(final Object o) {
		return (o instanceof GTAuthor) && getId().equals(((GTAuthor) o).getId());
	}

}
