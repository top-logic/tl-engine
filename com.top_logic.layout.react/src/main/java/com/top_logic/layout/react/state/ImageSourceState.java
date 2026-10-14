package com.top_logic.layout.react.state;

/**
 * State properties of a control displaying a picture.
 *
 * The client only ever loads an address: either the {@link #getUrl()} naming the picture, or - while
 * {@link #isHasData()} is set - the address of the data endpoint of the control itself, which serves the
 * picture. The client appends the {@link #getDataRevision()} to the latter, so that a picture which was
 * replaced is loaded again instead of being taken from the browser cache.
 */
public interface ImageSourceState extends com.top_logic.layout.react.state.ControlState {

	/**
	 * Creates a {@link com.top_logic.layout.react.state.ImageSourceState} instance.
	 */
	static com.top_logic.layout.react.state.ImageSourceState create() {
		return new com.top_logic.layout.react.state.impl.ImageSourceState_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.ImageSourceState} type in JSON format. */
	String IMAGE_SOURCE_STATE__TYPE = "ImageSourceState";

	/** @see #getUrl() */
	String URL__PROP = "url";

	/** @see #isHasData() */
	String HAS_DATA__PROP = "hasData";

	/** @see #getDataRevision() */
	String DATA_REVISION__PROP = "dataRevision";

	/**
	 * The address the picture is loaded from. Absent or {@code null}: no picture, unless
	 * {@link #isHasData()} is set.
	 */
	String getUrl();

	/**
	 * @see #getUrl()
	 */
	com.top_logic.layout.react.state.ImageSourceState setUrl(String value);

	/**
	 * Whether the control serves the picture itself from its data endpoint. The {@link #getUrl()} is
	 * then absent.
	 */
	boolean isHasData();

	/**
	 * @see #isHasData()
	 */
	com.top_logic.layout.react.state.ImageSourceState setHasData(boolean value);

	/**
	 * How often the picture the control serves was replaced, the revision the client appends to the
	 * address of the data endpoint. Absent: 0.
	 */
	int getDataRevision();

	/**
	 * @see #getDataRevision()
	 */
	com.top_logic.layout.react.state.ImageSourceState setDataRevision(int value);

	@Override
	com.top_logic.layout.react.state.ImageSourceState setHidden(boolean value);

	@Override
	com.top_logic.layout.react.state.ImageSourceState setCssClass(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.ImageSourceState readImageSourceState(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.ImageSourceState_Impl result = new com.top_logic.layout.react.state.impl.ImageSourceState_Impl();
		result.readContent(in);
		return result;
	}

}
