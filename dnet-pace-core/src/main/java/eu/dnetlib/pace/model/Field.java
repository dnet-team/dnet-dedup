package eu.dnetlib.pace.model;

import eu.dnetlib.pace.config.Type;

import java.io.Serializable;

/**
 * The Interface Field.
 */
public interface Field extends Iterable<Field>, Serializable {

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName();

	/**
	 * Sets the name.
	 * 
	 * @param name
	 *            the new name
	 */
	public void setName(String name);

	/**
	 * Gets the type.
	 * 
	 * @return the type
	 */
	public Type getType();

	/**
	 * Sets the type.
	 * 
	 * @param type
	 *            the new type
	 */
	public void setType(Type type);

	/**
	 * Checks if is empty.
	 * 
	 * @return true, if is empty
	 */
	public boolean isEmpty();

	/**
	 * String value.
	 * 
	 * @return the string
	 */
	public String stringValue();

}
