package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.AvatarState}.
 */
public class AvatarState_Impl extends com.top_logic.layout.react.state.impl.ImageSourceState_Impl implements com.top_logic.layout.react.state.AvatarState {

	private String _name = "";

	private com.top_logic.layout.react.state.AvatarState.Size _size = com.top_logic.layout.react.state.AvatarState.Size.SMALL;

	/**
	 * Creates a {@link AvatarState_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.AvatarState#create()
	 */
	public AvatarState_Impl() {
		super();
	}

	@Override
	public final String getName() {
		return _name;
	}

	@Override
	public com.top_logic.layout.react.state.AvatarState setName(String value) {
		internalSetName(value);
		return this;
	}

	/** Internal setter for {@link #getName()} without chain call utility. */
	protected final void internalSetName(String value) {
		_name = value;
	}

	@Override
	public final com.top_logic.layout.react.state.AvatarState.Size getSize() {
		return _size;
	}

	@Override
	public com.top_logic.layout.react.state.AvatarState setSize(com.top_logic.layout.react.state.AvatarState.Size value) {
		internalSetSize(value);
		return this;
	}

	/** Internal setter for {@link #getSize()} without chain call utility. */
	protected final void internalSetSize(com.top_logic.layout.react.state.AvatarState.Size value) {
		if (value == null) throw new IllegalArgumentException("Property 'size' cannot be null.");
		_size = value;
	}

	@Override
	public com.top_logic.layout.react.state.AvatarState setUrl(String value) {
		internalSetUrl(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.AvatarState setHasData(boolean value) {
		internalSetHasData(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.AvatarState setDataRevision(int value) {
		internalSetDataRevision(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.AvatarState setHidden(boolean value) {
		internalSetHidden(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.AvatarState setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public String jsonType() {
		return AVATAR_STATE__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(NAME__PROP);
		out.value(getName());
		out.name(SIZE__PROP);
		getSize().writeTo(out);
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case NAME__PROP: setName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case SIZE__PROP: setSize(com.top_logic.layout.react.state.AvatarState.Size.readSize(in)); break;
			default: super.readField(in, field);
		}
	}

}
