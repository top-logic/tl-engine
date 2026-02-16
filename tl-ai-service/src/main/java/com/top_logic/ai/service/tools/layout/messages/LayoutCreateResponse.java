package com.top_logic.ai.service.tools.layout.messages;

/**
 * Response for a layout creation operation.
 */
public class LayoutCreateResponse extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.service.tools.layout.messages.LayoutCreateResponse} instance.
	 */
	public static com.top_logic.ai.service.tools.layout.messages.LayoutCreateResponse create() {
		return new com.top_logic.ai.service.tools.layout.messages.LayoutCreateResponse();
	}

	/** Identifier for the {@link com.top_logic.ai.service.tools.layout.messages.LayoutCreateResponse} type in JSON format. */
	public static final String LAYOUT_CREATE_RESPONSE__TYPE = "LayoutCreateResponse";

	/** @see #isSuccess() */
	public static final String SUCCESS__PROP = "success";

	/** @see #getLayoutKey() */
	public static final String LAYOUT_KEY__PROP = "layoutKey";

	/** @see #getDetail() */
	public static final String DETAIL__PROP = "detail";

	private boolean _success = false;

	private String _layoutKey = "";

	private String _detail = "";

	/**
	 * Creates a {@link LayoutCreateResponse} instance.
	 *
	 * @see com.top_logic.ai.service.tools.layout.messages.LayoutCreateResponse#create()
	 */
	protected LayoutCreateResponse() {
		super();
	}

	/**
	 * Whether the creation was successful.
	 */
	public final boolean isSuccess() {
		return _success;
	}

	/**
	 * @see #isSuccess()
	 */
	public com.top_logic.ai.service.tools.layout.messages.LayoutCreateResponse setSuccess(boolean value) {
		internalSetSuccess(value);
		return this;
	}

	/** Internal setter for {@link #isSuccess()} without chain call utility. */
	protected final void internalSetSuccess(boolean value) {
		_success = value;
	}

	/**
	 * The layout key of the created layout.
	 */
	public final String getLayoutKey() {
		return _layoutKey;
	}

	/**
	 * @see #getLayoutKey()
	 */
	public com.top_logic.ai.service.tools.layout.messages.LayoutCreateResponse setLayoutKey(String value) {
		internalSetLayoutKey(value);
		return this;
	}

	/** Internal setter for {@link #getLayoutKey()} without chain call utility. */
	protected final void internalSetLayoutKey(String value) {
		_layoutKey = value;
	}

	/**
	 * Detail text describing the result of the operation.
	 */
	public final String getDetail() {
		return _detail;
	}

	/**
	 * @see #getDetail()
	 */
	public com.top_logic.ai.service.tools.layout.messages.LayoutCreateResponse setDetail(String value) {
		internalSetDetail(value);
		return this;
	}

	/** Internal setter for {@link #getDetail()} without chain call utility. */
	protected final void internalSetDetail(String value) {
		_detail = value;
	}

	@Override
	public String jsonType() {
		return LAYOUT_CREATE_RESPONSE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			SUCCESS__PROP, 
			LAYOUT_KEY__PROP, 
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
			case LAYOUT_KEY__PROP: return getLayoutKey();
			case DETAIL__PROP: return getDetail();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case SUCCESS__PROP: internalSetSuccess((boolean) value); break;
			case LAYOUT_KEY__PROP: internalSetLayoutKey((String) value); break;
			case DETAIL__PROP: internalSetDetail((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.service.tools.layout.messages.LayoutCreateResponse readLayoutCreateResponse(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.service.tools.layout.messages.LayoutCreateResponse result = new com.top_logic.ai.service.tools.layout.messages.LayoutCreateResponse();
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
		out.name(LAYOUT_KEY__PROP);
		out.value(getLayoutKey());
		out.name(DETAIL__PROP);
		out.value(getDetail());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case SUCCESS__PROP: setSuccess(in.nextBoolean()); break;
			case LAYOUT_KEY__PROP: setLayoutKey(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DETAIL__PROP: setDetail(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
