package com.top_logic.tl.ai.core.session.persist.messages;

/**
 * One source a run read, as its reading list recorded it.
 */
public class ReadSourceDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.session.persist.messages.ReadSourceDto} instance.
	 */
	public static com.top_logic.tl.ai.core.session.persist.messages.ReadSourceDto create() {
		return new com.top_logic.tl.ai.core.session.persist.messages.ReadSourceDto();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.session.persist.messages.ReadSourceDto} type in JSON format. */
	public static final String READ_SOURCE_DTO__TYPE = "ReadSourceDto";

	/** @see #getKind() */
	public static final String KIND__PROP = "kind";

	/** @see #getId() */
	public static final String ID__PROP = "id";

	/** @see #getTitle() */
	public static final String TITLE__PROP = "title";

	/** @see #getUrl() */
	public static final String URL__PROP = "url";

	/** @see #getArchiveUrl() */
	public static final String ARCHIVE_URL__PROP = "archiveUrl";

	/** @see #getReadAt() */
	public static final String READ_AT__PROP = "readAt";

	private String _kind = "";

	private String _id = "";

	private String _title = "";

	private String _url = "";

	private String _archiveUrl = "";

	private String _readAt = "";

	/**
	 * Creates a {@link ReadSourceDto} instance.
	 *
	 * @see com.top_logic.tl.ai.core.session.persist.messages.ReadSourceDto#create()
	 */
	protected ReadSourceDto() {
		super();
	}

	/**
	 * Declared kind, e.g. {@code web}, {@code object} or an application's own.
	 */
	public final String getKind() {
		return _kind;
	}

	/**
	 * @see #getKind()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.ReadSourceDto setKind(String value) {
		internalSetKind(value);
		return this;
	}

	/** Internal setter for {@link #getKind()} without chain call utility. */
	protected final void internalSetKind(String value) {
		_kind = value;
	}

	/**
	 * The reference an answer writes to point at it: an object id, a URL, a record code.
	 */
	public final String getId() {
		return _id;
	}

	/**
	 * @see #getId()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.ReadSourceDto setId(String value) {
		internalSetId(value);
		return this;
	}

	/** Internal setter for {@link #getId()} without chain call utility. */
	protected final void internalSetId(String value) {
		_id = value;
	}

	/**
	 * What to call it in front of a reader.
	 */
	public final String getTitle() {
		return _title;
	}

	/**
	 * @see #getTitle()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.ReadSourceDto setTitle(String value) {
		internalSetTitle(value);
		return this;
	}

	/** Internal setter for {@link #getTitle()} without chain call utility. */
	protected final void internalSetTitle(String value) {
		_title = value;
	}

	/**
	 * Address of a web source; empty otherwise.
	 */
	public final String getUrl() {
		return _url;
	}

	/**
	 * @see #getUrl()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.ReadSourceDto setUrl(String value) {
		internalSetUrl(value);
		return this;
	}

	/** Internal setter for {@link #getUrl()} without chain call utility. */
	protected final void internalSetUrl(String value) {
		_url = value;
	}

	/**
	 * Where the copy archived when it was read can be looked at; empty without one.
	 */
	public final String getArchiveUrl() {
		return _archiveUrl;
	}

	/**
	 * @see #getArchiveUrl()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.ReadSourceDto setArchiveUrl(String value) {
		internalSetArchiveUrl(value);
		return this;
	}

	/** Internal setter for {@link #getArchiveUrl()} without chain call utility. */
	protected final void internalSetArchiveUrl(String value) {
		_archiveUrl = value;
	}

	/**
	 * When it was read, ISO-8601.
	 */
	public final String getReadAt() {
		return _readAt;
	}

	/**
	 * @see #getReadAt()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.ReadSourceDto setReadAt(String value) {
		internalSetReadAt(value);
		return this;
	}

	/** Internal setter for {@link #getReadAt()} without chain call utility. */
	protected final void internalSetReadAt(String value) {
		_readAt = value;
	}

	@Override
	public String jsonType() {
		return READ_SOURCE_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			KIND__PROP, 
			ID__PROP, 
			TITLE__PROP, 
			URL__PROP, 
			ARCHIVE_URL__PROP, 
			READ_AT__PROP);
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
			case KIND__PROP: return getKind();
			case ID__PROP: return getId();
			case TITLE__PROP: return getTitle();
			case URL__PROP: return getUrl();
			case ARCHIVE_URL__PROP: return getArchiveUrl();
			case READ_AT__PROP: return getReadAt();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case KIND__PROP: internalSetKind((String) value); break;
			case ID__PROP: internalSetId((String) value); break;
			case TITLE__PROP: internalSetTitle((String) value); break;
			case URL__PROP: internalSetUrl((String) value); break;
			case ARCHIVE_URL__PROP: internalSetArchiveUrl((String) value); break;
			case READ_AT__PROP: internalSetReadAt((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.session.persist.messages.ReadSourceDto readReadSourceDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.session.persist.messages.ReadSourceDto result = new com.top_logic.tl.ai.core.session.persist.messages.ReadSourceDto();
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
		out.name(KIND__PROP);
		out.value(getKind());
		out.name(ID__PROP);
		out.value(getId());
		out.name(TITLE__PROP);
		out.value(getTitle());
		out.name(URL__PROP);
		out.value(getUrl());
		out.name(ARCHIVE_URL__PROP);
		out.value(getArchiveUrl());
		out.name(READ_AT__PROP);
		out.value(getReadAt());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case KIND__PROP: setKind(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ID__PROP: setId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TITLE__PROP: setTitle(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case URL__PROP: setUrl(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ARCHIVE_URL__PROP: setArchiveUrl(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case READ_AT__PROP: setReadAt(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
