package com.top_logic.layout.react.protocol.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.protocol.FunctionCall}.
 */
public class FunctionCall_Impl extends com.top_logic.layout.react.protocol.impl.SSEEvent_Impl implements com.top_logic.layout.react.protocol.FunctionCall {

	private String _elementId = "";

	private String _functionRef = "";

	private String _functionName = "";

	private String _arguments = "";

	/**
	 * Creates a {@link FunctionCall_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.protocol.FunctionCall#create()
	 */
	public FunctionCall_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.FUNCTION_CALL;
	}

	@Override
	public final String getElementId() {
		return _elementId;
	}

	@Override
	public com.top_logic.layout.react.protocol.FunctionCall setElementId(String value) {
		internalSetElementId(value);
		return this;
	}

	/** Internal setter for {@link #getElementId()} without chain call utility. */
	protected final void internalSetElementId(String value) {
		_listener.beforeSet(this, ELEMENT_ID__PROP, value);
		_elementId = value;
		_listener.afterChanged(this, ELEMENT_ID__PROP);
	}

	@Override
	public final String getFunctionRef() {
		return _functionRef;
	}

	@Override
	public com.top_logic.layout.react.protocol.FunctionCall setFunctionRef(String value) {
		internalSetFunctionRef(value);
		return this;
	}

	/** Internal setter for {@link #getFunctionRef()} without chain call utility. */
	protected final void internalSetFunctionRef(String value) {
		_listener.beforeSet(this, FUNCTION_REF__PROP, value);
		_functionRef = value;
		_listener.afterChanged(this, FUNCTION_REF__PROP);
	}

	@Override
	public final String getFunctionName() {
		return _functionName;
	}

	@Override
	public com.top_logic.layout.react.protocol.FunctionCall setFunctionName(String value) {
		internalSetFunctionName(value);
		return this;
	}

	/** Internal setter for {@link #getFunctionName()} without chain call utility. */
	protected final void internalSetFunctionName(String value) {
		_listener.beforeSet(this, FUNCTION_NAME__PROP, value);
		_functionName = value;
		_listener.afterChanged(this, FUNCTION_NAME__PROP);
	}

	@Override
	public final String getArguments() {
		return _arguments;
	}

	@Override
	public com.top_logic.layout.react.protocol.FunctionCall setArguments(String value) {
		internalSetArguments(value);
		return this;
	}

	/** Internal setter for {@link #getArguments()} without chain call utility. */
	protected final void internalSetArguments(String value) {
		_listener.beforeSet(this, ARGUMENTS__PROP, value);
		_arguments = value;
		_listener.afterChanged(this, ARGUMENTS__PROP);
	}

	@Override
	public String jsonType() {
		return FUNCTION_CALL__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			ELEMENT_ID__PROP, 
			FUNCTION_REF__PROP, 
			FUNCTION_NAME__PROP, 
			ARGUMENTS__PROP);
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
			case ELEMENT_ID__PROP: return getElementId();
			case FUNCTION_REF__PROP: return getFunctionRef();
			case FUNCTION_NAME__PROP: return getFunctionName();
			case ARGUMENTS__PROP: return getArguments();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ELEMENT_ID__PROP: internalSetElementId((String) value); break;
			case FUNCTION_REF__PROP: internalSetFunctionRef((String) value); break;
			case FUNCTION_NAME__PROP: internalSetFunctionName((String) value); break;
			case ARGUMENTS__PROP: internalSetArguments((String) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(ELEMENT_ID__PROP);
		out.value(getElementId());
		out.name(FUNCTION_REF__PROP);
		out.value(getFunctionRef());
		out.name(FUNCTION_NAME__PROP);
		out.value(getFunctionName());
		out.name(ARGUMENTS__PROP);
		out.value(getArguments());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ELEMENT_ID__PROP: setElementId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case FUNCTION_REF__PROP: setFunctionRef(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case FUNCTION_NAME__PROP: setFunctionName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ARGUMENTS__PROP: setArguments(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.layout.react.protocol.SSEEvent.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
