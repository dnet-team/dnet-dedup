package eu.dnetlib.pace.model.gt;

import java.util.Collection;
import java.util.HashSet;

import com.google.common.base.Function;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.gson.Gson;

public class CoAuthors extends HashSet<CoAuthor> implements Comparable<CoAuthors> {

	private static final long serialVersionUID = 2525591524516562892L;

	private Function<CoAuthors, Integer> hashFunction;

	private static Function<CoAuthors, Integer> defaultHashFunction = new Function<CoAuthors, Integer>() {

		@Override
		public Integer apply(final CoAuthors input) {
			int res = 0;
			for (final CoAuthor a : input) {
				res += a.hashCode();
			}
			return res;

		}
	};

	public CoAuthors() {
		super();
	}

	public CoAuthors(final Collection<CoAuthor> coauthors) {
		super(coauthors);
	}

	public CoAuthors(final CoAuthor coauthor) {
		super(Sets.newHashSet(coauthor));
	}

	public Function<CoAuthors, Integer> getHashFunction() {
		return hashFunction;
	}

	public void setHashFunction(final Function<CoAuthors, Integer> hashFunction) {
		this.hashFunction = hashFunction;
	}

	@Override
	public int compareTo(final CoAuthors a) {
		return ComparisonChain.start()
				.compare(this.size(), a.size(), Ordering.natural().nullsLast())
				.result();
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

	@Override
	public boolean equals(final Object o) {
		final boolean res = o instanceof CoAuthors;
		return res && (Sets.intersection(this, (CoAuthors) o).size() == this.size());
	}

	public String hashCodeString() {
		return String.valueOf(hashCode());
	}

	@Override
	public int hashCode() {
		return (getHashFunction() != null) ? getHashFunction().apply(this) : defaultHashFunction.apply(this);
	}

}
