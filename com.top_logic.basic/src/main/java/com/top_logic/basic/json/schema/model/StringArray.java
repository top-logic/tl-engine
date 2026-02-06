package com.top_logic.basic.json.schema.model;

/**
 * Array of strings for use in maps.
 *
 * <p>
 * Proto3 doesn't support repeated fields in maps directly.
 * </p>
 */
public class StringArray extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.basic.json.schema.model.StringArray} instance.
	 */
	public static com.top_logic.basic.json.schema.model.StringArray create() {
		return new com.top_logic.basic.json.schema.model.StringArray();
	}

	/** Identifier for the {@link com.top_logic.basic.json.schema.model.StringArray} type in JSON format. */
	public static final String STRING_ARRAY__TYPE = "StringArray";

	/** @see #getValues() */
	public static final String VALUES__PROP = "values";

	private final java.util.List<String> _values = new java.util.ArrayList<>();

	/**
	 * Creates a {@link StringArray} instance.
	 *
	 * @see com.top_logic.basic.json.schema.model.StringArray#create()
	 */
	protected StringArray() {
		super();
	}

	/**
	 * Array of string values.
	 */
	public final java.util.List<String> getValues() {
		return _values;
	}

	/**
	 * @see #getValues()
	 */
	public com.top_logic.basic.json.schema.model.StringArray setValues(java.util.List<? extends String> value) {
		internalSetValues(value);
		return this;
	}

	/** Internal setter for {@link #getValues()} without chain call utility. */
	protected final void internalSetValues(java.util.List<? extends String> value) {
		_values.clear();
		_values.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getValues()} list.
	 */
	public com.top_logic.basic.json.schema.model.StringArray addValue(String value) {
		internalAddValue(value);
		return this;
	}

	/** Implementation of {@link #addValue(String)} without chain call utility. */
	protected final void internalAddValue(String value) {
		_values.add(value);
	}

	/**
	 * Removes a value from the {@link #getValues()} list.
	 */
	public final void removeValue(String value) {
		_values.remove(value);
	}

	@Override
	public String jsonType() {
		return STRING_ARRAY__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			VALUES__PROP);
		PROPERTIES = java.util.Collections.unmodifiableList(local);
	}

	static final java.util.Set<String> TRANSIENT_PROPERTIES;
	static {
		java.util.HashSet<String> tmp = new java.util.HashSet<>();
		tmp.addAll(java.util.Arrays.asList(
				));
		TRANSIENT_PROPERTIES = java.util.Collections.unmodifiableSet(tmp);
	}

	@Override
	public java.util.List<String> properties() {
		return PROPERTIES;
	}

	@Override
	public java.util.Set<String> transientProperties() {
		return TRANSIENT_PROPERTIES;
	}

	@Override
	public Object get(String field) {
		switch (field) {
			case VALUES__PROP: return getValues();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case VALUES__PROP: internalSetValues(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.basic.json.schema.model.StringArray readStringArray(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.basic.json.schema.model.StringArray result = new com.top_logic.basic.json.schema.model.StringArray();
		result.readContent(in);
		return result;
	}

	@Override
	public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		writeContent(out);
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(VALUES__PROP);
		out.beginArray();
		for (String x : getValues()) {
			out.value(x);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case VALUES__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setValues(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
