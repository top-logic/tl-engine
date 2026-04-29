package com.top_logic.tl.ai.core.tools.model.messages;

/**
 * List of modules response.
 */
public class ModuleListResponse extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.model.messages.ModuleListResponse} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.model.messages.ModuleListResponse create() {
		return new com.top_logic.tl.ai.core.tools.model.messages.ModuleListResponse();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.model.messages.ModuleListResponse} type in JSON format. */
	public static final String MODULE_LIST_RESPONSE__TYPE = "ModuleListResponse";

	/** @see #getModules() */
	public static final String MODULES__PROP = "modules";

	private final java.util.List<com.top_logic.tl.ai.core.tools.model.messages.ModuleInfo> _modules = new java.util.ArrayList<>();

	/**
	 * Creates a {@link ModuleListResponse} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.model.messages.ModuleListResponse#create()
	 */
	protected ModuleListResponse() {
		super();
	}

	/**
	 * Array of module information.
	 */
	public final java.util.List<com.top_logic.tl.ai.core.tools.model.messages.ModuleInfo> getModules() {
		return _modules;
	}

	/**
	 * @see #getModules()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.ModuleListResponse setModules(java.util.List<? extends com.top_logic.tl.ai.core.tools.model.messages.ModuleInfo> value) {
		internalSetModules(value);
		return this;
	}

	/** Internal setter for {@link #getModules()} without chain call utility. */
	protected final void internalSetModules(java.util.List<? extends com.top_logic.tl.ai.core.tools.model.messages.ModuleInfo> value) {
		if (value == null) throw new IllegalArgumentException("Property 'modules' cannot be null.");
		_modules.clear();
		_modules.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getModules()} list.
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.ModuleListResponse addModule(com.top_logic.tl.ai.core.tools.model.messages.ModuleInfo value) {
		internalAddModule(value);
		return this;
	}

	/** Implementation of {@link #addModule(com.top_logic.tl.ai.core.tools.model.messages.ModuleInfo)} without chain call utility. */
	protected final void internalAddModule(com.top_logic.tl.ai.core.tools.model.messages.ModuleInfo value) {
		_modules.add(value);
	}

	/**
	 * Removes a value from the {@link #getModules()} list.
	 */
	public final void removeModule(com.top_logic.tl.ai.core.tools.model.messages.ModuleInfo value) {
		_modules.remove(value);
	}

	@Override
	public String jsonType() {
		return MODULE_LIST_RESPONSE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			MODULES__PROP);
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
			case MODULES__PROP: return getModules();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case MODULES__PROP: internalSetModules(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.tl.ai.core.tools.model.messages.ModuleInfo.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.model.messages.ModuleListResponse readModuleListResponse(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.model.messages.ModuleListResponse result = new com.top_logic.tl.ai.core.tools.model.messages.ModuleListResponse();
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
		out.name(MODULES__PROP);
		out.beginArray();
		for (com.top_logic.tl.ai.core.tools.model.messages.ModuleInfo x : getModules()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case MODULES__PROP: {
				java.util.List<com.top_logic.tl.ai.core.tools.model.messages.ModuleInfo> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.tl.ai.core.tools.model.messages.ModuleInfo.readModuleInfo(in));
				}
				in.endArray();
				setModules(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
