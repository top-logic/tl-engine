package com.top_logic.tl.ai.core.tools.model.messages;

/**
 * The instances {@code getInstance} was asked for, in the order they were named.
 */
public class InstancesResponse extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.model.messages.InstancesResponse} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.model.messages.InstancesResponse create() {
		return new com.top_logic.tl.ai.core.tools.model.messages.InstancesResponse();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.model.messages.InstancesResponse} type in JSON format. */
	public static final String INSTANCES_RESPONSE__TYPE = "InstancesResponse";

	/** @see #getInstances() */
	public static final String INSTANCES__PROP = "instances";

	/** @see #getErrors() */
	public static final String ERRORS__PROP = "errors";

	private final java.util.List<com.top_logic.tl.ai.core.tools.model.messages.InstanceResponse> _instances = new java.util.ArrayList<>();

	private final java.util.List<String> _errors = new java.util.ArrayList<>();

	/**
	 * Creates a {@link InstancesResponse} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.model.messages.InstancesResponse#create()
	 */
	protected InstancesResponse() {
		super();
	}

	/**
	 * The instances that were found and may be read.
	 */
	public final java.util.List<com.top_logic.tl.ai.core.tools.model.messages.InstanceResponse> getInstances() {
		return _instances;
	}

	/**
	 * @see #getInstances()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.InstancesResponse setInstances(java.util.List<? extends com.top_logic.tl.ai.core.tools.model.messages.InstanceResponse> value) {
		internalSetInstances(value);
		return this;
	}

	/** Internal setter for {@link #getInstances()} without chain call utility. */
	protected final void internalSetInstances(java.util.List<? extends com.top_logic.tl.ai.core.tools.model.messages.InstanceResponse> value) {
		if (value == null) throw new IllegalArgumentException("Property 'instances' cannot be null.");
		_instances.clear();
		_instances.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getInstances()} list.
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.InstancesResponse addInstance(com.top_logic.tl.ai.core.tools.model.messages.InstanceResponse value) {
		internalAddInstance(value);
		return this;
	}

	/** Implementation of {@link #addInstance(com.top_logic.tl.ai.core.tools.model.messages.InstanceResponse)} without chain call utility. */
	protected final void internalAddInstance(com.top_logic.tl.ai.core.tools.model.messages.InstanceResponse value) {
		_instances.add(value);
	}

	/**
	 * Removes a value from the {@link #getInstances()} list.
	 */
	public final void removeInstance(com.top_logic.tl.ai.core.tools.model.messages.InstanceResponse value) {
		_instances.remove(value);
	}

	/**
	 * One sentence per identifier that named nothing the acting account may read.
	 */
	public final java.util.List<String> getErrors() {
		return _errors;
	}

	/**
	 * @see #getErrors()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.InstancesResponse setErrors(java.util.List<? extends String> value) {
		internalSetErrors(value);
		return this;
	}

	/** Internal setter for {@link #getErrors()} without chain call utility. */
	protected final void internalSetErrors(java.util.List<? extends String> value) {
		_errors.clear();
		_errors.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getErrors()} list.
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.InstancesResponse addError(String value) {
		internalAddError(value);
		return this;
	}

	/** Implementation of {@link #addError(String)} without chain call utility. */
	protected final void internalAddError(String value) {
		_errors.add(value);
	}

	/**
	 * Removes a value from the {@link #getErrors()} list.
	 */
	public final void removeError(String value) {
		_errors.remove(value);
	}

	@Override
	public String jsonType() {
		return INSTANCES_RESPONSE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			INSTANCES__PROP, 
			ERRORS__PROP);
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
			case INSTANCES__PROP: return getInstances();
			case ERRORS__PROP: return getErrors();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case INSTANCES__PROP: internalSetInstances(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.tl.ai.core.tools.model.messages.InstanceResponse.class, value)); break;
			case ERRORS__PROP: internalSetErrors(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.model.messages.InstancesResponse readInstancesResponse(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.model.messages.InstancesResponse result = new com.top_logic.tl.ai.core.tools.model.messages.InstancesResponse();
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
		out.name(INSTANCES__PROP);
		out.beginArray();
		for (com.top_logic.tl.ai.core.tools.model.messages.InstanceResponse x : getInstances()) {
			x.writeTo(out);
		}
		out.endArray();
		out.name(ERRORS__PROP);
		out.beginArray();
		for (String x : getErrors()) {
			out.value(x);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case INSTANCES__PROP: {
				java.util.List<com.top_logic.tl.ai.core.tools.model.messages.InstanceResponse> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.tl.ai.core.tools.model.messages.InstanceResponse.readInstanceResponse(in));
				}
				in.endArray();
				setInstances(newValue);
			}
			break;
			case ERRORS__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setErrors(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
