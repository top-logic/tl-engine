package com.top_logic.tl.ai.core.tools.model.messages;

/**
 * Instance metadata.
 */
public class InstanceMetadata extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.model.messages.InstanceMetadata} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.model.messages.InstanceMetadata create() {
		return new com.top_logic.tl.ai.core.tools.model.messages.InstanceMetadata();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.model.messages.InstanceMetadata} type in JSON format. */
	public static final String INSTANCE_METADATA__TYPE = "InstanceMetadata";

	/** @see #getContainer() */
	public static final String CONTAINER__PROP = "container";

	/** @see #getContainerReference() */
	public static final String CONTAINER_REFERENCE__PROP = "containerReference";

	/** @see #isTransient() */
	public static final String TRANSIENT__PROP = "transient";

	private String _container = "";

	private String _containerReference = "";

	private boolean _transient = false;

	/**
	 * Creates a {@link InstanceMetadata} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.model.messages.InstanceMetadata#create()
	 */
	protected InstanceMetadata() {
		super();
	}

	/**
	 * Container TID, if the instance is contained and the acting account may read the container.
	 */
	public final String getContainer() {
		return _container;
	}

	/**
	 * @see #getContainer()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.InstanceMetadata setContainer(String value) {
		internalSetContainer(value);
		return this;
	}

	/** Internal setter for {@link #getContainer()} without chain call utility. */
	protected final void internalSetContainer(String value) {
		_container = value;
	}

	/**
	 * Name of the container reference.
	 */
	public final String getContainerReference() {
		return _containerReference;
	}

	/**
	 * @see #getContainerReference()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.InstanceMetadata setContainerReference(String value) {
		internalSetContainerReference(value);
		return this;
	}

	/** Internal setter for {@link #getContainerReference()} without chain call utility. */
	protected final void internalSetContainerReference(String value) {
		_containerReference = value;
	}

	/**
	 * Whether the instance is transient.
	 */
	public final boolean isTransient() {
		return _transient;
	}

	/**
	 * @see #isTransient()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.InstanceMetadata setTransient(boolean value) {
		internalSetTransient(value);
		return this;
	}

	/** Internal setter for {@link #isTransient()} without chain call utility. */
	protected final void internalSetTransient(boolean value) {
		_transient = value;
	}

	@Override
	public String jsonType() {
		return INSTANCE_METADATA__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			CONTAINER__PROP, 
			CONTAINER_REFERENCE__PROP, 
			TRANSIENT__PROP);
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
			case CONTAINER__PROP: return getContainer();
			case CONTAINER_REFERENCE__PROP: return getContainerReference();
			case TRANSIENT__PROP: return isTransient();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case CONTAINER__PROP: internalSetContainer((String) value); break;
			case CONTAINER_REFERENCE__PROP: internalSetContainerReference((String) value); break;
			case TRANSIENT__PROP: internalSetTransient((boolean) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.model.messages.InstanceMetadata readInstanceMetadata(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.model.messages.InstanceMetadata result = new com.top_logic.tl.ai.core.tools.model.messages.InstanceMetadata();
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
		out.name(CONTAINER__PROP);
		out.value(getContainer());
		out.name(CONTAINER_REFERENCE__PROP);
		out.value(getContainerReference());
		out.name(TRANSIENT__PROP);
		out.value(isTransient());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case CONTAINER__PROP: setContainer(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CONTAINER_REFERENCE__PROP: setContainerReference(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TRANSIENT__PROP: setTransient(in.nextBoolean()); break;
			default: super.readField(in, field);
		}
	}

}
