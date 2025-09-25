package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.TreeConnector}.
 */
public class TreeConnector_Impl extends com.top_logic.graphic.flow.data.impl.Widget_Impl implements com.top_logic.graphic.flow.data.TreeConnector {

	private com.top_logic.graphic.flow.data.TreeConnection _connection = null;

	private com.top_logic.graphic.flow.data.Box _anchor = null;

	private double _connectPosition = 0.5;

	private com.top_logic.graphic.flow.data.ConnectorSymbol _symbol = com.top_logic.graphic.flow.data.ConnectorSymbol.NONE;

	/**
	 * Creates a {@link TreeConnector_Impl} instance.
	 *
	 * @see com.top_logic.graphic.flow.data.TreeConnector#create()
	 */
	public TreeConnector_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.TREE_CONNECTOR;
	}

	@Override
	public final com.top_logic.graphic.flow.data.TreeConnection getConnection() {
		return _connection;
	}

	/**
	 * Internal setter for updating derived field.
	 */
	com.top_logic.graphic.flow.data.TreeConnector setConnection(com.top_logic.graphic.flow.data.TreeConnection value) {
		internalSetConnection(value);
		return this;
	}

	/** Internal setter for {@link #getConnection()} without chain call utility. */
	protected final void internalSetConnection(com.top_logic.graphic.flow.data.TreeConnection value) {
		_listener.beforeSet(this, CONNECTION__PROP, value);
		if (value != null && _connection != null) {
			throw new IllegalStateException("Object may not be part of two different containers.");
		}
		_connection = value;
		_listener.afterChanged(this, CONNECTION__PROP);
	}

	@Override
	public final boolean hasConnection() {
		return _connection != null;
	}

	@Override
	public final com.top_logic.graphic.flow.data.Box getAnchor() {
		return _anchor;
	}

	@Override
	public com.top_logic.graphic.flow.data.TreeConnector setAnchor(com.top_logic.graphic.flow.data.Box value) {
		internalSetAnchor(value);
		return this;
	}

	/** Internal setter for {@link #getAnchor()} without chain call utility. */
	protected final void internalSetAnchor(com.top_logic.graphic.flow.data.Box value) {
		_listener.beforeSet(this, ANCHOR__PROP, value);
		_anchor = value;
		_listener.afterChanged(this, ANCHOR__PROP);
	}

	@Override
	public final boolean hasAnchor() {
		return _anchor != null;
	}

	@Override
	public final double getConnectPosition() {
		return _connectPosition;
	}

	@Override
	public com.top_logic.graphic.flow.data.TreeConnector setConnectPosition(double value) {
		internalSetConnectPosition(value);
		return this;
	}

	/** Internal setter for {@link #getConnectPosition()} without chain call utility. */
	protected final void internalSetConnectPosition(double value) {
		_listener.beforeSet(this, CONNECT_POSITION__PROP, value);
		_connectPosition = value;
		_listener.afterChanged(this, CONNECT_POSITION__PROP);
	}

	@Override
	public final com.top_logic.graphic.flow.data.ConnectorSymbol getSymbol() {
		return _symbol;
	}

	@Override
	public com.top_logic.graphic.flow.data.TreeConnector setSymbol(com.top_logic.graphic.flow.data.ConnectorSymbol value) {
		internalSetSymbol(value);
		return this;
	}

	/** Internal setter for {@link #getSymbol()} without chain call utility. */
	protected final void internalSetSymbol(com.top_logic.graphic.flow.data.ConnectorSymbol value) {
		if (value == null) throw new IllegalArgumentException("Property 'symbol' cannot be null.");
		_listener.beforeSet(this, SYMBOL__PROP, value);
		_symbol = value;
		_listener.afterChanged(this, SYMBOL__PROP);
	}

	@Override
	public com.top_logic.graphic.flow.data.TreeConnector setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.TreeConnector setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.TreeConnector setClientId(String value) {
		internalSetClientId(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.TreeConnector setRenderInfo(java.lang.Object value) {
		internalSetRenderInfo(value);
		return this;
	}

	@Override
	public String jsonType() {
		return TREE_CONNECTOR__TYPE;
	}

	@SuppressWarnings("hiding")
	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			CONNECTION__PROP, 
			ANCHOR__PROP, 
			CONNECT_POSITION__PROP, 
			SYMBOL__PROP);
		java.util.List<String> tmp = new java.util.ArrayList<>();
		tmp.addAll(com.top_logic.graphic.flow.data.impl.Widget_Impl.PROPERTIES);
		tmp.addAll(local);
		PROPERTIES = java.util.Collections.unmodifiableList(tmp);
	}

	@SuppressWarnings("hiding")
	static final java.util.Set<String> TRANSIENT_PROPERTIES;
	static {
		java.util.HashSet<String> tmp = new java.util.HashSet<>();
		tmp.addAll(com.top_logic.graphic.flow.data.impl.Widget_Impl.TRANSIENT_PROPERTIES);
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
			case CONNECTION__PROP: return getConnection();
			case ANCHOR__PROP: return getAnchor();
			case CONNECT_POSITION__PROP: return getConnectPosition();
			case SYMBOL__PROP: return getSymbol();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ANCHOR__PROP: internalSetAnchor((com.top_logic.graphic.flow.data.Box) value); break;
			case CONNECT_POSITION__PROP: internalSetConnectPosition((double) value); break;
			case SYMBOL__PROP: internalSetSymbol((com.top_logic.graphic.flow.data.ConnectorSymbol) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		if (hasAnchor()) {
			out.name(ANCHOR__PROP);
			getAnchor().writeTo(scope, out);
		}
		out.name(CONNECT_POSITION__PROP);
		out.value(getConnectPosition());
		out.name(SYMBOL__PROP);
		getSymbol().writeTo(out);
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case CONNECTION__PROP: {
				if (hasConnection()) {
					getConnection().writeTo(scope, out);
				} else {
					out.nullValue();
				}
				break;
			}
			case ANCHOR__PROP: {
				if (hasAnchor()) {
					getAnchor().writeTo(scope, out);
				} else {
					out.nullValue();
				}
				break;
			}
			case CONNECT_POSITION__PROP: {
				out.value(getConnectPosition());
				break;
			}
			case SYMBOL__PROP: {
				getSymbol().writeTo(out);
				break;
			}
			default: super.writeFieldValue(scope, out, field);
		}
	}

	@Override
	public void readField(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ANCHOR__PROP: setAnchor(com.top_logic.graphic.flow.data.Box.readBox(scope, in)); break;
			case CONNECT_POSITION__PROP: setConnectPosition(in.nextDouble()); break;
			case SYMBOL__PROP: setSymbol(com.top_logic.graphic.flow.data.ConnectorSymbol.readConnectorSymbol(in)); break;
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Widget.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
