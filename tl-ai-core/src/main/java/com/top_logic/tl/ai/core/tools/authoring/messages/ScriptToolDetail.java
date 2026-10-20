package com.top_logic.tl.ai.core.tools.authoring.messages;

/**
 * Full detail of a single TL-Script tool.
 */
public class ScriptToolDetail extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolDetail} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolDetail create() {
		return new com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolDetail();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolDetail} type in JSON format. */
	public static final String SCRIPT_TOOL_DETAIL__TYPE = "ScriptToolDetail";

	/** @see #isExists() */
	public static final String EXISTS__PROP = "exists";

	/** @see #getName() */
	public static final String NAME__PROP = "name";

	/** @see #getLabel() */
	public static final String LABEL__PROP = "label";

	/** @see #getDescription() */
	public static final String DESCRIPTION__PROP = "description";

	/** @see #getImplementation() */
	public static final String IMPLEMENTATION__PROP = "implementation";

	/** @see #getMode() */
	public static final String MODE__PROP = "mode";

	/** @see #isConcurrencySafe() */
	public static final String CONCURRENCY_SAFE__PROP = "concurrencySafe";

	/** @see #getParameters() */
	public static final String PARAMETERS__PROP = "parameters";

	private boolean _exists = false;

	private String _name = "";

	private String _label = "";

	private String _description = "";

	private String _implementation = "";

	private String _mode = "";

	private boolean _concurrencySafe = false;

	private final java.util.List<com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolParamInfo> _parameters = new java.util.ArrayList<>();

	/**
	 * Creates a {@link ScriptToolDetail} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolDetail#create()
	 */
	protected ScriptToolDetail() {
		super();
	}

	/**
	 * Whether a tool with this name exists in the 'custom' factory.
	 */
	public final boolean isExists() {
		return _exists;
	}

	/**
	 * @see #isExists()
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolDetail setExists(boolean value) {
		internalSetExists(value);
		return this;
	}

	/** Internal setter for {@link #isExists()} without chain call utility. */
	protected final void internalSetExists(boolean value) {
		_exists = value;
	}

	/**
	 * Tool name.
	 */
	public final String getName() {
		return _name;
	}

	/**
	 * @see #getName()
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolDetail setName(String value) {
		internalSetName(value);
		return this;
	}

	/** Internal setter for {@link #getName()} without chain call utility. */
	protected final void internalSetName(String value) {
		_name = value;
	}

	/**
	 * Human-readable label.
	 */
	public final String getLabel() {
		return _label;
	}

	/**
	 * @see #getLabel()
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolDetail setLabel(String value) {
		internalSetLabel(value);
		return this;
	}

	/** Internal setter for {@link #getLabel()} without chain call utility. */
	protected final void internalSetLabel(String value) {
		_label = value;
	}

	/**
	 * Tool description.
	 */
	public final String getDescription() {
		return _description;
	}

	/**
	 * @see #getDescription()
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolDetail setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	/** Internal setter for {@link #getDescription()} without chain call utility. */
	protected final void internalSetDescription(String value) {
		_description = value;
	}

	/**
	 * TL-Script implementation source.
	 */
	public final String getImplementation() {
		return _implementation;
	}

	/**
	 * @see #getImplementation()
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolDetail setImplementation(String value) {
		internalSetImplementation(value);
		return this;
	}

	/** Internal setter for {@link #getImplementation()} without chain call utility. */
	protected final void internalSetImplementation(String value) {
		_implementation = value;
	}

	/**
	 * Tool mode.
	 */
	public final String getMode() {
		return _mode;
	}

	/**
	 * @see #getMode()
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolDetail setMode(String value) {
		internalSetMode(value);
		return this;
	}

	/** Internal setter for {@link #getMode()} without chain call utility. */
	protected final void internalSetMode(String value) {
		_mode = value;
	}

	/**
	 * Whether the tool may run in parallel with other concurrency-safe tools.
	 */
	public final boolean isConcurrencySafe() {
		return _concurrencySafe;
	}

	/**
	 * @see #isConcurrencySafe()
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolDetail setConcurrencySafe(boolean value) {
		internalSetConcurrencySafe(value);
		return this;
	}

	/** Internal setter for {@link #isConcurrencySafe()} without chain call utility. */
	protected final void internalSetConcurrencySafe(boolean value) {
		_concurrencySafe = value;
	}

	/**
	 * Declared parameters.
	 */
	public final java.util.List<com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolParamInfo> getParameters() {
		return _parameters;
	}

	/**
	 * @see #getParameters()
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolDetail setParameters(java.util.List<? extends com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolParamInfo> value) {
		internalSetParameters(value);
		return this;
	}

	/** Internal setter for {@link #getParameters()} without chain call utility. */
	protected final void internalSetParameters(java.util.List<? extends com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolParamInfo> value) {
		if (value == null) throw new IllegalArgumentException("Property 'parameters' cannot be null.");
		_parameters.clear();
		_parameters.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getParameters()} list.
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolDetail addParameter(com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolParamInfo value) {
		internalAddParameter(value);
		return this;
	}

	/** Implementation of {@link #addParameter(com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolParamInfo)} without chain call utility. */
	protected final void internalAddParameter(com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolParamInfo value) {
		_parameters.add(value);
	}

	/**
	 * Removes a value from the {@link #getParameters()} list.
	 */
	public final void removeParameter(com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolParamInfo value) {
		_parameters.remove(value);
	}

	@Override
	public String jsonType() {
		return SCRIPT_TOOL_DETAIL__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			EXISTS__PROP, 
			NAME__PROP, 
			LABEL__PROP, 
			DESCRIPTION__PROP, 
			IMPLEMENTATION__PROP, 
			MODE__PROP, 
			CONCURRENCY_SAFE__PROP, 
			PARAMETERS__PROP);
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
			case EXISTS__PROP: return isExists();
			case NAME__PROP: return getName();
			case LABEL__PROP: return getLabel();
			case DESCRIPTION__PROP: return getDescription();
			case IMPLEMENTATION__PROP: return getImplementation();
			case MODE__PROP: return getMode();
			case CONCURRENCY_SAFE__PROP: return isConcurrencySafe();
			case PARAMETERS__PROP: return getParameters();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case EXISTS__PROP: internalSetExists((boolean) value); break;
			case NAME__PROP: internalSetName((String) value); break;
			case LABEL__PROP: internalSetLabel((String) value); break;
			case DESCRIPTION__PROP: internalSetDescription((String) value); break;
			case IMPLEMENTATION__PROP: internalSetImplementation((String) value); break;
			case MODE__PROP: internalSetMode((String) value); break;
			case CONCURRENCY_SAFE__PROP: internalSetConcurrencySafe((boolean) value); break;
			case PARAMETERS__PROP: internalSetParameters(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolParamInfo.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolDetail readScriptToolDetail(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolDetail result = new com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolDetail();
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
		out.name(EXISTS__PROP);
		out.value(isExists());
		out.name(NAME__PROP);
		out.value(getName());
		out.name(LABEL__PROP);
		out.value(getLabel());
		out.name(DESCRIPTION__PROP);
		out.value(getDescription());
		out.name(IMPLEMENTATION__PROP);
		out.value(getImplementation());
		out.name(MODE__PROP);
		out.value(getMode());
		out.name(CONCURRENCY_SAFE__PROP);
		out.value(isConcurrencySafe());
		out.name(PARAMETERS__PROP);
		out.beginArray();
		for (com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolParamInfo x : getParameters()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case EXISTS__PROP: setExists(in.nextBoolean()); break;
			case NAME__PROP: setName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case LABEL__PROP: setLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DESCRIPTION__PROP: setDescription(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case IMPLEMENTATION__PROP: setImplementation(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case MODE__PROP: setMode(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CONCURRENCY_SAFE__PROP: setConcurrencySafe(in.nextBoolean()); break;
			case PARAMETERS__PROP: {
				java.util.List<com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolParamInfo> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolParamInfo.readScriptToolParamInfo(in));
				}
				in.endArray();
				setParameters(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
