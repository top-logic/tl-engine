package com.top_logic.layout.react.protocol.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.protocol.DownloadEvent}.
 */
public class DownloadEvent_Impl extends com.top_logic.layout.react.protocol.impl.SSEEvent_Impl implements com.top_logic.layout.react.protocol.DownloadEvent {

	private String _url = "";

	private String _fileName = "";

	/**
	 * Creates a {@link DownloadEvent_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.protocol.DownloadEvent#create()
	 */
	public DownloadEvent_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.DOWNLOAD_EVENT;
	}

	@Override
	public final String getUrl() {
		return _url;
	}

	@Override
	public com.top_logic.layout.react.protocol.DownloadEvent setUrl(String value) {
		internalSetUrl(value);
		return this;
	}

	/** Internal setter for {@link #getUrl()} without chain call utility. */
	protected final void internalSetUrl(String value) {
		_url = value;
	}

	@Override
	public final String getFileName() {
		return _fileName;
	}

	@Override
	public com.top_logic.layout.react.protocol.DownloadEvent setFileName(String value) {
		internalSetFileName(value);
		return this;
	}

	/** Internal setter for {@link #getFileName()} without chain call utility. */
	protected final void internalSetFileName(String value) {
		_fileName = value;
	}

	@Override
	public String jsonType() {
		return DOWNLOAD_EVENT__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(URL__PROP);
		out.value(getUrl());
		out.name(FILE_NAME__PROP);
		out.value(getFileName());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case URL__PROP: setUrl(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case FILE_NAME__PROP: setFileName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.layout.react.protocol.SSEEvent.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
