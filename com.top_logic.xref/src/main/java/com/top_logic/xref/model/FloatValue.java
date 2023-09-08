package com.top_logic.xref.model;

/**
 * The value of a float or double annotation property.
 */
public class FloatValue extends Value {

	/**
	 * Creates a {@link FloatValue} instance.
	 */
	public static FloatValue create() {
		return new FloatValue();
	}

	/** Identifier for the {@link FloatValue} type in JSON format. */
	public static final String FLOAT_VALUE__TYPE = "F";

	/** @see #getValue() */
	private static final String VALUE = "v";

	private double _value = 0.0d;

	/**
	 * Creates a {@link FloatValue} instance.
	 *
	 * @see #create()
	 */
	protected FloatValue() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.FLOAT_VALUE;
	}

	/**
	 * The annotation value.
	 */
	public final double getValue() {
		return _value;
	}

	/**
	 * @see #getValue()
	 */
	public final FloatValue setValue(double value) {
		_value = value;
		return this;
	}

	/** Reads a new instance from the given reader. */
	public static FloatValue readFloatValue(com.top_logic.common.json.gstream.JsonReader in) throws java.io.IOException {
		FloatValue result = new FloatValue();
		in.beginObject();
		result.readFields(in);
		in.endObject();
		return result;
	}

	@Override
	public String jsonType() {
		return FLOAT_VALUE__TYPE;
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
			case VALUE: setValue(in.nextDouble()); break;
			default: super.readField(in, field);
		}
	}

	@Override
	public <R,A> R visit(Value.Visitor<R,A> v, A arg) {
		return v.visit(this, arg);
	}

}
