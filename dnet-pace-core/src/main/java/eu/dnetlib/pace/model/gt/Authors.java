package eu.dnetlib.pace.model.gt;

import java.util.Collection;
import java.util.HashSet;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.gson.Gson;

public class Authors extends HashSet<Author> implements Comparable<Authors> {

	private static final long serialVersionUID = -6878376220805286142L;

	public Authors() {
		super();
	}

	public Authors(final Collection<Author> authors) {
		super(authors);
	}

	public Authors(final Author author) {
		super(Sets.newHashSet(author));
	}

	@Override
	public int compareTo(final Authors a) {
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
		final boolean res = o instanceof Authors;
		return res && (Sets.intersection(this, (Authors) o).size() == this.size());
	}

	@Override
	public int hashCode() {
		int res = 0;
		for (final Author a : this) {
			res += a.hashCode();
		}
		return res;
	}

}
