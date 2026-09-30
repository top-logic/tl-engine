package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.ChildControl}.
 */
public class ChildControl_Impl extends de.haumacher.msgbuf.data.AbstractDataObject implements com.top_logic.layout.react.state.ChildControl {

	private String _controlId = "";

	private String _module = "";

	private Object _state = null;

	private String _viewSource = "";

	/**
	 * Creates a {@link ChildControl_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.ChildControl#create()
	 */
	public ChildControl_Impl() {
		super();
	}

	@Override
	public final String getControlId() {
		return _controlId;
	}

	@Override
	public com.top_logic.layout.react.state.ChildControl setControlId(String value) {
		internalSetControlId(value);
		return this;
	}

	/** Internal setter for {@link #getControlId()} without chain call utility. */
	protected final void internalSetControlId(String value) {
		_controlId = value;
	}

	@Override
	public final String getModule() {
		return _module;
	}

	@Override
	public com.top_logic.layout.react.state.ChildControl setModule(String value) {
		internalSetModule(value);
		return this;
	}

	/** Internal setter for {@link #getModule()} without chain call utility. */
	protected final void internalSetModule(String value) {
		_module = value;
	}

	@Override
	public final Object getState() {
		return _state;
	}

	@Override
	public com.top_logic.layout.react.state.ChildControl setState(Object value) {
		internalSetState(value);
		return this;
	}

	/** Internal setter for {@link #getState()} without chain call utility. */
	protected final void internalSetState(Object value) {
		_state = value;
	}

	@Override
	public final boolean hasState() {
		return _state != null;
	}

	@Override
	public final String getViewSource() {
		return _viewSource;
	}

	@Override
	public com.top_logic.layout.react.state.ChildControl setViewSource(String value) {
		internalSetViewSource(value);
		return this;
	}

	/** Internal setter for {@link #getViewSource()} without chain call utility. */
	protected final void internalSetViewSource(String value) {
		_viewSource = value;
	}

	@Override
	public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		writeContent(out);
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(CONTROL_ID__PROP);
		out.value(getControlId());
		out.name(MODULE__PROP);
		out.value(getModule());
		if (hasState()) {
			out.name(STATE__PROP);
			de.haumacher.msgbuf.json.JsonUtil.writeJsonValue(out, getState());
		}
		out.name(VIEW_SOURCE__PROP);
		out.value(getViewSource());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case CONTROL_ID__PROP: setControlId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case MODULE__PROP: setModule(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case STATE__PROP: setState(de.haumacher.msgbuf.json.JsonUtil.nextJsonValue(in)); break;
			case VIEW_SOURCE__PROP: setViewSource(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
