package eu.dnetlib.pace.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import eu.dnetlib.pace.config.Type;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * The Class FieldListImpl.
 */
public class FieldListImpl extends AbstractField implements FieldList {

	/** The fields. */
	private List<Field> fields;

	/**
	 * Instantiates a new field list impl.
	 */
	public FieldListImpl() {
		fields = Lists.newArrayList();
	}

	/**
	 * Instantiates a new field list impl.
	 *
	 * @param name
	 *            the name
	 */
	public FieldListImpl(final String name, final Type type) {
		super(type, name);
		fields = Lists.newArrayList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#add(java.lang.Object)
	 */
	@Override
	public boolean add(final Field f) {
		return fields.add(f);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#add(int, java.lang.Object)
	 */
	@Override
	public void add(final int i, final Field f) {
		fields.add(i, f);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(final Collection<? extends Field> f) {
		return fields.addAll(f);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#addAll(int, java.util.Collection)
	 */
	@Override
	public boolean addAll(final int i, final Collection<? extends Field> f) {
		return fields.addAll(i, f);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#clear()
	 */
	@Override
	public void clear() {
		fields.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(final Object o) {
		return fields.contains(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#containsAll(java.util.Collection)
	 */
	@Override
	public boolean containsAll(final Collection<?> f) {
		return fields.containsAll(f);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#get(int)
	 */
	@Override
	public Field get(final int i) {
		return fields.get(i);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#indexOf(java.lang.Object)
	 */
	@Override
	public int indexOf(final Object o) {
		return fields.indexOf(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.dnetlib.pace.model.Field#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return Iterables.all(fields, f -> f.isEmpty());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Field> iterator() {
		return fields.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#lastIndexOf(java.lang.Object)
	 */
	@Override
	public int lastIndexOf(final Object o) {
		return fields.lastIndexOf(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#listIterator()
	 */
	@Override
	public ListIterator<Field> listIterator() {
		return fields.listIterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#listIterator(int)
	 */
	@Override
	public ListIterator<Field> listIterator(final int i) {
		return fields.listIterator(i);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(final Object o) {
		return fields.remove(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#remove(int)
	 */
	@Override
	public Field remove(final int i) {
		return fields.remove(i);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#removeAll(java.util.Collection)
	 */
	@Override
	public boolean removeAll(final Collection<?> f) {
		return fields.removeAll(f);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#retainAll(java.util.Collection)
	 */
	@Override
	public boolean retainAll(final Collection<?> f) {
		return fields.retainAll(f);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#set(int, java.lang.Object)
	 */
	@Override
	public Field set(final int i, final Field f) {
		return fields.set(i, f);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#size()
	 */
	@Override
	public int size() {
		return fields.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#subList(int, int)
	 */
	@Override
	public List<Field> subList(final int from, final int to) {
		return fields.subList(from, to);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#toArray()
	 */
	@Override
	public Object[] toArray() {
		return fields.toArray();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#toArray(java.lang.Object[])
	 */
	@Override
	public <T> T[] toArray(final T[] t) {
		return fields.toArray(t);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.dnetlib.pace.model.Field#stringValue()
	 */
	@Override
	public String stringValue() {
		switch (getType()) {

		case List:
		case Int:
		case String:
			return Joiner.on(" ").join(stringList());
		case JSON:
			String json;
			try {
				json = new ObjectMapper().writeValueAsString(this);
			} catch (JsonProcessingException e) {
				json = null;
			}
			return json;
		default:
			throw new IllegalArgumentException("Unknown type: " + getType().toString());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.dnetlib.pace.model.FieldList#stringList()
	 */
	@Override
	public List<String> stringList() {
		return Lists.newArrayList(Iterables.transform(fields, getValuesTransformer()));
	}

	private Function<Field, String> getValuesTransformer() {
		return new Function<Field, String>() {

			@Override
			public String apply(final Field f) {
				return f.stringValue();
			}
		};
	}

	@Override
	public double[] doubleArray() {
		return Lists.newArrayList(Iterables.transform(fields, getDouble())).stream().mapToDouble(d-> d).toArray();
	}

	private Function<Field,Double> getDouble() {

		return new Function<Field, Double>() {
			@Override
			public Double apply(final Field f) {
				return Double.parseDouble(f.stringValue());
			}
		};
	}

	@Override
	public String toString() {
		return stringList().toString();
	}

}
