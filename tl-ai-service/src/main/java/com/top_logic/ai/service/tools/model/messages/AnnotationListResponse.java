package com.top_logic.ai.service.tools.model.messages;

/**
 * Annotation list response.
 */
public class AnnotationListResponse extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.service.tools.model.messages.AnnotationListResponse} instance.
	 */
	public static com.top_logic.ai.service.tools.model.messages.AnnotationListResponse create() {
		return new com.top_logic.ai.service.tools.model.messages.AnnotationListResponse();
	}

	/** Identifier for the {@link com.top_logic.ai.service.tools.model.messages.AnnotationListResponse} type in JSON format. */
	public static final String ANNOTATION_LIST_RESPONSE__TYPE = "AnnotationListResponse";

	/** @see #getTarget() */
	public static final String TARGET__PROP = "target";

	/** @see #getKind() */
	public static final String KIND__PROP = "kind";

	/** @see #getModule() */
	public static final String MODULE__PROP = "module";

	/** @see #getType() */
	public static final String TYPE__PROP = "type";

	/** @see #getPart() */
	public static final String PART__PROP = "part";

	/** @see #getClassifier() */
	public static final String CLASSIFIER__PROP = "classifier";

	/** @see #isIncludeInherited() */
	public static final String INCLUDE_INHERITED__PROP = "includeInherited";

	/** @see #getAnnotations() */
	public static final String ANNOTATIONS__PROP = "annotations";

	private String _target = "";

	private String _kind = "";

	private String _module = "";

	private String _type = "";

	private String _part = "";

	private String _classifier = "";

	private boolean _includeInherited = false;

	private final java.util.List<com.top_logic.ai.service.tools.model.messages.AnnotationInfo> _annotations = new java.util.ArrayList<>();

	/**
	 * Creates a {@link AnnotationListResponse} instance.
	 *
	 * @see com.top_logic.ai.service.tools.model.messages.AnnotationListResponse#create()
	 */
	protected AnnotationListResponse() {
		super();
	}

	/**
	 * Target reference (module, type, or part).
	 */
	public final String getTarget() {
		return _target;
	}

	/**
	 * @see #getTarget()
	 */
	public com.top_logic.ai.service.tools.model.messages.AnnotationListResponse setTarget(String value) {
		internalSetTarget(value);
		return this;
	}

	/** Internal setter for {@link #getTarget()} without chain call utility. */
	protected final void internalSetTarget(String value) {
		_target = value;
	}

	/**
	 * Kind of target: module, type, enumeration, part, or classifier.
	 */
	public final String getKind() {
		return _kind;
	}

	/**
	 * @see #getKind()
	 */
	public com.top_logic.ai.service.tools.model.messages.AnnotationListResponse setKind(String value) {
		internalSetKind(value);
		return this;
	}

	/** Internal setter for {@link #getKind()} without chain call utility. */
	protected final void internalSetKind(String value) {
		_kind = value;
	}

	/**
	 * Module name.
	 */
	public final String getModule() {
		return _module;
	}

	/**
	 * @see #getModule()
	 */
	public com.top_logic.ai.service.tools.model.messages.AnnotationListResponse setModule(String value) {
		internalSetModule(value);
		return this;
	}

	/** Internal setter for {@link #getModule()} without chain call utility. */
	protected final void internalSetModule(String value) {
		_module = value;
	}

	/**
	 * Type name (null for module targets).
	 */
	public final String getType() {
		return _type;
	}

	/**
	 * @see #getType()
	 */
	public com.top_logic.ai.service.tools.model.messages.AnnotationListResponse setType(String value) {
		internalSetType(value);
		return this;
	}

	/** Internal setter for {@link #getType()} without chain call utility. */
	protected final void internalSetType(String value) {
		_type = value;
	}

	/**
	 * Part name (null for module/type targets).
	 */
	public final String getPart() {
		return _part;
	}

	/**
	 * @see #getPart()
	 */
	public com.top_logic.ai.service.tools.model.messages.AnnotationListResponse setPart(String value) {
		internalSetPart(value);
		return this;
	}

	/** Internal setter for {@link #getPart()} without chain call utility. */
	protected final void internalSetPart(String value) {
		_part = value;
	}

	/**
	 * Classifier name (null for non-classifier targets).
	 */
	public final String getClassifier() {
		return _classifier;
	}

	/**
	 * @see #getClassifier()
	 */
	public com.top_logic.ai.service.tools.model.messages.AnnotationListResponse setClassifier(String value) {
		internalSetClassifier(value);
		return this;
	}

	/** Internal setter for {@link #getClassifier()} without chain call utility. */
	protected final void internalSetClassifier(String value) {
		_classifier = value;
	}

	/**
	 * Whether inherited annotations are included.
	 */
	public final boolean isIncludeInherited() {
		return _includeInherited;
	}

	/**
	 * @see #isIncludeInherited()
	 */
	public com.top_logic.ai.service.tools.model.messages.AnnotationListResponse setIncludeInherited(boolean value) {
		internalSetIncludeInherited(value);
		return this;
	}

	/** Internal setter for {@link #isIncludeInherited()} without chain call utility. */
	protected final void internalSetIncludeInherited(boolean value) {
		_includeInherited = value;
	}

	/**
	 * List of annotations.
	 */
	public final java.util.List<com.top_logic.ai.service.tools.model.messages.AnnotationInfo> getAnnotations() {
		return _annotations;
	}

	/**
	 * @see #getAnnotations()
	 */
	public com.top_logic.ai.service.tools.model.messages.AnnotationListResponse setAnnotations(java.util.List<? extends com.top_logic.ai.service.tools.model.messages.AnnotationInfo> value) {
		internalSetAnnotations(value);
		return this;
	}

	/** Internal setter for {@link #getAnnotations()} without chain call utility. */
	protected final void internalSetAnnotations(java.util.List<? extends com.top_logic.ai.service.tools.model.messages.AnnotationInfo> value) {
		if (value == null) throw new IllegalArgumentException("Property 'annotations' cannot be null.");
		_annotations.clear();
		_annotations.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getAnnotations()} list.
	 */
	public com.top_logic.ai.service.tools.model.messages.AnnotationListResponse addAnnotation(com.top_logic.ai.service.tools.model.messages.AnnotationInfo value) {
		internalAddAnnotation(value);
		return this;
	}

	/** Implementation of {@link #addAnnotation(com.top_logic.ai.service.tools.model.messages.AnnotationInfo)} without chain call utility. */
	protected final void internalAddAnnotation(com.top_logic.ai.service.tools.model.messages.AnnotationInfo value) {
		_annotations.add(value);
	}

	/**
	 * Removes a value from the {@link #getAnnotations()} list.
	 */
	public final void removeAnnotation(com.top_logic.ai.service.tools.model.messages.AnnotationInfo value) {
		_annotations.remove(value);
	}

	@Override
	public String jsonType() {
		return ANNOTATION_LIST_RESPONSE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TARGET__PROP, 
			KIND__PROP, 
			MODULE__PROP, 
			TYPE__PROP, 
			PART__PROP, 
			CLASSIFIER__PROP, 
			INCLUDE_INHERITED__PROP, 
			ANNOTATIONS__PROP);
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
			case TARGET__PROP: return getTarget();
			case KIND__PROP: return getKind();
			case MODULE__PROP: return getModule();
			case TYPE__PROP: return getType();
			case PART__PROP: return getPart();
			case CLASSIFIER__PROP: return getClassifier();
			case INCLUDE_INHERITED__PROP: return isIncludeInherited();
			case ANNOTATIONS__PROP: return getAnnotations();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TARGET__PROP: internalSetTarget((String) value); break;
			case KIND__PROP: internalSetKind((String) value); break;
			case MODULE__PROP: internalSetModule((String) value); break;
			case TYPE__PROP: internalSetType((String) value); break;
			case PART__PROP: internalSetPart((String) value); break;
			case CLASSIFIER__PROP: internalSetClassifier((String) value); break;
			case INCLUDE_INHERITED__PROP: internalSetIncludeInherited((boolean) value); break;
			case ANNOTATIONS__PROP: internalSetAnnotations(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.ai.service.tools.model.messages.AnnotationInfo.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.service.tools.model.messages.AnnotationListResponse readAnnotationListResponse(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.service.tools.model.messages.AnnotationListResponse result = new com.top_logic.ai.service.tools.model.messages.AnnotationListResponse();
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
		out.name(TARGET__PROP);
		out.value(getTarget());
		out.name(KIND__PROP);
		out.value(getKind());
		out.name(MODULE__PROP);
		out.value(getModule());
		out.name(TYPE__PROP);
		out.value(getType());
		out.name(PART__PROP);
		out.value(getPart());
		out.name(CLASSIFIER__PROP);
		out.value(getClassifier());
		out.name(INCLUDE_INHERITED__PROP);
		out.value(isIncludeInherited());
		out.name(ANNOTATIONS__PROP);
		out.beginArray();
		for (com.top_logic.ai.service.tools.model.messages.AnnotationInfo x : getAnnotations()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TARGET__PROP: setTarget(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case KIND__PROP: setKind(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case MODULE__PROP: setModule(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TYPE__PROP: setType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case PART__PROP: setPart(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CLASSIFIER__PROP: setClassifier(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case INCLUDE_INHERITED__PROP: setIncludeInherited(in.nextBoolean()); break;
			case ANNOTATIONS__PROP: {
				java.util.List<com.top_logic.ai.service.tools.model.messages.AnnotationInfo> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.ai.service.tools.model.messages.AnnotationInfo.readAnnotationInfo(in));
				}
				in.endArray();
				setAnnotations(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
