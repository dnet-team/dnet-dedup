package eu.dnetlib.pace.config;

/**
 * Enumerates the distance Algos.
 */
public enum Algo {

	/** The Jaro winkler. */
	JaroWinkler,
	/** The Jaro winkler title. */
	JaroWinklerTitle,
	/** The Levenstein. */
	Levenstein,
	/** The Levenstein distance for title matching */
	LevensteinTitle,
	/** The Level2 jaro winkler. */
	Level2JaroWinkler,
	/** The Level2 jaro winkler for title matching */
	Level2JaroWinklerTitle,
	/** The Level2 levenstein. */
	Level2Levenstein,
	/** The Sub string levenstein. */
	SubStringLevenstein,
	/** The Year levenstein. */
	YearLevenstein,
	/** The Sorted jaro winkler. */
	SortedJaroWinkler,
	/** The Sorted level2 jaro winkler. */
	SortedLevel2JaroWinkler,
	/** Compares two urls */
	urlMatcher,
	/** Exact match algo. */
	ExactMatch,
	/**
	 * Returns 0 for equal strings, 1 for different strings.
	 */
	MustBeDifferent,
	/** Always return 1.0 as distance. */
	AlwaysMatch,
	/** Person distance */
	PersonCoAuthorSurnamesDistance,
	PersonCoAnchorsDistance,
	PersonDistance,
	/** The Null. */
	Null
}
