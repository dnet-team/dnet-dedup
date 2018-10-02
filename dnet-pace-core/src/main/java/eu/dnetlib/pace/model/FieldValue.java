package eu.dnetlib.pace.model;

/**
 * The Interface FieldValue.
 */
public interface FieldValue extends Field {

	/**
	 * Gets the value.
	 * 
	 * @return the value
	 */
	public Object getValue();

	/**
	 * Sets the value.
	 * 
	 * @param value
	 *            the new value
	 */
	public void setValue(final Object value);

}
