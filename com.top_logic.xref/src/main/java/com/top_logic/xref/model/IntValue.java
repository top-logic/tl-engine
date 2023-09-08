package com.top_logic.xref.model;

/**
 * The value of a int or long annotation property.
 */
public class IntValue extends Value {

	/**
	 * Creates a {@link IntValue} instance.
	 */
	public static IntValue create() {
		return new IntValue();
	}

	/** Identifier for the {@link IntValue} type in JSON format. */
	public static final String INT_VALUE__TYPE = "I";

	/** @see #getValue() */
	private static final String VALUE = "v";

	private long _value = 0L;

	/**
	 * Creates a {@link IntValue} instance.
	 *
	 * @see #create()
	 */
	protected IntValue() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.INT_VALUE;
	}

	/**
	 * The annotation value.
	 */
	public final long getValue() {
		return _value;
	}

	/**
	 * @see #getValue()
	 */
	public final IntValue setValue(long value) {
		_value = value;
		return this;
	}

	/** Reads a new instance from the given reader. */
	public static IntValue readIntValue(com.top_logic.common.json.gstream.JsonReader in) throws java.io.IOException {
		IntValue result = new IntValue();
		in.beginObject();
		result.readFields(in);
		in.endObject();
		return result;
	}

	@Override
	public String jsonType() {
		return INT_VALUE__TYPE;
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
			case VALUE: setValue(in.nextLong()); break;
			default: super.readField(in, field);
		}
	}

	@Override
	public <R,A> R visit(Value.Visitor<R,A> v, A arg) {
		return v.visit(this, arg);
	}

}
