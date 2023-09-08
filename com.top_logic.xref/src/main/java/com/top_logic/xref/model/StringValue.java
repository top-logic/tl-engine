package com.top_logic.xref.model;

/**
 * The value of string, class, or enum annotation property.
 */
public class StringValue extends Value {

	/**
	 * Creates a {@link StringValue} instance.
	 */
	public static StringValue create() {
		return new StringValue();
	}

	/** Identifier for the {@link StringValue} type in JSON format. */
	public static final String STRING_VALUE__TYPE = "S";

	/** @see #getValue() */
	private static final String VALUE = "v";

	private String _value = "";

	/**
	 * Creates a {@link StringValue} instance.
	 *
	 * @see #create()
	 */
	protected StringValue() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.STRING_VALUE;
	}

	/**
	 * The annotation value.
	 */
	public final String getValue() {
		return _value;
	}

	/**
	 * @see #getValue()
	 */
	public final StringValue setValue(String value) {
		_value = value;
		return this;
	}

	/** Reads a new instance from the given reader. */
	public static StringValue readStringValue(com.top_logic.common.json.gstream.JsonReader in) throws java.io.IOException {
		StringValue result = new StringValue();
		in.beginObject();
		result.readFields(in);
		in.endObject();
		return result;
	}

	@Override
	public String jsonType() {
		return STRING_VALUE__TYPE;
	}

	@Override
	protected void writeFields(com.top_logic.common.json.gstream.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(VALUE);
		out.value(getValue());
	}

	@Override
	protected void readField(com.top_logic.common.json.gstream.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case VALUE: setValue(JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

	@Override
	public <R,A> R visit(Value.Visitor<R,A> v, A arg) {
		return v.visit(this, arg);
	}

}
