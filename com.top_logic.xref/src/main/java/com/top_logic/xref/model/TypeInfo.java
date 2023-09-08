package com.top_logic.xref.model;

/**
 * Information about a Java class or interface.
 *
 * <p>
 * By reading all <code>TypeIndex.json</code> files in the class path, an application can construct global knowledge 
 * about all public classes and their relationship without actually loading the classes. 
 * </p>
 *
 * @see #getGeneralizations()
 */
public class TypeInfo extends AbstractDataObject {

	/**
	 * Creates a {@link TypeInfo} instance.
	 */
	public static TypeInfo create() {
		return new TypeInfo();
	}

	/** Identifier for the {@link TypeInfo} type in JSON format. */
	public static final String TYPE_INFO__TYPE = "TypeInfo";

	/** @see #isPublic() */
	private static final String PUBLIC = "P";

	/** @see #isInterface() */
	private static final String INTERFACE = "I";

	/** @see #isAbstract() */
	private static final String ABSTRACT = "A";

	/** @see #getGeneralizations() */
	private static final String GENERALIZATIONS = "x";

	/** @see #getConfiguration() */
	private static final String CONFIGURATION = "c";

	/** @see #getImplementation() */
	private static final String IMPLEMENTATION = "i";

	/** @see #getAnnotations() */
	private static final String ANNOTATIONS = "a";

	private boolean _public = false;

	private boolean _interface = false;

	private boolean _abstract = false;

	private final java.util.List<String> _generalizations = new java.util.ArrayList<>();

	private String _configuration = "";

	private String _implementation = "";

	private final java.util.Map<String, AnnotationInfo> _annotations = new java.util.HashMap<>();

	/**
	 * Creates a {@link TypeInfo} instance.
	 *
	 * @see #create()
	 */
	protected TypeInfo() {
		super();
	}

	/**
	 * Whether the type represented by this info is public.
	 */
	public final boolean isPublic() {
		return _public;
	}

	/**
	 * @see #isPublic()
	 */
	public final TypeInfo setPublic(boolean value) {
		_public = value;
		return this;
	}

	/**
	 * Whether the type represented by this info is an interface.
	 */
	public final boolean isInterface() {
		return _interface;
	}

	/**
	 * @see #isInterface()
	 */
	public final TypeInfo setInterface(boolean value) {
		_interface = value;
		return this;
	}

	/**
	 * Whether the type represented by this info is abstract. A class is abstract, if it has the abstract modifier. An interface is abstract, if it has the @Abstract annotation.
	 */
	public final boolean isAbstract() {
		return _abstract;
	}

	/**
	 * @see #isAbstract()
	 */
	public final TypeInfo setAbstract(boolean value) {
		_abstract = value;
		return this;
	}

	/**
	 * The names of types this type extends (classes and interfaces).
	 */
	public final java.util.List<String> getGeneralizations() {
		return _generalizations;
	}

	/**
	 * @see #getGeneralizations()
	 */
	public final TypeInfo setGeneralizations(java.util.List<String> value) {
		_generalizations.clear();
		_generalizations.addAll(value);
		return this;
	}

	/**
	 * Adds a value to the {@link #getGeneralizations()} list.
	 */
	public final TypeInfo addGeneralization(String value) {
		_generalizations.add(value);
		return this;
	}

	/**
	 * The name of the configuration interface this implementation class is configured with, or <code>null</code> if this is not a configured class.
	 */
	public final String getConfiguration() {
		return _configuration;
	}

	/**
	 * @see #getConfiguration()
	 */
	public final TypeInfo setConfiguration(String value) {
		_configuration = value;
		return this;
	}

	/**
	 * The name of the implementation type this polymorphic configuration interface is instantiated to.
	 */
	public final String getImplementation() {
		return _implementation;
	}

	/**
	 * @see #getImplementation()
	 */
	public final TypeInfo setImplementation(String value) {
		_implementation = value;
		return this;
	}

	/**
	 * The annotations of this type. The key is the annotation interface name.
	 */
	public final java.util.Map<String, AnnotationInfo> getAnnotations() {
		return _annotations;
	}

	/**
	 * @see #getAnnotations()
	 */
	public final TypeInfo setAnnotations(java.util.Map<String, AnnotationInfo> value) {
		if (value == null) throw new IllegalArgumentException("Property 'annotations' cannot be null.");
		_annotations.clear();
		_annotations.putAll(value);
		return this;
	}

	/**
	 * Adds a value to the {@link #getAnnotations()} map.
	 */
	public final void putAnnotation(String key, AnnotationInfo value) {
		if (_annotations.containsKey(key)) {
			throw new IllegalArgumentException("Property 'annotations' already contains a value for key '" + key + "'.");
		}
		_annotations.put(key, value);
	}

	/** Reads a new instance from the given reader. */
	public static TypeInfo readTypeInfo(com.top_logic.common.json.gstream.JsonReader in) throws java.io.IOException {
		TypeInfo result = new TypeInfo();
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
		out.name(PUBLIC);
		out.value(isPublic());
		out.name(INTERFACE);
		out.value(isInterface());
		out.name(ABSTRACT);
		out.value(isAbstract());
		out.name(GENERALIZATIONS);
		out.beginArray();
		for (String x : getGeneralizations()) {
			out.value(x);
		}
		out.endArray();
		out.name(CONFIGURATION);
		out.value(getConfiguration());
		out.name(IMPLEMENTATION);
		out.value(getImplementation());
		out.name(ANNOTATIONS);
		out.beginObject();
		for (java.util.Map.Entry<String,AnnotationInfo> entry : getAnnotations().entrySet()) {
			out.name(entry.getKey());
			entry.getValue().writeTo(out);
		}
		out.endObject();
	}

	@Override
	protected void readField(com.top_logic.common.json.gstream.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case PUBLIC: setPublic(in.nextBoolean()); break;
			case INTERFACE: setInterface(in.nextBoolean()); break;
			case ABSTRACT: setAbstract(in.nextBoolean()); break;
			case GENERALIZATIONS: {
				in.beginArray();
				while (in.hasNext()) {
					addGeneralization(JsonUtil.nextStringOptional(in));
				}
				in.endArray();
			}
			break;
			case CONFIGURATION: setConfiguration(JsonUtil.nextStringOptional(in)); break;
			case IMPLEMENTATION: setImplementation(JsonUtil.nextStringOptional(in)); break;
			case ANNOTATIONS: {
				in.beginObject();
				while (in.hasNext()) {
					putAnnotation(in.nextName(), AnnotationInfo.readAnnotationInfo(in));
				}
				in.endObject();
				break;
			}
			default: super.readField(in, field);
		}
	}

}
