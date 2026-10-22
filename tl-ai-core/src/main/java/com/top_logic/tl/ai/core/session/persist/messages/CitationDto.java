package com.top_logic.tl.ai.core.session.persist.messages;

/**
 * One source a citation marker in an answer points to.
 */
public class CitationDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.session.persist.messages.CitationDto} instance.
	 */
	public static com.top_logic.tl.ai.core.session.persist.messages.CitationDto create() {
		return new com.top_logic.tl.ai.core.session.persist.messages.CitationDto();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.session.persist.messages.CitationDto} type in JSON format. */
	public static final String CITATION_DTO__TYPE = "CitationDto";

	/** @see #getIndex() */
	public static final String INDEX__PROP = "index";

	/** @see #getToolName() */
	public static final String TOOL_NAME__PROP = "toolName";

	/** @see #getToolCallId() */
	public static final String TOOL_CALL_ID__PROP = "toolCallId";

	/** @see #getKind() */
	public static final String KIND__PROP = "kind";

	/** @see #getKindLabel() */
	public static final String KIND_LABEL__PROP = "kindLabel";

	/** @see #getId() */
	public static final String ID__PROP = "id";

	/** @see #getLabel() */
	public static final String LABEL__PROP = "label";

	/** @see #getUrl() */
	public static final String URL__PROP = "url";

	/** @see #getPassage() */
	public static final String PASSAGE__PROP = "passage";

	/** @see #getExcerpt() */
	public static final String EXCERPT__PROP = "excerpt";

	/** @see #getArchiveUrl() */
	public static final String ARCHIVE_URL__PROP = "archiveUrl";

	/** @see #getArchivedAt() */
	public static final String ARCHIVED_AT__PROP = "archivedAt";

	private int _index = 0;

	private String _toolName = "";

	private String _toolCallId = "";

	private String _kind = "";

	private String _kindLabel = "";

	private String _id = "";

	private String _label = "";

	private String _url = "";

	private String _passage = "";

	private String _excerpt = "";

	private String _archiveUrl = "";

	private String _archivedAt = "";

	/**
	 * Creates a {@link CitationDto} instance.
	 *
	 * @see com.top_logic.tl.ai.core.session.persist.messages.CitationDto#create()
	 */
	protected CitationDto() {
		super();
	}

	/**
	 * 1-based marker index shown in the answer.
	 */
	public final int getIndex() {
		return _index;
	}

	/**
	 * @see #getIndex()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.CitationDto setIndex(int value) {
		internalSetIndex(value);
		return this;
	}

	/** Internal setter for {@link #getIndex()} without chain call utility. */
	protected final void internalSetIndex(int value) {
		_index = value;
	}

	/**
	 * Tool that produced the cited id or URL.
	 */
	public final String getToolName() {
		return _toolName;
	}

	/**
	 * @see #getToolName()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.CitationDto setToolName(String value) {
		internalSetToolName(value);
		return this;
	}

	/** Internal setter for {@link #getToolName()} without chain call utility. */
	protected final void internalSetToolName(String value) {
		_toolName = value;
	}

	/**
	 * Tool call id of that tool, when known.
	 */
	public final String getToolCallId() {
		return _toolCallId;
	}

	/**
	 * @see #getToolCallId()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.CitationDto setToolCallId(String value) {
		internalSetToolCallId(value);
		return this;
	}

	/** Internal setter for {@link #getToolCallId()} without chain call utility. */
	protected final void internalSetToolCallId(String value) {
		_toolCallId = value;
	}

	/**
	 * Host kind, e.g. {@code web} or {@code invoice}, as the application declared it.
	 */
	public final String getKind() {
		return _kind;
	}

	/**
	 * @see #getKind()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.CitationDto setKind(String value) {
		internalSetKind(value);
		return this;
	}

	/** Internal setter for {@link #getKind()} without chain call utility. */
	protected final void internalSetKind(String value) {
		_kind = value;
	}

	/**
	 * What to call that kind in front of a reader, in the reader's language.
	 *
	 * Empty when the host declared no label for the kind; the {@code kind} is then shown instead.
	 */
	public final String getKindLabel() {
		return _kindLabel;
	}

	/**
	 * @see #getKindLabel()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.CitationDto setKindLabel(String value) {
		internalSetKindLabel(value);
		return this;
	}

	/** Internal setter for {@link #getKindLabel()} without chain call utility. */
	protected final void internalSetKindLabel(String value) {
		_kindLabel = value;
	}

	/**
	 * Id the tool returned (a URL, a TID, …).
	 */
	public final String getId() {
		return _id;
	}

	/**
	 * @see #getId()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.CitationDto setId(String value) {
		internalSetId(value);
		return this;
	}

	/** Internal setter for {@link #getId()} without chain call utility. */
	protected final void internalSetId(String value) {
		_id = value;
	}

	/**
	 * Human label for the chip popover.
	 */
	public final String getLabel() {
		return _label;
	}

	/**
	 * @see #getLabel()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.CitationDto setLabel(String value) {
		internalSetLabel(value);
		return this;
	}

	/** Internal setter for {@link #getLabel()} without chain call utility. */
	protected final void internalSetLabel(String value) {
		_label = value;
	}

	/**
	 * Link for a web source; empty otherwise.
	 */
	public final String getUrl() {
		return _url;
	}

	/**
	 * @see #getUrl()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.CitationDto setUrl(String value) {
		internalSetUrl(value);
		return this;
	}

	/** Internal setter for {@link #getUrl()} without chain call utility. */
	protected final void internalSetUrl(String value) {
		_url = value;
	}

	/**
	 * The sentence of the source the fact rests on, as the model read it; empty when none.
	 */
	public final String getPassage() {
		return _passage;
	}

	/**
	 * @see #getPassage()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.CitationDto setPassage(String value) {
		internalSetPassage(value);
		return this;
	}

	/** Internal setter for {@link #getPassage()} without chain call utility. */
	protected final void internalSetPassage(String value) {
		_passage = value;
	}

	/**
	 * A window of the source around {@link #getPassage()}, for the marker's popover.
	 */
	public final String getExcerpt() {
		return _excerpt;
	}

	/**
	 * @see #getExcerpt()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.CitationDto setExcerpt(String value) {
		internalSetExcerpt(value);
		return this;
	}

	/** Internal setter for {@link #getExcerpt()} without chain call utility. */
	protected final void internalSetExcerpt(String value) {
		_excerpt = value;
	}

	/**
	 * Where the copy of a web page archived when it was read can be looked at; empty without one.
	 */
	public final String getArchiveUrl() {
		return _archiveUrl;
	}

	/**
	 * @see #getArchiveUrl()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.CitationDto setArchiveUrl(String value) {
		internalSetArchiveUrl(value);
		return this;
	}

	/** Internal setter for {@link #getArchiveUrl()} without chain call utility. */
	protected final void internalSetArchiveUrl(String value) {
		_archiveUrl = value;
	}

	/**
	 * When that copy was taken, ISO-8601; empty without one.
	 */
	public final String getArchivedAt() {
		return _archivedAt;
	}

	/**
	 * @see #getArchivedAt()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.CitationDto setArchivedAt(String value) {
		internalSetArchivedAt(value);
		return this;
	}

	/** Internal setter for {@link #getArchivedAt()} without chain call utility. */
	protected final void internalSetArchivedAt(String value) {
		_archivedAt = value;
	}

	@Override
	public String jsonType() {
		return CITATION_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			INDEX__PROP, 
			TOOL_NAME__PROP, 
			TOOL_CALL_ID__PROP, 
			KIND__PROP, 
			KIND_LABEL__PROP, 
			ID__PROP, 
			LABEL__PROP, 
			URL__PROP, 
			PASSAGE__PROP, 
			EXCERPT__PROP, 
			ARCHIVE_URL__PROP, 
			ARCHIVED_AT__PROP);
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
			case INDEX__PROP: return getIndex();
			case TOOL_NAME__PROP: return getToolName();
			case TOOL_CALL_ID__PROP: return getToolCallId();
			case KIND__PROP: return getKind();
			case KIND_LABEL__PROP: return getKindLabel();
			case ID__PROP: return getId();
			case LABEL__PROP: return getLabel();
			case URL__PROP: return getUrl();
			case PASSAGE__PROP: return getPassage();
			case EXCERPT__PROP: return getExcerpt();
			case ARCHIVE_URL__PROP: return getArchiveUrl();
			case ARCHIVED_AT__PROP: return getArchivedAt();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case INDEX__PROP: internalSetIndex((int) value); break;
			case TOOL_NAME__PROP: internalSetToolName((String) value); break;
			case TOOL_CALL_ID__PROP: internalSetToolCallId((String) value); break;
			case KIND__PROP: internalSetKind((String) value); break;
			case KIND_LABEL__PROP: internalSetKindLabel((String) value); break;
			case ID__PROP: internalSetId((String) value); break;
			case LABEL__PROP: internalSetLabel((String) value); break;
			case URL__PROP: internalSetUrl((String) value); break;
			case PASSAGE__PROP: internalSetPassage((String) value); break;
			case EXCERPT__PROP: internalSetExcerpt((String) value); break;
			case ARCHIVE_URL__PROP: internalSetArchiveUrl((String) value); break;
			case ARCHIVED_AT__PROP: internalSetArchivedAt((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.session.persist.messages.CitationDto readCitationDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.session.persist.messages.CitationDto result = new com.top_logic.tl.ai.core.session.persist.messages.CitationDto();
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
		out.name(INDEX__PROP);
		out.value(getIndex());
		out.name(TOOL_NAME__PROP);
		out.value(getToolName());
		out.name(TOOL_CALL_ID__PROP);
		out.value(getToolCallId());
		out.name(KIND__PROP);
		out.value(getKind());
		out.name(KIND_LABEL__PROP);
		out.value(getKindLabel());
		out.name(ID__PROP);
		out.value(getId());
		out.name(LABEL__PROP);
		out.value(getLabel());
		out.name(URL__PROP);
		out.value(getUrl());
		out.name(PASSAGE__PROP);
		out.value(getPassage());
		out.name(EXCERPT__PROP);
		out.value(getExcerpt());
		out.name(ARCHIVE_URL__PROP);
		out.value(getArchiveUrl());
		out.name(ARCHIVED_AT__PROP);
		out.value(getArchivedAt());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case INDEX__PROP: setIndex(in.nextInt()); break;
			case TOOL_NAME__PROP: setToolName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TOOL_CALL_ID__PROP: setToolCallId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case KIND__PROP: setKind(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case KIND_LABEL__PROP: setKindLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ID__PROP: setId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case LABEL__PROP: setLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case URL__PROP: setUrl(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case PASSAGE__PROP: setPassage(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case EXCERPT__PROP: setExcerpt(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ARCHIVE_URL__PROP: setArchiveUrl(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ARCHIVED_AT__PROP: setArchivedAt(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
