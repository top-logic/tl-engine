package com.top_logic.layout.react.protocol.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.protocol.PropertyUpdate}.
 */
public class PropertyUpdate_Impl extends com.top_logic.layout.react.protocol.impl.SSEEvent_Impl implements com.top_logic.layout.react.protocol.PropertyUpdate {

	private String _elementId = "";

	private final java.util.List<com.top_logic.layout.react.protocol.Property> _properties = new java.util.ArrayList<>();

	/**
	 * Creates a {@link PropertyUpdate_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.protocol.PropertyUpdate#create()
	 */
	public PropertyUpdate_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.PROPERTY_UPDATE;
	}

	@Override
	public final String getElementId() {
		return _elementId;
	}

	@Override
	public com.top_logic.layout.react.protocol.PropertyUpdate setElementId(String value) {
		internalSetElementId(value);
		return this;
	}

	/** Internal setter for {@link #getElementId()} without chain call utility. */
	protected final void internalSetElementId(String value) {
		_elementId = value;
	}

	@Override
	public final java.util.List<com.top_logic.layout.react.protocol.Property> getProperties() {
		return _properties;
	}

	@Override
	public com.top_logic.layout.react.protocol.PropertyUpdate setProperties(java.util.List<? extends com.top_logic.layout.react.protocol.Property> value) {
		internalSetProperties(value);
		return this;
	}

	/** Internal setter for {@link #getProperties()} without chain call utility. */
	protected final void internalSetProperties(java.util.List<? extends com.top_logic.layout.react.protocol.Property> value) {
		if (value == null) throw new IllegalArgumentException("Property 'properties' cannot be null.");
		_properties.clear();
		_properties.addAll(value);
	}

	@Override
	public com.top_logic.layout.react.protocol.PropertyUpdate addProperty(com.top_logic.layout.react.protocol.Property value) {
		internalAddProperty(value);
		return this;
	}

	/** Implementation of {@link #addProperty(com.top_logic.layout.react.protocol.Property)} without chain call utility. */
	protected final void internalAddProperty(com.top_logic.layout.react.protocol.Property value) {
		_properties.add(value);
	}

	@Override
	public final void removeProperty(com.top_logic.layout.react.protocol.Property value) {
		_properties.remove(value);
	}

	@Override
	public String jsonType() {
		return PROPERTY_UPDATE__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(ELEMENT_ID__PROP);
		out.value(getElementId());
		out.name(PROPERTIES__PROP);
		out.beginArray();
		for (com.top_logic.layout.react.protocol.Property x : getProperties()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ELEMENT_ID__PROP: setElementId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case PROPERTIES__PROP: {
				java.util.List<com.top_logic.layout.react.protocol.Property> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.layout.react.protocol.Property.readProperty(in));
				}
				in.endArray();
				setProperties(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.layout.react.protocol.SSEEvent.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
