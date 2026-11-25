package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.SideRailSectionsState}.
 */
public class SideRailSectionsState_Impl extends com.top_logic.layout.react.state.impl.ControlState_Impl implements com.top_logic.layout.react.state.SideRailSectionsState {

	private final java.util.List<com.top_logic.layout.react.state.SideRailState.Section> _sections = new java.util.ArrayList<>();

	/**
	 * Creates a {@link SideRailSectionsState_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.SideRailSectionsState#create()
	 */
	public SideRailSectionsState_Impl() {
		super();
	}

	@Override
	public final java.util.List<com.top_logic.layout.react.state.SideRailState.Section> getSections() {
		return _sections;
	}

	@Override
	public com.top_logic.layout.react.state.SideRailSectionsState setSections(java.util.List<? extends com.top_logic.layout.react.state.SideRailState.Section> value) {
		internalSetSections(value);
		return this;
	}

	/** Internal setter for {@link #getSections()} without chain call utility. */
	protected final void internalSetSections(java.util.List<? extends com.top_logic.layout.react.state.SideRailState.Section> value) {
		if (value == null) throw new IllegalArgumentException("Property 'sections' cannot be null.");
		_sections.clear();
		_sections.addAll(value);
	}

	@Override
	public com.top_logic.layout.react.state.SideRailSectionsState addSection(com.top_logic.layout.react.state.SideRailState.Section value) {
		internalAddSection(value);
		return this;
	}

	/** Implementation of {@link #addSection(com.top_logic.layout.react.state.SideRailState.Section)} without chain call utility. */
	protected final void internalAddSection(com.top_logic.layout.react.state.SideRailState.Section value) {
		_sections.add(value);
	}

	@Override
	public final void removeSection(com.top_logic.layout.react.state.SideRailState.Section value) {
		_sections.remove(value);
	}

	@Override
	public com.top_logic.layout.react.state.SideRailSectionsState setHidden(boolean value) {
		internalSetHidden(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.SideRailSectionsState setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public String jsonType() {
		return SIDE_RAIL_SECTIONS_STATE__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(SECTIONS__PROP);
		out.beginArray();
		for (com.top_logic.layout.react.state.SideRailState.Section x : getSections()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case SECTIONS__PROP: {
				java.util.List<com.top_logic.layout.react.state.SideRailState.Section> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.layout.react.state.SideRailState.Section.readSection(in));
				}
				in.endArray();
				setSections(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
