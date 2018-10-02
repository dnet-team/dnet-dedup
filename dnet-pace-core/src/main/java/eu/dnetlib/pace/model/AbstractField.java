package eu.dnetlib.pace.model;

import eu.dnetlib.pace.config.Type;

/**
 * The Class AbstractField.
 */
public abstract class AbstractField implements Field {

	/** The type. */
	protected Type type = Type.String;

	/** The name. */
	protected String name;

	/**
	 * Instantiates a new abstract field.
	 */
	protected AbstractField() {}

	/**
	 * Instantiates a new abstract field.
	 *
	 * @param type
	 *            the type
	 * @param name
	 *            the name
	 */
	protected AbstractField(final Type type, final String name) {
		this.type = type;
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.dnetlib.pace.model.Field#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.dnetlib.pace.model.Field#getType()
	 */
	@Override
	public Type getType() {
		return type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.dnetlib.pace.model.Field#setName(java.lang.String)
	 */
	@Override
	public void setName(final String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.dnetlib.pace.model.Field#setType(eu.dnetlib.pace.config.Type)
	 */
	@Override
	public void setType(final Type type) {
		this.type = type;
	}

}
