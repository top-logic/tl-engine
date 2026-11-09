package com.top_logic.tl.ai.core.tools.refusal.messages;

/**
 * One reason the application gave for a refusal.
 */
public class Reason extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.refusal.messages.Reason} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.refusal.messages.Reason create() {
		return new com.top_logic.tl.ai.core.tools.refusal.messages.Reason();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.refusal.messages.Reason} type in JSON format. */
	public static final String REASON__TYPE = "Reason";

	/** @see #getObject() */
	public static final String OBJECT__PROP = "object";

	/** @see #getPlace() */
	public static final String PLACE__PROP = "place";

	/** @see #getAttribute() */
	public static final String ATTRIBUTE__PROP = "attribute";

	/** @see #getText() */
	public static final String TEXT__PROP = "text";

	private String _object = null;

	private String _place = null;

	private String _attribute = null;

	private String _text = "";

	/**
	 * Creates a {@link Reason} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.refusal.messages.Reason#create()
	 */
	protected Reason() {
		super();
	}

	/**
	 * The object the reason is about, as <code>label [id:TID]</code>, if the refusal covers several
	 * objects.
	 */
	public final String getObject() {
		return _object;
	}

	/**
	 * @see #getObject()
	 */
	public com.top_logic.tl.ai.core.tools.refusal.messages.Reason setObject(String value) {
		internalSetObject(value);
		return this;
	}

	/** Internal setter for {@link #getObject()} without chain call utility. */
	protected final void internalSetObject(String value) {
		_object = value;
	}

	/**
	 * Checks, whether {@link #getObject()} has a value.
	 */
	public final boolean hasObject() {
		return _object != null;
	}

	/**
	 * Where the object stands in the call, if it is a part of the object a call creates.
	 */
	public final String getPlace() {
		return _place;
	}

	/**
	 * @see #getPlace()
	 */
	public com.top_logic.tl.ai.core.tools.refusal.messages.Reason setPlace(String value) {
		internalSetPlace(value);
		return this;
	}

	/** Internal setter for {@link #getPlace()} without chain call utility. */
	protected final void internalSetPlace(String value) {
		_place = value;
	}

	/**
	 * Checks, whether {@link #getPlace()} has a value.
	 */
	public final boolean hasPlace() {
		return _place != null;
	}

	/**
	 * The name of the attribute the reason is about, if it is about one.
	 */
	public final String getAttribute() {
		return _attribute;
	}

	/**
	 * @see #getAttribute()
	 */
	public com.top_logic.tl.ai.core.tools.refusal.messages.Reason setAttribute(String value) {
		internalSetAttribute(value);
		return this;
	}

	/** Internal setter for {@link #getAttribute()} without chain call utility. */
	protected final void internalSetAttribute(String value) {
		_attribute = value;
	}

	/**
	 * Checks, whether {@link #getAttribute()} has a value.
	 */
	public final boolean hasAttribute() {
		return _attribute != null;
	}

	/**
	 * The application's message, in the language of the run's user.
	 */
	public final String getText() {
		return _text;
	}

	/**
	 * @see #getText()
	 */
	public com.top_logic.tl.ai.core.tools.refusal.messages.Reason setText(String value) {
		internalSetText(value);
		return this;
	}

	/** Internal setter for {@link #getText()} without chain call utility. */
	protected final void internalSetText(String value) {
		_text = value;
	}

	@Override
	public String jsonType() {
		return REASON__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			OBJECT__PROP, 
			PLACE__PROP, 
			ATTRIBUTE__PROP, 
			TEXT__PROP);
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
			case OBJECT__PROP: return getObject();
			case PLACE__PROP: return getPlace();
			case ATTRIBUTE__PROP: return getAttribute();
			case TEXT__PROP: return getText();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case OBJECT__PROP: internalSetObject((String) value); break;
			case PLACE__PROP: internalSetPlace((String) value); break;
			case ATTRIBUTE__PROP: internalSetAttribute((String) value); break;
			case TEXT__PROP: internalSetText((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.refusal.messages.Reason readReason(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.refusal.messages.Reason result = new com.top_logic.tl.ai.core.tools.refusal.messages.Reason();
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
		if (hasObject()) {
			out.name(OBJECT__PROP);
			out.value(getObject());
		}
		if (hasPlace()) {
			out.name(PLACE__PROP);
			out.value(getPlace());
		}
		if (hasAttribute()) {
			out.name(ATTRIBUTE__PROP);
			out.value(getAttribute());
		}
		out.name(TEXT__PROP);
		out.value(getText());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case OBJECT__PROP: setObject(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case PLACE__PROP: setPlace(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ATTRIBUTE__PROP: setAttribute(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TEXT__PROP: setText(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
