package eu.dnetlib.pace.model;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * The Class MapDocument.
 */
public class MapDocument implements Document, Serializable {

	/** The identifier. */
	private String identifier;

	/** The field map. */
	private Map<String, Field> fieldMap;

	/**
	 * Instantiates a new map document.
	 */
	public MapDocument() {
		identifier = null;
		fieldMap = Maps.newHashMap();
	}

	/**
	 * Instantiates a new map document.
	 *
	 * @param identifier
	 *            the identifier
	 * @param fieldMap
	 *            the field map
	 */
	public MapDocument(final String identifier, final Map<String, Field> fieldMap) {
		this.setIdentifier(identifier);
		this.fieldMap = fieldMap;
	}

	/**
	 * Instantiates a new map document.
	 *
	 * @param identifier
	 *            the identifier
	 * @param data
	 *            the data
	 */
	public MapDocument(final String identifier, final byte[] data) {
		final MapDocument doc = MapDocumentSerializer.decode(data);

		this.fieldMap = doc.fieldMap;
		this.identifier = doc.identifier;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.dnetlib.pace.model.document.Document#fields()
	 */
	@Override
	public Iterable<Field> fields() {
		return Lists.newArrayList(Iterables.concat(fieldMap.values()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.dnetlib.pace.model.document.Document#values(java.lang.String)
	 */
	@Override
	public Field values(final String name) {
		return fieldMap.get(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.dnetlib.pace.model.document.Document#fieldNames()
	 */
	@Override
	public Set<String> fieldNames() {
		return fieldMap.keySet();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return MapDocumentSerializer.toString(this);
		// return String.format("Document(%s)", fieldMap.toString());
	}

	/**
	 * To byte array.
	 *
	 * @return the byte[]
	 */
	public byte[] toByteArray() {
		return MapDocumentSerializer.toByteArray(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.dnetlib.pace.model.document.Document#getIdentifier()
	 */
	@Override
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Sets the identifier.
	 *
	 * @param identifier
	 *            the new identifier
	 */
	public void setIdentifier(final String identifier) {
		this.identifier = identifier;
	}

	/**
	 * Gets the field map.
	 *
	 * @return the field map
	 */
	public Map<String, Field> getFieldMap() {
		return fieldMap;
	}

	/**
	 * Sets the field map.
	 *
	 * @param fieldMap
	 *            the field map
	 */
	public void setFieldMap(final Map<String, Field> fieldMap) {
		this.fieldMap = fieldMap;
	}

}
