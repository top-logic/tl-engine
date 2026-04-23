package com.top_logic.tl.ai.core.tools.model.messages;

/**
 * Reference to another TLObject.
 */
public class TLObjectReference extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.model.messages.TLObjectReference} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.model.messages.TLObjectReference create() {
		return new com.top_logic.tl.ai.core.tools.model.messages.TLObjectReference();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.model.messages.TLObjectReference} type in JSON format. */
	public static final String TLOBJECT_REFERENCE__TYPE = "TLObjectReference";

	/** @see #getTid() */
	public static final String TID__PROP = "tid";

	/** @see #getType() */
	public static final String TYPE__PROP = "type";

	/** @see #getLabel() */
	public static final String LABEL__PROP = "label";

	private String _tid = "";

	private String _type = "";

	private String _label = "";

	/**
	 * Creates a {@link TLObjectReference} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.model.messages.TLObjectReference#create()
	 */
	protected TLObjectReference() {
		super();
	}

	/**
	 * Object TID.
	 */
	public final String getTid() {
		return _tid;
	}

	/**
	 * @see #getTid()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.TLObjectReference setTid(String value) {
		internalSetTid(value);
		return this;
	}

	/** Internal setter for {@link #getTid()} without chain call utility. */
	protected final void internalSetTid(String value) {
		_tid = value;
	}

	/**
	 * Type of the referenced object.
	 */
	public final String getType() {
		return _type;
	}

	/**
	 * @see #getType()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.TLObjectReference setType(String value) {
		internalSetType(value);
		return this;
	}

	/** Internal setter for {@link #getType()} without chain call utility. */
	protected final void internalSetType(String value) {
		_type = value;
	}

	/**
	 * Label of the referenced object (if available).
	 */
	public final String getLabel() {
		return _label;
	}

	/**
	 * @see #getLabel()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.TLObjectReference setLabel(String value) {
		internalSetLabel(value);
		return this;
	}

	/** Internal setter for {@link #getLabel()} without chain call utility. */
	protected final void internalSetLabel(String value) {
		_label = value;
	}

	@Override
	public String jsonType() {
		return TLOBJECT_REFERENCE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TID__PROP, 
			TYPE__PROP, 
			LABEL__PROP);
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
			case TID__PROP: return getTid();
			case TYPE__PROP: return getType();
			case LABEL__PROP: return getLabel();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TID__PROP: internalSetTid((String) value); break;
			case TYPE__PROP: internalSetType((String) value); break;
			case LABEL__PROP: internalSetLabel((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.model.messages.TLObjectReference readTLObjectReference(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.model.messages.TLObjectReference result = new com.top_logic.tl.ai.core.tools.model.messages.TLObjectReference();
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
		out.name(TID__PROP);
		out.value(getTid());
		out.name(TYPE__PROP);
		out.value(getType());
		out.name(LABEL__PROP);
		out.value(getLabel());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TID__PROP: setTid(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TYPE__PROP: setType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case LABEL__PROP: setLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
