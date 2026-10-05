/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.accordion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ScriptingControl;
import com.top_logic.layout.react.reveal.ChildRevealer;
import com.top_logic.layout.react.state.AccordionState;

/**
 * A {@link ReactControl} that renders a stack of sections, each with a header and a body that is
 * expanded or collapsed independently.
 *
 * <p>
 * The content of a section is created the first time the section is expanded (for a section that
 * is initially expanded, when the accordion is attached or written). From then on it stays part of
 * the state: collapsing a section only hides its content, so that what the user did there survives
 * until the section is expanded again. A section never expanded has no content.
 * </p>
 *
 * <p>
 * In an {@link #isExclusive() exclusive} accordion, at most one section is expanded at a time:
 * expanding a section collapses all others. All sections may be collapsed.
 * </p>
 *
 * <p>
 * The state is described by {@link AccordionState}. The client expands and collapses a section
 * through the {@link #TOGGLE_SECTION_COMMAND} command. Every change of a section's expansion is
 * reported to the {@link #addExpansionListener(AccordionExpansionListener) expansion listeners}.
 * </p>
 */
public class ReactAccordionControl extends ReactControl implements ChildRevealer {

	private static final String REACT_MODULE = "TLAccordion";

	/** The {@link ReactCommandHandler} that expands or collapses a section. */
	public static final String TOGGLE_SECTION_COMMAND = "toggleSection";

	/** Role of the navigation slot addressing the content of a section, keyed by its id. */
	public static final String SECTION_SLOT = "section";

	/** Role of the navigation slot addressing the header actions of a section, keyed by its id. */
	public static final String ACTIONS_SLOT = "actions";

	private final Map<String, AccordionSection> _sections = new LinkedHashMap<>();

	private final boolean _exclusive;

	private final Set<String> _expanded = new HashSet<>();

	private final Map<String, ReactControl> _contents = new HashMap<>();

	private final List<AccordionExpansionListener> _expansionListeners = new ArrayList<>();

	/**
	 * Creates a new {@link ReactAccordionControl}.
	 *
	 * @param context
	 *        The context of the control.
	 * @param model
	 *        The server-side model object.
	 * @param sections
	 *        The sections, in the order from top to bottom. Their
	 *        {@link AccordionSection#isExpanded() expansion} is the initial one; in an exclusive
	 *        accordion, only the first of the initially expanded sections is expanded.
	 * @param exclusive
	 *        Whether at most one section is expanded at a time.
	 */
	public ReactAccordionControl(ReactContext context, Object model, List<AccordionSection> sections,
			boolean exclusive) {
		super(context, model, REACT_MODULE);
		_exclusive = exclusive;
		for (AccordionSection section : sections) {
			String id = section.getId();
			if (_sections.put(id, section) != null) {
				throw new IllegalArgumentException("Duplicate section ID: " + id);
			}
			if (section.isExpanded() && !(exclusive && !_expanded.isEmpty())) {
				_expanded.add(id);
			}
		}
		putState(AccordionState.EXCLUSIVE__PROP, exclusive);
		writeSections();
		// The content of the expanded sections is created when this accordion is attached (or
		// written) - see onAttach().
	}

	/**
	 * Whether at most one section is expanded at a time.
	 */
	public boolean isExclusive() {
		return _exclusive;
	}

	/**
	 * Whether the section with the given ID is expanded.
	 *
	 * @param sectionId
	 *        The {@link AccordionSection#getId() id} of the section.
	 * @throws IllegalArgumentException
	 *         If this accordion has no section with the given ID.
	 */
	public boolean isExpanded(String sectionId) {
		findSection(sectionId);
		return _expanded.contains(sectionId);
	}

	/**
	 * Expands or collapses the section with the given ID.
	 *
	 * <p>
	 * Expanding a section creates its content, unless it was expanded before. In an
	 * {@link #isExclusive() exclusive} accordion, expanding a section collapses all others. Each
	 * section whose expansion changes is reported to the
	 * {@link #addExpansionListener(AccordionExpansionListener) expansion listeners}.
	 * </p>
	 *
	 * @param sectionId
	 *        The {@link AccordionSection#getId() id} of the section.
	 * @param expanded
	 *        Whether the section is to be expanded.
	 * @throws IllegalArgumentException
	 *         If this accordion has no section with the given ID.
	 */
	public void setExpanded(String sectionId, boolean expanded) {
		findSection(sectionId);
		if (_expanded.contains(sectionId) == expanded) {
			return;
		}

		List<String> collapsed = new ArrayList<>();
		if (expanded) {
			if (_exclusive) {
				for (String other : _sections.keySet()) {
					if (_expanded.remove(other)) {
						collapsed.add(other);
					}
				}
			}
			_expanded.add(sectionId);
		} else {
			_expanded.remove(sectionId);
		}

		List<ReactControl> created = new ArrayList<>();
		if (expanded && isAttached()) {
			// An accordion not yet displayed creates the content when it is attached or written.
			ReactControl content = createContent(sectionId);
			if (content != null) {
				created.add(content);
			}
		}

		Object tx = beginUpdate();
		writeSections();
		commitUpdate(tx);

		for (ReactControl content : created) {
			content.attach();
		}

		for (String other : collapsed) {
			notifyExpansionChanged(other, false);
		}
		notifyExpansionChanged(sectionId, expanded);
	}

	/**
	 * Registers a listener that is informed whenever a section is expanded or collapsed.
	 *
	 * @param listener
	 *        The listener to add.
	 */
	public void addExpansionListener(AccordionExpansionListener listener) {
		_expansionListeners.add(listener);
	}

	/**
	 * Unregisters a listener added by {@link #addExpansionListener(AccordionExpansionListener)}.
	 *
	 * @param listener
	 *        The listener to remove.
	 */
	public void removeExpansionListener(AccordionExpansionListener listener) {
		_expansionListeners.remove(listener);
	}

	private void notifyExpansionChanged(String sectionId, boolean expanded) {
		for (AccordionExpansionListener listener : new ArrayList<>(_expansionListeners)) {
			listener.onExpansionChanged(this, sectionId, expanded);
		}
	}

	@Override
	protected void onAttach() {
		super.onAttach();
		// The content of an expanded section comes into existence here rather than at the first
		// write, because what it contributes to its surroundings has to be in place before those
		// surroundings are rendered. Putting the content into the state now also lets the attach
		// propagation that follows this hook reach it.
		materializeExpandedContents();
	}

	@Override
	protected void onBeforeWrite() {
		super.onBeforeWrite();
		// Covers an accordion written without being attached; an attached one materialized its
		// content in onAttach().
		materializeExpandedContents();
	}

	/**
	 * Creates the content of each expanded section that has none yet, and displays it.
	 */
	private void materializeExpandedContents() {
		List<ReactControl> created = new ArrayList<>();
		for (String sectionId : _sections.keySet()) {
			if (_expanded.contains(sectionId)) {
				ReactControl content = createContent(sectionId);
				if (content != null) {
					created.add(content);
				}
			}
		}
		if (created.isEmpty()) {
			return;
		}
		writeSections();
		if (isAttached()) {
			for (ReactControl content : created) {
				content.attach();
			}
		}
	}

	/**
	 * Creates the content of the given section, unless it already exists.
	 *
	 * @return The created content, or {@code null} if the section had its content already.
	 */
	private ReactControl createContent(String sectionId) {
		if (_contents.containsKey(sectionId)) {
			return null;
		}
		ReactControl content = findSection(sectionId).getContentFactory().get();
		_contents.put(sectionId, content);
		return content;
	}

	/**
	 * Puts the {@link AccordionState#SECTIONS__PROP sections} into the state.
	 */
	private void writeSections() {
		List<Map<String, Object>> sectionList = new ArrayList<>();
		for (AccordionSection section : _sections.values()) {
			String id = section.getId();
			Map<String, Object> sectionInfo = new HashMap<>();
			sectionInfo.put(AccordionState.Section.ID__PROP, id);
			sectionInfo.put(AccordionState.Section.LABEL__PROP, section.getLabel());
			if (section.getIcon() != null) {
				sectionInfo.put(AccordionState.Section.ICON__PROP, section.getIcon());
			}
			sectionInfo.put(AccordionState.Section.EXPANDED__PROP, _expanded.contains(id));
			if (section.getActions() != null) {
				sectionInfo.put(AccordionState.Section.ACTIONS__PROP, section.getActions());
			}
			ReactControl content = _contents.get(id);
			if (content != null) {
				sectionInfo.put(AccordionState.Section.CONTENT__PROP, content);
			}
			sectionList.add(sectionInfo);
		}
		putState(AccordionState.SECTIONS__PROP, sectionList);
	}

	private AccordionSection findSection(String sectionId) {
		AccordionSection section = _sections.get(sectionId);
		if (section == null) {
			throw new IllegalArgumentException("Unknown section ID: " + sectionId);
		}
		return section;
	}

	/**
	 * The header actions of all sections and the content of the expanded ones: the content of a
	 * collapsed section is rendered but hidden.
	 */
	@Override
	public List<ReactControl> visibleChildren() {
		List<ReactControl> result = new ArrayList<>();
		for (AccordionSection section : _sections.values()) {
			String id = section.getId();
			if (section.getActions() != null) {
				result.add(section.getActions());
			}
			ReactControl content = _contents.get(id);
			if (content != null && _expanded.contains(id)) {
				result.add(content);
			}
		}
		return result;
	}

	// -- Commands --

	/**
	 * Expands the section with the given id. Revealing a section that is expanded already changes
	 * nothing.
	 */
	@Override
	public void revealChild(String key) {
		setExpanded(key, true);
	}

	/**
	 * Handles expanding and collapsing a section from the client.
	 */
	@ReactCommandHandler(TOGGLE_SECTION_COMMAND)
	void handleToggleSection(ToggleSectionArguments args) {
		setExpanded(args.getSectionId(), args.isExpanded());
	}

	/**
	 * Addresses the content of a section by the section's stable ID (e.g. {@code section[details]}),
	 * and its header actions likewise (e.g. {@code actions[details]}).
	 */
	@Override
	public String scriptingChildSlot(ReactControl child) {
		for (AccordionSection section : _sections.values()) {
			String id = section.getId();
			if (child == _contents.get(id)) {
				return ScriptingControl.slotSegment(SECTION_SLOT, id);
			}
			if (child == section.getActions()) {
				return ScriptingControl.slotSegment(ACTIONS_SLOT, id);
			}
		}
		return null;
	}

}
