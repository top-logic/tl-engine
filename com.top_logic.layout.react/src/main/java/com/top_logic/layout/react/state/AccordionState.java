package com.top_logic.layout.react.state;

/**
 * State of an accordion: a stack of sections, each with a header and a body that is expanded or
 * collapsed independently, the component {@code TLAccordion}.
 *
 * A click on the header of a section sends the command {@code toggleSection} to the server with the
 * arguments {@code sectionId} (the {@link Section#getId()} of the section) and {@code expanded} (whether
 * the section is to be expanded). The server answers with the resulting {@link #getSections()}: in an
 * {@link #isExclusive()} accordion, expanding a section collapses all others.
 */
public interface AccordionState extends com.top_logic.layout.react.state.ControlState {
	/**
	 * A section of the accordion.
	 */
	public interface Section extends de.haumacher.msgbuf.data.DataObject {

		/**
		 * Creates a {@link com.top_logic.layout.react.state.AccordionState.Section} instance.
		 */
		static com.top_logic.layout.react.state.AccordionState.Section create() {
			return new com.top_logic.layout.react.state.impl.AccordionState_Impl.Section_Impl();
		}

		/** Identifier for the {@link com.top_logic.layout.react.state.AccordionState.Section} type in JSON format. */
		String SECTION__TYPE = "Section";

		/** @see #getId() */
		String ID__PROP = "id";

		/** @see #getLabel() */
		String LABEL__PROP = "label";

		/** @see #getIcon() */
		String ICON__PROP = "icon";

		/** @see #isExpanded() */
		String EXPANDED__PROP = "expanded";

		/** @see #getActions() */
		String ACTIONS__PROP = "actions";

		/** @see #getContent() */
		String CONTENT__PROP = "content";

		/**
		 * The ID of the section, unique within the accordion.
		 */
		String getId();

		/**
		 * @see #getId()
		 */
		com.top_logic.layout.react.state.AccordionState.Section setId(String value);

		/**
		 * The label displayed in the header of the section.
		 */
		String getLabel();

		/**
		 * @see #getLabel()
		 */
		com.top_logic.layout.react.state.AccordionState.Section setLabel(String value);

		/**
		 * The icon displayed in the header of the section before the {@link #getLabel()}, the encoded form
		 * of a theme image. Absent for a section without an icon.
		 */
		String getIcon();

		/**
		 * @see #getIcon()
		 */
		com.top_logic.layout.react.state.AccordionState.Section setIcon(String value);

		/**
		 * Whether the body of the section is displayed.
		 */
		boolean isExpanded();

		/**
		 * @see #isExpanded()
		 */
		com.top_logic.layout.react.state.AccordionState.Section setExpanded(boolean value);

		/**
		 * The actions displayed at the end of the header of the section, a toolbar for instance.
		 * Absent for a section without actions.
		 */
		com.top_logic.layout.react.state.ChildControl getActions();

		/**
		 * @see #getActions()
		 */
		com.top_logic.layout.react.state.AccordionState.Section setActions(com.top_logic.layout.react.state.ChildControl value);

		/**
		 * Checks, whether {@link #getActions()} has a value.
		 */
		boolean hasActions();

		/**
		 * The body of the section. Absent until the section is expanded for the first time; from
		 * then on it stays present when the section is collapsed, so that the component can keep it
		 * mounted (with its local state) and only hide it.
		 */
		com.top_logic.layout.react.state.ChildControl getContent();

		/**
		 * @see #getContent()
		 */
		com.top_logic.layout.react.state.AccordionState.Section setContent(com.top_logic.layout.react.state.ChildControl value);

		/**
		 * Checks, whether {@link #getContent()} has a value.
		 */
		boolean hasContent();

		/** Reads a new instance from the given reader. */
		static com.top_logic.layout.react.state.AccordionState.Section readSection(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
			com.top_logic.layout.react.state.impl.AccordionState_Impl.Section_Impl result = new com.top_logic.layout.react.state.impl.AccordionState_Impl.Section_Impl();
			result.readContent(in);
			return result;
		}

	}

	/**
	 * Creates a {@link com.top_logic.layout.react.state.AccordionState} instance.
	 */
	static com.top_logic.layout.react.state.AccordionState create() {
		return new com.top_logic.layout.react.state.impl.AccordionState_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.AccordionState} type in JSON format. */
	String ACCORDION_STATE__TYPE = "AccordionState";

	/** @see #getSections() */
	String SECTIONS__PROP = "sections";

	/** @see #isExclusive() */
	String EXCLUSIVE__PROP = "exclusive";

	/**
	 * The sections, in the order from top to bottom.
	 */
	java.util.List<com.top_logic.layout.react.state.AccordionState.Section> getSections();

	/**
	 * @see #getSections()
	 */
	com.top_logic.layout.react.state.AccordionState setSections(java.util.List<? extends com.top_logic.layout.react.state.AccordionState.Section> value);

	/**
	 * Adds a value to the {@link #getSections()} list.
	 */
	com.top_logic.layout.react.state.AccordionState addSection(com.top_logic.layout.react.state.AccordionState.Section value);

	/**
	 * Removes a value from the {@link #getSections()} list.
	 */
	void removeSection(com.top_logic.layout.react.state.AccordionState.Section value);

	/**
	 * Whether at most one section is expanded at a time. Expanding a section of an exclusive
	 * accordion collapses the section expanded before. All sections may be collapsed.
	 */
	boolean isExclusive();

	/**
	 * @see #isExclusive()
	 */
	com.top_logic.layout.react.state.AccordionState setExclusive(boolean value);

	@Override
	com.top_logic.layout.react.state.AccordionState setHidden(boolean value);

	@Override
	com.top_logic.layout.react.state.AccordionState setCssClass(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.AccordionState readAccordionState(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.AccordionState_Impl result = new com.top_logic.layout.react.state.impl.AccordionState_Impl();
		result.readContent(in);
		return result;
	}

}
