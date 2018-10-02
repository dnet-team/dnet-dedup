package eu.dnetlib.pace.model;

import java.util.Set;

/**
 * The Interface Document. Models the common operations available on a Pace Document.
 */
public interface Document {

	/**
	 * Gets the identifier.
	 *
	 * @return the identifier
	 */
	String getIdentifier();

	/**
	 * Fields.
	 *
	 * @return the iterable
	 */
	Iterable<Field> fields();

	/**
	 * Values.
	 *
	 * @param name
	 *            the name
	 * @return the field list
	 */
	Field values(String name);

	/**
	 * Field names.
	 *
	 * @return the sets the
	 */
	Set<String> fieldNames();
}
