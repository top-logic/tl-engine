package com.top_logic.xref.model;

/**
 * Top-level type stored in a <code>TypeIndex.json</code> file.
 *
 * @see TypeInfo
 */
public class IndexFile extends AbstractDataObject {

	/**
	 * Creates a {@link IndexFile} instance.
	 */
	public static IndexFile create() {
		return new IndexFile();
	}

	/** Identifier for the {@link IndexFile} type in JSON format. */
	public static final String INDEX_FILE__TYPE = "IndexFile";

	/** @see #getTypes() */
	private static final String TYPES = "types";

	private final java.util.Map<String, TypeInfo> _types = new java.util.HashMap<>();

	/**
	 * Creates a {@link IndexFile} instance.
	 *
	 * @see #create()
	 */
	protected IndexFile() {
		super();
	}

	/**
	 * Information of all public types in a JAR. The key is the qualified class or interface name.
	 */
	public final java.util.Map<String, TypeInfo> getTypes() {
		return _types;
	}

	/**
	 * @see #getTypes()
	 */
	public final IndexFile setTypes(java.util.Map<String, TypeInfo> value) {
		if (value == null) throw new IllegalArgumentException("Property 'types' cannot be null.");
		_types.clear();
		_types.putAll(value);
		return this;
	}

	/**
	 * Adds a value to the {@link #getTypes()} map.
	 */
	public final void putType(String key, TypeInfo value) {
		if (_types.containsKey(key)) {
			throw new IllegalArgumentException("Property 'types' already contains a value for key '" + key + "'.");
		}
		_types.put(key, value);
	}

	/** Reads a new instance from the given reader. */
	public static IndexFile readIndexFile(com.top_logic.common.json.gstream.JsonReader in) throws java.io.IOException {
		IndexFile result = new IndexFile();
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
		out.name(TYPES);
		out.beginObject();
		for (java.util.Map.Entry<String,TypeInfo> entry : getTypes().entrySet()) {
			out.name(entry.getKey());
			entry.getValue().writeTo(out);
		}
		out.endObject();
	}

	@Override
	protected void readField(com.top_logic.common.json.gstream.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TYPES: {
				in.beginObject();
				while (in.hasNext()) {
					putType(in.nextName(), TypeInfo.readTypeInfo(in));
				}
				in.endObject();
				break;
			}
			default: super.readField(in, field);
		}
	}

}
