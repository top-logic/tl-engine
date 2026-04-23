package com.top_logic.tl.ai.core.tools.model.messages;

/**
 * Response for annotation set/remove operations.
 */
public class AnnotationEditorResponse extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.model.messages.AnnotationEditorResponse} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.model.messages.AnnotationEditorResponse create() {
		return new com.top_logic.tl.ai.core.tools.model.messages.AnnotationEditorResponse();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.model.messages.AnnotationEditorResponse} type in JSON format. */
	public static final String ANNOTATION_EDITOR_RESPONSE__TYPE = "AnnotationEditorResponse";

	/** @see #isSuccess() */
	public static final String SUCCESS__PROP = "success";

	/** @see #getTarget() */
	public static final String TARGET__PROP = "target";

	/** @see #getKind() */
	public static final String KIND__PROP = "kind";

	/** @see #getConfigInterface() */
	public static final String CONFIG_INTERFACE__PROP = "configInterface";

	/** @see #getDetail() */
	public static final String DETAIL__PROP = "detail";

	private boolean _success = false;

	private String _target = "";

	private String _kind = "";

	private String _configInterface = "";

	private String _detail = "";

	/**
	 * Creates a {@link AnnotationEditorResponse} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.model.messages.AnnotationEditorResponse#create()
	 */
	protected AnnotationEditorResponse() {
		super();
	}

	/**
	 * Whether the operation succeeded.
	 */
	public final boolean isSuccess() {
		return _success;
	}

	/**
	 * @see #isSuccess()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.AnnotationEditorResponse setSuccess(boolean value) {
		internalSetSuccess(value);
		return this;
	}

	/** Internal setter for {@link #isSuccess()} without chain call utility. */
	protected final void internalSetSuccess(boolean value) {
		_success = value;
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
	public com.top_logic.tl.ai.core.tools.model.messages.AnnotationEditorResponse setTarget(String value) {
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
	public com.top_logic.tl.ai.core.tools.model.messages.AnnotationEditorResponse setKind(String value) {
		internalSetKind(value);
		return this;
	}

	/** Internal setter for {@link #getKind()} without chain call utility. */
	protected final void internalSetKind(String value) {
		_kind = value;
	}

	/**
	 * Configuration interface class name.
	 */
	public final String getConfigInterface() {
		return _configInterface;
	}

	/**
	 * @see #getConfigInterface()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.AnnotationEditorResponse setConfigInterface(String value) {
		internalSetConfigInterface(value);
		return this;
	}

	/** Internal setter for {@link #getConfigInterface()} without chain call utility. */
	protected final void internalSetConfigInterface(String value) {
		_configInterface = value;
	}

	/**
	 * Human-readable result message.
	 */
	public final String getDetail() {
		return _detail;
	}

	/**
	 * @see #getDetail()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.AnnotationEditorResponse setDetail(String value) {
		internalSetDetail(value);
		return this;
	}

	/** Internal setter for {@link #getDetail()} without chain call utility. */
	protected final void internalSetDetail(String value) {
		_detail = value;
	}

	@Override
	public String jsonType() {
		return ANNOTATION_EDITOR_RESPONSE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			SUCCESS__PROP, 
			TARGET__PROP, 
			KIND__PROP, 
			CONFIG_INTERFACE__PROP, 
			DETAIL__PROP);
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
			case SUCCESS__PROP: return isSuccess();
			case TARGET__PROP: return getTarget();
			case KIND__PROP: return getKind();
			case CONFIG_INTERFACE__PROP: return getConfigInterface();
			case DETAIL__PROP: return getDetail();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case SUCCESS__PROP: internalSetSuccess((boolean) value); break;
			case TARGET__PROP: internalSetTarget((String) value); break;
			case KIND__PROP: internalSetKind((String) value); break;
			case CONFIG_INTERFACE__PROP: internalSetConfigInterface((String) value); break;
			case DETAIL__PROP: internalSetDetail((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.model.messages.AnnotationEditorResponse readAnnotationEditorResponse(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.model.messages.AnnotationEditorResponse result = new com.top_logic.tl.ai.core.tools.model.messages.AnnotationEditorResponse();
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
		out.name(SUCCESS__PROP);
		out.value(isSuccess());
		out.name(TARGET__PROP);
		out.value(getTarget());
		out.name(KIND__PROP);
		out.value(getKind());
		out.name(CONFIG_INTERFACE__PROP);
		out.value(getConfigInterface());
		out.name(DETAIL__PROP);
		out.value(getDetail());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case SUCCESS__PROP: setSuccess(in.nextBoolean()); break;
			case TARGET__PROP: setTarget(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case KIND__PROP: setKind(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CONFIG_INTERFACE__PROP: setConfigInterface(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DETAIL__PROP: setDetail(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
