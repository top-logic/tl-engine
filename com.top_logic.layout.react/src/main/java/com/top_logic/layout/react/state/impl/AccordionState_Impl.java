package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.AccordionState}.
 */
public class AccordionState_Impl extends com.top_logic.layout.react.state.impl.ControlState_Impl implements com.top_logic.layout.react.state.AccordionState {
	/**
	 * Implementation of {@link com.top_logic.layout.react.state.AccordionState.Section}.
	 */
	public static class Section_Impl extends de.haumacher.msgbuf.data.AbstractDataObject implements com.top_logic.layout.react.state.AccordionState.Section {

		private String _id = "";

		private String _label = "";

		private String _icon = "";

		private boolean _expanded = false;

		private com.top_logic.layout.react.state.ChildControl _actions = null;

		private com.top_logic.layout.react.state.ChildControl _content = null;

		/**
		 * Creates a {@link Section_Impl} instance.
		 *
		 * @see com.top_logic.layout.react.state.AccordionState.Section#create()
		 */
		public Section_Impl() {
			super();
		}

		@Override
		public final String getId() {
			return _id;
		}

		@Override
		public com.top_logic.layout.react.state.AccordionState.Section setId(String value) {
			internalSetId(value);
			return this;
		}

		/** Internal setter for {@link #getId()} without chain call utility. */
		protected final void internalSetId(String value) {
			_id = value;
		}

		@Override
		public final String getLabel() {
			return _label;
		}

		@Override
		public com.top_logic.layout.react.state.AccordionState.Section setLabel(String value) {
			internalSetLabel(value);
			return this;
		}

		/** Internal setter for {@link #getLabel()} without chain call utility. */
		protected final void internalSetLabel(String value) {
			_label = value;
		}

		@Override
		public final String getIcon() {
			return _icon;
		}

		@Override
		public com.top_logic.layout.react.state.AccordionState.Section setIcon(String value) {
			internalSetIcon(value);
			return this;
		}

		/** Internal setter for {@link #getIcon()} without chain call utility. */
		protected final void internalSetIcon(String value) {
			_icon = value;
		}

		@Override
		public final boolean isExpanded() {
			return _expanded;
		}

		@Override
		public com.top_logic.layout.react.state.AccordionState.Section setExpanded(boolean value) {
			internalSetExpanded(value);
			return this;
		}

		/** Internal setter for {@link #isExpanded()} without chain call utility. */
		protected final void internalSetExpanded(boolean value) {
			_expanded = value;
		}

		@Override
		public final com.top_logic.layout.react.state.ChildControl getActions() {
			return _actions;
		}

		@Override
		public com.top_logic.layout.react.state.AccordionState.Section setActions(com.top_logic.layout.react.state.ChildControl value) {
			internalSetActions(value);
			return this;
		}

		/** Internal setter for {@link #getActions()} without chain call utility. */
		protected final void internalSetActions(com.top_logic.layout.react.state.ChildControl value) {
			_actions = value;
		}

		@Override
		public final boolean hasActions() {
			return _actions != null;
		}

		@Override
		public final com.top_logic.layout.react.state.ChildControl getContent() {
			return _content;
		}

		@Override
		public com.top_logic.layout.react.state.AccordionState.Section setContent(com.top_logic.layout.react.state.ChildControl value) {
			internalSetContent(value);
			return this;
		}

		/** Internal setter for {@link #getContent()} without chain call utility. */
		protected final void internalSetContent(com.top_logic.layout.react.state.ChildControl value) {
			_content = value;
		}

		@Override
		public final boolean hasContent() {
			return _content != null;
		}

		@Override
		public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			writeContent(out);
		}

		@Override
		protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			super.writeFields(out);
			out.name(ID__PROP);
			out.value(getId());
			out.name(LABEL__PROP);
			out.value(getLabel());
			out.name(ICON__PROP);
			out.value(getIcon());
			out.name(EXPANDED__PROP);
			out.value(isExpanded());
			if (hasActions()) {
				out.name(ACTIONS__PROP);
				getActions().writeTo(out);
			}
			if (hasContent()) {
				out.name(CONTENT__PROP);
				getContent().writeTo(out);
			}
		}

		@Override
		protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
			switch (field) {
				case ID__PROP: setId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
				case LABEL__PROP: setLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
				case ICON__PROP: setIcon(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
				case EXPANDED__PROP: setExpanded(in.nextBoolean()); break;
				case ACTIONS__PROP: setActions(com.top_logic.layout.react.state.ChildControl.readChildControl(in)); break;
				case CONTENT__PROP: setContent(com.top_logic.layout.react.state.ChildControl.readChildControl(in)); break;
				default: super.readField(in, field);
			}
		}

	}

	private final java.util.List<com.top_logic.layout.react.state.AccordionState.Section> _sections = new java.util.ArrayList<>();

	private boolean _exclusive = false;

	/**
	 * Creates a {@link AccordionState_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.AccordionState#create()
	 */
	public AccordionState_Impl() {
		super();
	}

	@Override
	public final java.util.List<com.top_logic.layout.react.state.AccordionState.Section> getSections() {
		return _sections;
	}

	@Override
	public com.top_logic.layout.react.state.AccordionState setSections(java.util.List<? extends com.top_logic.layout.react.state.AccordionState.Section> value) {
		internalSetSections(value);
		return this;
	}

	/** Internal setter for {@link #getSections()} without chain call utility. */
	protected final void internalSetSections(java.util.List<? extends com.top_logic.layout.react.state.AccordionState.Section> value) {
		if (value == null) throw new IllegalArgumentException("Property 'sections' cannot be null.");
		_sections.clear();
		_sections.addAll(value);
	}

	@Override
	public com.top_logic.layout.react.state.AccordionState addSection(com.top_logic.layout.react.state.AccordionState.Section value) {
		internalAddSection(value);
		return this;
	}

	/** Implementation of {@link #addSection(com.top_logic.layout.react.state.AccordionState.Section)} without chain call utility. */
	protected final void internalAddSection(com.top_logic.layout.react.state.AccordionState.Section value) {
		_sections.add(value);
	}

	@Override
	public final void removeSection(com.top_logic.layout.react.state.AccordionState.Section value) {
		_sections.remove(value);
	}

	@Override
	public final boolean isExclusive() {
		return _exclusive;
	}

	@Override
	public com.top_logic.layout.react.state.AccordionState setExclusive(boolean value) {
		internalSetExclusive(value);
		return this;
	}

	/** Internal setter for {@link #isExclusive()} without chain call utility. */
	protected final void internalSetExclusive(boolean value) {
		_exclusive = value;
	}

	@Override
	public com.top_logic.layout.react.state.AccordionState setHidden(boolean value) {
		internalSetHidden(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.AccordionState setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public String jsonType() {
		return ACCORDION_STATE__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(SECTIONS__PROP);
		out.beginArray();
		for (com.top_logic.layout.react.state.AccordionState.Section x : getSections()) {
			x.writeTo(out);
		}
		out.endArray();
		out.name(EXCLUSIVE__PROP);
		out.value(isExclusive());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case SECTIONS__PROP: {
				java.util.List<com.top_logic.layout.react.state.AccordionState.Section> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.layout.react.state.AccordionState.Section.readSection(in));
				}
				in.endArray();
				setSections(newValue);
			}
			break;
			case EXCLUSIVE__PROP: setExclusive(in.nextBoolean()); break;
			default: super.readField(in, field);
		}
	}

}
