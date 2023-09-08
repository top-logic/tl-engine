package com.top_logic.xref.model;

/**
 * The value of an annotation valued annotation property.
 */
public class AnnotationValue extends Value {

	/**
	 * Creates a {@link AnnotationValue} instance.
	 */
	public static AnnotationValue create() {
		return new AnnotationValue();
	}

	/** Identifier for the {@link AnnotationValue} type in JSON format. */
	public static final String ANNOTATION_VALUE__TYPE = "A";

	/** @see #getType() */
	private static final String TYPE = "t";

	/** @see #getProperties() */
	private static final String PROPERTIES = "p";

	private String _type = "";

	private final java.util.Map<String, Value> _properties = new java.util.HashMap<>();

	/**
	 * Creates a {@link AnnotationValue} instance.
	 *
	 * @see #create()
	 */
	protected AnnotationValue() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.ANNOTATION_VALUE;
	}

	/**
	 * The name of the annotation interface.
	 */
	public final String getType() {
		return _type;
	}

	/**
	 * @see #getType()
	 */
	public final AnnotationValue setType(String value) {
		_type = value;
		return this;
	}

	/**
	 * The properties of the annotation.
	 */
	public final java.util.Map<String, Value> getProperties() {
		return _properties;
	}

	/**
	 * @see #getProperties()
	 */
	public final AnnotationValue setProperties(java.util.Map<String, Value> value) {
		if (value == null) throw new IllegalArgumentException("Property 'properties' cannot be null.");
		_properties.clear();
		_properties.putAll(value);
		return this;
	}

	/**
	 * Adds a value to the {@link #getProperties()} map.
	 */
	public final void putPropertie(String key, Value value) {
		if (_properties.containsKey(key)) {
			throw new IllegalArgumentException("Property 'properties' already contains a value for key '" + key + "'.");
		}
		_properties.put(key, value);
	}

	/** Reads a new instance from the given reader. */
	public static AnnotationValue readAnnotationValue(com.top_logic.common.json.gstream.JsonReader in) throws java.io.IOException {
		AnnotationValue result = new AnnotationValue();
		in.beginObject();
		result.readFields(in);
		in.endObject();
		return result;
	}

	@Override
	public String jsonType() {
		return ANNOTATION_VALUE__TYPE;
	}

	@Override
	protected void writeFields(com.top_logic.common.json.gstream.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(TYPE);
		out.value(getType());
		out.name(PROPERTIES);
		out.beginObject();
		for (java.util.Map.Entry<String,Value> entry : getProperties().entrySet()) {
			out.name(entry.getKey());
			entry.getValue().writeTo(out);
		}
		out.endObject();
	}

	@Override
	protected void readField(com.top_logic.common.json.gstream.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TYPE:
				setType(JsonUtil.nextStringOptional(in));
				break;
			case PROPERTIES: {
				in.beginObject();
				while (in.hasNext()) {
					putPropertie(in.nextName(), Value.readValue(in));
				}
				in.endObject();
				break;
			}
			default: super.readField(in, field);
		}
	}

	@Override
	public <R,A> R visit(Value.Visitor<R,A> v, A arg) {
		return v.visit(this, arg);
	}

}
