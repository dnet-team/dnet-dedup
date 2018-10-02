package eu.dnetlib.pace.model.gt;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.gson.Gson;

public class Author implements Comparable<Author> {

	private String id;
	private String fullname;
	private String firstname;
	private String secondnames;

	private List<Match> matches = Lists.newArrayList();
	private Set<Author> coauthors = Sets.newHashSet();
	private SubjectsMap subjectsMap = new SubjectsMap();

	public Author() {
		super();
	}

	public Author(final Author a) {
		this.id = a.getId();
		this.fullname = a.getFullname();
		this.firstname = a.getFirstname();
		this.secondnames = a.getSecondnames();

		this.matches = a.getMatches();
		this.coauthors = a.getCoauthors();
		this.subjectsMap = a.getSubjectsMap();
	}

	public boolean hasMatches() {
		return (getMatches() != null) && !getMatches().isEmpty();
	}

	public boolean hasCoauthors() {
		return (getCoauthors() != null) && !getCoauthors().isEmpty();
	}

	public boolean isWellFormed() {
		return StringUtils.isNotBlank(getSecondnames()) && StringUtils.isNotBlank(getFirstname());
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(final String fullname) {
		this.fullname = fullname;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(final String firstname) {
		this.firstname = firstname;
	}

	public String getSecondnames() {
		return secondnames;
	}

	public void setSecondnames(final String secondnames) {
		this.secondnames = secondnames;
	}

	public List<Match> getMatches() {
		return matches;
	}

	public void setMatches(final List<Match> matches) {
		this.matches = matches;
	}

	public Set<Author> getCoauthors() {
		return coauthors;
	}

	public void setCoauthors(final Set<Author> coauthors) {
		this.coauthors = coauthors;
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
	public int compareTo(final Author o) {
		return ComparisonChain.start()
				.compare(this.getId(), o.getId(), Ordering.natural().nullsLast())
				.result();
	}

	@Override
	public boolean equals(final Object o) {
		return (o instanceof Author) && getId().equals(((Author) o).getId());
	}

	public SubjectsMap getSubjectsMap() {
		return subjectsMap;
	}

	public void setSubjectsMap(final SubjectsMap subjectsMap) {
		this.subjectsMap = subjectsMap;
	}
}
