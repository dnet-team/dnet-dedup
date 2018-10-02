package eu.dnetlib.pace.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import eu.dnetlib.pace.config.Type;
import org.apache.commons.collections.iterators.SingletonIterator;
import org.apache.commons.lang.StringUtils;

/**
 * The Class FieldValueImpl.
 */
public class FieldValueImpl extends AbstractField implements FieldValue {

	/** The value. */
	private Object value = null;

	/**
	 * Instantiates a new field value impl.
	 */
	public FieldValueImpl() {}

	/**
	 * Instantiates a new field value impl.
	 * 
	 * @param type
	 *            the type
	 * @param name
	 *            the name
	 * @param value
	 *            the value
	 */
	public FieldValueImpl(final Type type, final String name, final Object value) {
		super(type, name);
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.dnetlib.pace.model.Field#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		if (value == null) return false;

		switch (type) {
		case String:
		case JSON:
			return value.toString().isEmpty();
		case List:
			List<?> list = (List<?>) value;
			return list.isEmpty() || ((FieldValueImpl) list.get(0)).isEmpty();
		case URL:
			String str = value.toString();
			return StringUtils.isBlank(str) || !isValidURL(str);
		default:
			return true;
		}
	}

	private boolean isValidURL(final String s) {
		try {
			new URL(s);
			return true;
		} catch (MalformedURLException e) {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.dnetlib.pace.model.FieldValue#getValue()
	 */
	@Override
	public Object getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.dnetlib.pace.model.FieldValue#setValue(java.lang.Object)
	 */
	@Override
	public void setValue(final Object value) {
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.dnetlib.pace.model.Field#stringValue()
	 */
	@Override
	// @SuppressWarnings("unchecked")
	public String stringValue() {
		return String.valueOf(getValue());
		// switch (getType()) {
		//
		// case Int:
		// return String.valueOf(getValue());
		// case List:
		// return Joiner.on(" ").join((List<String>) getValue());
		// case String:
		// return (String) getValue();
		// default:
		// throw new IllegalArgumentException("Unknown type: " + getType().toString());
		// }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Iterator<Field> iterator() {
		return new SingletonIterator(this);
	}

}
