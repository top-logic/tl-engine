package com.top_logic.tl.ai.core.tools.authoring.messages;

/**
 * Result of listing the factories that hold registered TL-Script tools.
 */
public class ScriptToolFactoryListResult extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolFactoryListResult} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolFactoryListResult create() {
		return new com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolFactoryListResult();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolFactoryListResult} type in JSON format. */
	public static final String SCRIPT_TOOL_FACTORY_LIST_RESULT__TYPE = "ScriptToolFactoryListResult";

	/** @see #getFactoryNames() */
	public static final String FACTORY_NAMES__PROP = "factoryNames";

	private final java.util.List<String> _factoryNames = new java.util.ArrayList<>();

	/**
	 * Creates a {@link ScriptToolFactoryListResult} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolFactoryListResult#create()
	 */
	protected ScriptToolFactoryListResult() {
		super();
	}

	/**
	 * Distinct factory names that contain at least one registered TL-Script tool.
	 */
	public final java.util.List<String> getFactoryNames() {
		return _factoryNames;
	}

	/**
	 * @see #getFactoryNames()
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolFactoryListResult setFactoryNames(java.util.List<? extends String> value) {
		internalSetFactoryNames(value);
		return this;
	}

	/** Internal setter for {@link #getFactoryNames()} without chain call utility. */
	protected final void internalSetFactoryNames(java.util.List<? extends String> value) {
		_factoryNames.clear();
		_factoryNames.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getFactoryNames()} list.
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolFactoryListResult addFactoryName(String value) {
		internalAddFactoryName(value);
		return this;
	}

	/** Implementation of {@link #addFactoryName(String)} without chain call utility. */
	protected final void internalAddFactoryName(String value) {
		_factoryNames.add(value);
	}

	/**
	 * Removes a value from the {@link #getFactoryNames()} list.
	 */
	public final void removeFactoryName(String value) {
		_factoryNames.remove(value);
	}

	@Override
	public String jsonType() {
		return SCRIPT_TOOL_FACTORY_LIST_RESULT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			FACTORY_NAMES__PROP);
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
			case FACTORY_NAMES__PROP: return getFactoryNames();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case FACTORY_NAMES__PROP: internalSetFactoryNames(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolFactoryListResult readScriptToolFactoryListResult(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolFactoryListResult result = new com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolFactoryListResult();
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
		out.name(FACTORY_NAMES__PROP);
		out.beginArray();
		for (String x : getFactoryNames()) {
			out.value(x);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case FACTORY_NAMES__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setFactoryNames(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
