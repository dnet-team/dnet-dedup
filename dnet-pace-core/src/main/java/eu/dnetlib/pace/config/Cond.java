package eu.dnetlib.pace.config;

/**
 * The Enum Cond.
 */
public enum Cond {

	/** The year match. */
	yearMatch,
	/** The title version match. */
	titleVersionMatch,
	/** The size match. */
	sizeMatch,
	/**
	 * Returns true if the field values are different
	 */
	mustBeDifferent,
	/** The Exact match. */
	exactMatch,
	/**
	 * The Exact match ignore case.
	 */
	exactMatchIgnoreCase,
	/** The Exact match specialized to recognize DOI values. */
	doiExactMatch,
	/** The Exact match that checks if pid type and value are the same */
	pidMatch
}
