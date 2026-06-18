package com.top_logic.tl.ai.core.tools.authoring.messages;

/**
 * Result of listing the agent-created TL-Script tools.
 */
public class ScriptToolListResult extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolListResult} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolListResult create() {
		return new com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolListResult();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolListResult} type in JSON format. */
	public static final String SCRIPT_TOOL_LIST_RESULT__TYPE = "ScriptToolListResult";

	/** @see #getTools() */
	public static final String TOOLS__PROP = "tools";

	private final java.util.List<com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolSummary> _tools = new java.util.ArrayList<>();

	/**
	 * Creates a {@link ScriptToolListResult} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolListResult#create()
	 */
	protected ScriptToolListResult() {
		super();
	}

	/**
	 * The TL-Script tools in the 'custom' factory.
	 */
	public final java.util.List<com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolSummary> getTools() {
		return _tools;
	}

	/**
	 * @see #getTools()
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolListResult setTools(java.util.List<? extends com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolSummary> value) {
		internalSetTools(value);
		return this;
	}

	/** Internal setter for {@link #getTools()} without chain call utility. */
	protected final void internalSetTools(java.util.List<? extends com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolSummary> value) {
		if (value == null) throw new IllegalArgumentException("Property 'tools' cannot be null.");
		_tools.clear();
		_tools.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getTools()} list.
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolListResult addTool(com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolSummary value) {
		internalAddTool(value);
		return this;
	}

	/** Implementation of {@link #addTool(com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolSummary)} without chain call utility. */
	protected final void internalAddTool(com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolSummary value) {
		_tools.add(value);
	}

	/**
	 * Removes a value from the {@link #getTools()} list.
	 */
	public final void removeTool(com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolSummary value) {
		_tools.remove(value);
	}

	@Override
	public String jsonType() {
		return SCRIPT_TOOL_LIST_RESULT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TOOLS__PROP);
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
			case TOOLS__PROP: return getTools();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TOOLS__PROP: internalSetTools(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolSummary.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolListResult readScriptToolListResult(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolListResult result = new com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolListResult();
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
		out.name(TOOLS__PROP);
		out.beginArray();
		for (com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolSummary x : getTools()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TOOLS__PROP: {
				java.util.List<com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolSummary> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolSummary.readScriptToolSummary(in));
				}
				in.endArray();
				setTools(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
