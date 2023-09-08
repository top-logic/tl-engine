package com.top_logic.xref.model;

/**
 * The value of an array annotation property.
 */
public class ListValue extends Value {

	/**
	 * Creates a {@link ListValue} instance.
	 */
	public static ListValue create() {
		return new ListValue();
	}

	/** Identifier for the {@link ListValue} type in JSON format. */
	public static final String LIST_VALUE__TYPE = "L";

	/** @see #getValues() */
	private static final String VALUES = "v";

	private final java.util.List<Value> _values = new java.util.ArrayList<>();

	/**
	 * Creates a {@link ListValue} instance.
	 *
	 * @see #create()
	 */
	protected ListValue() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.LIST_VALUE;
	}

	/**
	 * The annotation values.
	 */
	public final java.util.List<Value> getValues() {
		return _values;
	}

	/**
	 * @see #getValues()
	 */
	public final ListValue setValues(java.util.List<Value> value) {
		if (value == null) throw new IllegalArgumentException("Property 'values' cannot be null.");
		_values.clear();
		_values.addAll(value);
		return this;
	}

	/**
	 * Adds a value to the {@link #getValues()} list.
	 */
	public final ListValue addValue(Value value) {
		_values.add(value);
		return this;
	}

	/** Reads a new instance from the given reader. */
	public static ListValue readListValue(com.top_logic.common.json.gstream.JsonReader in) throws java.io.IOException {
		ListValue result = new ListValue();
		in.beginObject();
		result.readFields(in);
		in.endObject();
		return result;
	}

	@Override
	public String jsonType() {
		return LIST_VALUE__TYPE;
	}

	@Override
	protected void writeFields(com.top_logic.common.json.gstream.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(VALUES);
		out.beginArray();
		for (Value x : getValues()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(com.top_logic.common.json.gstream.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case VALUES: {
				in.beginArray();
				while (in.hasNext()) {
					addValue(Value.readValue(in));
				}
				in.endArray();
			}
			break;
			default: super.readField(in, field);
		}
	}

	@Override
	public <R,A> R visit(Value.Visitor<R,A> v, A arg) {
		return v.visit(this, arg);
	}

}
