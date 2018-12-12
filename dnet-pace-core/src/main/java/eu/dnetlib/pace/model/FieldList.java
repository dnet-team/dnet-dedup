package eu.dnetlib.pace.model;

import java.util.List;

/**
 * The Interface FieldList.
 */
public interface FieldList extends List<Field>, Field {

	/**
	 * String list.
	 * 
	 * @return the list
	 */
	public List<String> stringList();

	/**
	 * Double[] Array
	 *
	 * @return the double[] array
	 */
	public double[] doubleArray();

}
