package com.top_logic.layout.react.state;

/**
 * State of a window: a frame with a title bar, a body and a footer, the component
 * {@code TLWindow}.
 *
 * Closing the window sends the command {@code close}, resizing it the command {@code resize}.
 */
public interface WindowState extends com.top_logic.layout.react.state.ControlState {

	/**
	 * Creates a {@link com.top_logic.layout.react.state.WindowState} instance.
	 */
	static com.top_logic.layout.react.state.WindowState create() {
		return new com.top_logic.layout.react.state.impl.WindowState_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.WindowState} type in JSON format. */
	String WINDOW_STATE__TYPE = "WindowState";

	/** @see #getTitle() */
	String TITLE__PROP = "title";

	/** @see #getWidth() */
	String WIDTH__PROP = "width";

	/** @see #getHeight() */
	String HEIGHT__PROP = "height";

	/** @see #getMinHeight() */
	String MIN_HEIGHT__PROP = "minHeight";

	/** @see #isResizable() */
	String RESIZABLE__PROP = "resizable";

	/** @see #isClosable() */
	String CLOSABLE__PROP = "closable";

	/** @see #getChild() */
	String CHILD__PROP = "child";

	/** @see #getToolbar() */
	String TOOLBAR__PROP = "toolbar";

	/** @see #getFooter() */
	String FOOTER__PROP = "footer";

	/** @see #getToolbarButtons() */
	String TOOLBAR_BUTTONS__PROP = "toolbarButtons";

	/**
	 * The title of the window.
	 */
	String getTitle();

	/**
	 * @see #getTitle()
	 */
	com.top_logic.layout.react.state.WindowState setTitle(String value);

	/**
	 * The width of the window, a CSS length (e.g. {@code 32rem}, {@code 640px}).
	 */
	String getWidth();

	/**
	 * @see #getWidth()
	 */
	com.top_logic.layout.react.state.WindowState setWidth(String value);

	/**
	 * The height of the window, a CSS length. Absent: the height of the content.
	 */
	String getHeight();

	/**
	 * @see #getHeight()
	 */
	com.top_logic.layout.react.state.WindowState setHeight(String value);

	/**
	 * The least height of the window, a CSS length. Absent: none.
	 */
	String getMinHeight();

	/**
	 * @see #getMinHeight()
	 */
	com.top_logic.layout.react.state.WindowState setMinHeight(String value);

	/**
	 * Whether the user can resize the window by dragging its edges.
	 */
	boolean isResizable();

	/**
	 * @see #isResizable()
	 */
	com.top_logic.layout.react.state.WindowState setResizable(boolean value);

	/**
	 * Whether the user can close the window. Absent: closable.
	 */
	boolean isClosable();

	/**
	 * @see #isClosable()
	 */
	com.top_logic.layout.react.state.WindowState setClosable(boolean value);

	/**
	 * The content of the window body.
	 */
	com.top_logic.layout.react.state.ChildControl getChild();

	/**
	 * @see #getChild()
	 */
	com.top_logic.layout.react.state.WindowState setChild(com.top_logic.layout.react.state.ChildControl value);

	/**
	 * Checks, whether {@link #getChild()} has a value.
	 */
	boolean hasChild();

	/**
	 * The toolbar of the title bar. Absent: none.
	 */
	com.top_logic.layout.react.state.ChildControl getToolbar();

	/**
	 * @see #getToolbar()
	 */
	com.top_logic.layout.react.state.WindowState setToolbar(com.top_logic.layout.react.state.ChildControl value);

	/**
	 * Checks, whether {@link #getToolbar()} has a value.
	 */
	boolean hasToolbar();

	/**
	 * The toolbar of the footer. Absent: none.
	 */
	com.top_logic.layout.react.state.ChildControl getFooter();

	/**
	 * @see #getFooter()
	 */
	com.top_logic.layout.react.state.WindowState setFooter(com.top_logic.layout.react.state.ChildControl value);

	/**
	 * Checks, whether {@link #getFooter()} has a value.
	 */
	boolean hasFooter();

	/**
	 * Buttons added to the window one by one.
	 */
	java.util.List<com.top_logic.layout.react.state.ChildControl> getToolbarButtons();

	/**
	 * @see #getToolbarButtons()
	 */
	com.top_logic.layout.react.state.WindowState setToolbarButtons(java.util.List<? extends com.top_logic.layout.react.state.ChildControl> value);

	/**
	 * Adds a value to the {@link #getToolbarButtons()} list.
	 */
	com.top_logic.layout.react.state.WindowState addToolbarButton(com.top_logic.layout.react.state.ChildControl value);

	/**
	 * Removes a value from the {@link #getToolbarButtons()} list.
	 */
	void removeToolbarButton(com.top_logic.layout.react.state.ChildControl value);

	@Override
	com.top_logic.layout.react.state.WindowState setHidden(boolean value);

	@Override
	com.top_logic.layout.react.state.WindowState setCssClass(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.WindowState readWindowState(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.WindowState_Impl result = new com.top_logic.layout.react.state.impl.WindowState_Impl();
		result.readContent(in);
		return result;
	}

}
