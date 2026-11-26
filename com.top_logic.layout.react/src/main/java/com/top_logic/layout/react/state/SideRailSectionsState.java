package com.top_logic.layout.react.state;

/**
 * State of the column of a side rail: the sections stacked, each with a header and its content, the
 * component {@code TLSideRailSections}.
 *
 * A click on the header of a section sends the command {@code toggleSection} with the argument
 * {@code name} (the name of the {@link SideRailState.Section}). The content of a collapsed section stays
 * mounted and hidden, so that it keeps what the user did there and keeps reporting its badge.
 */
public interface SideRailSectionsState extends com.top_logic.layout.react.state.ControlState {

	/**
	 * Creates a {@link com.top_logic.layout.react.state.SideRailSectionsState} instance.
	 */
	static com.top_logic.layout.react.state.SideRailSectionsState create() {
		return new com.top_logic.layout.react.state.impl.SideRailSectionsState_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.SideRailSectionsState} type in JSON format. */
	String SIDE_RAIL_SECTIONS_STATE__TYPE = "SideRailSectionsState";

	/** @see #getSections() */
	String SECTIONS__PROP = "sections";

	/**
	 * The sections, top to bottom, with their content.
	 */
	java.util.List<com.top_logic.layout.react.state.SideRailState.Section> getSections();

	/**
	 * @see #getSections()
	 */
	com.top_logic.layout.react.state.SideRailSectionsState setSections(java.util.List<? extends com.top_logic.layout.react.state.SideRailState.Section> value);

	/**
	 * Adds a value to the {@link #getSections()} list.
	 */
	com.top_logic.layout.react.state.SideRailSectionsState addSection(com.top_logic.layout.react.state.SideRailState.Section value);

	/**
	 * Removes a value from the {@link #getSections()} list.
	 */
	void removeSection(com.top_logic.layout.react.state.SideRailState.Section value);

	@Override
	com.top_logic.layout.react.state.SideRailSectionsState setHidden(boolean value);

	@Override
	com.top_logic.layout.react.state.SideRailSectionsState setCssClass(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.SideRailSectionsState readSideRailSectionsState(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.SideRailSectionsState_Impl result = new com.top_logic.layout.react.state.impl.SideRailSectionsState_Impl();
		result.readContent(in);
		return result;
	}

}
