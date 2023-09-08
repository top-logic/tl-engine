package com.top_logic.xref.model;

/**
 * Information about the values of an annotation.
 */
public class AnnotationInfo extends AbstractDataObject {

	/**
	 * Creates a {@link AnnotationInfo} instance.
	 */
	public static AnnotationInfo create() {
		return new AnnotationInfo();
	}

	/** Identifier for the {@link AnnotationInfo} type in JSON format. */
	public static final String ANNOTATION_INFO__TYPE = "AnnotationInfo";

	/** @see #getProperties() */
	private static final String PROPERTIES = "p";

	private final java.util.Map<String, Value> _properties = new java.util.HashMap<>();

	/**
	 * Creates a {@link AnnotationInfo} instance.
	 *
	 * @see #create()
	 */
	protected AnnotationInfo() {
		super();
	}

	/**
	 * The values of an annotation. The key is the annotation property name.
	 */
	public final java.util.Map<String, Value> getProperties() {
		return _properties;
	}

	/**
	 * @see #getProperties()
	 */
	public final AnnotationInfo setProperties(java.util.Map<String, Value> value) {
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
	public static AnnotationInfo readAnnotationInfo(com.top_logic.common.json.gstream.JsonReader in) throws java.io.IOException {
		AnnotationInfo result = new AnnotationInfo();
		in.beginObject();
		result.readFields(in);
		in.endObject();
		return result;
	}

	@Override
	public final void writeTo(com.top_logic.common.json.gstream.JsonWriter out) throws java.io.IOException {
		writeContent(out);
	}

	@Override
	protected void writeFields(com.top_logic.common.json.gstream.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
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

}
