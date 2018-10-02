package eu.dnetlib.pace.model;

import java.util.Comparator;

import com.google.common.collect.Iterables;

import eu.dnetlib.pace.clustering.NGramUtils;

/**
 * The Class MapDocumentComparator.
 */
public class MapDocumentComparator implements Comparator<Document> {

	/** The comparator field. */
	private String comparatorField;

	private final FieldList emptyField = new FieldListImpl();

	/**
	 * Instantiates a new map document comparator.
	 *
	 * @param comparatorField
	 *            the comparator field
	 */
	public MapDocumentComparator(final String comparatorField) {
		this.comparatorField = comparatorField;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(final Document d1, final Document d2) {

		if (d1.values(comparatorField).isEmpty() || d2.values(comparatorField).isEmpty()) return 0;

		final String o1 = Iterables.getFirst(d1.values(comparatorField), emptyField).stringValue();
		final String o2 = Iterables.getFirst(d2.values(comparatorField), emptyField).stringValue();

		if ((o1 == null) || (o2 == null)) return 0;

		final String to1 = NGramUtils.cleanupForOrdering(o1);
		final String to2 = NGramUtils.cleanupForOrdering(o2);

		return to1.compareTo(to2);
	}

}
