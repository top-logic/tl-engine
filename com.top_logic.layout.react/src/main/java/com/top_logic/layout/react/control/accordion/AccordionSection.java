/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.accordion;

import java.util.function.Supplier;

import com.top_logic.layout.react.control.ReactControl;

/**
 * Definition of a single section of a {@link ReactAccordionControl}.
 *
 * <p>
 * Each section has a unique identifier, a label displayed in its header, and a factory for lazily
 * creating the section's content control. Optionally, the header shows an icon and a control with
 * actions for the section (a toolbar, for instance).
 * </p>
 */
public class AccordionSection {

	private final String _id;

	private final String _label;

	private final Supplier<ReactControl> _contentFactory;

	private boolean _expanded;

	private String _icon;

	private ReactControl _actions;

	/**
	 * Creates a new {@link AccordionSection} that is initially collapsed.
	 *
	 * @param id
	 *        The identifier of this section, unique within its accordion.
	 * @param label
	 *        The label displayed in the header.
	 * @param contentFactory
	 *        Factory creating the content control the first time the section is expanded.
	 */
	public AccordionSection(String id, String label, Supplier<ReactControl> contentFactory) {
		_id = id;
		_label = label;
		_contentFactory = contentFactory;
	}

	/**
	 * The identifier of this section, unique within its accordion.
	 */
	public String getId() {
		return _id;
	}

	/**
	 * The label displayed in the header.
	 */
	public String getLabel() {
		return _label;
	}

	/**
	 * Factory creating the content control the first time the section is expanded.
	 */
	public Supplier<ReactControl> getContentFactory() {
		return _contentFactory;
	}

	/**
	 * Whether the section is expanded when the accordion is created.
	 */
	public boolean isExpanded() {
		return _expanded;
	}

	/**
	 * Sets whether the section is expanded when the accordion is created.
	 *
	 * @param expanded
	 *        Whether the section is initially expanded.
	 * @return This instance for fluent chaining.
	 */
	public AccordionSection withExpanded(boolean expanded) {
		_expanded = expanded;
		return this;
	}

	/**
	 * The icon displayed in the header, the encoded form of a theme image, or {@code null} for no
	 * icon.
	 */
	public String getIcon() {
		return _icon;
	}

	/**
	 * Sets the icon displayed in the header.
	 *
	 * @param icon
	 *        The encoded form of a theme image, or {@code null} for no icon.
	 * @return This instance for fluent chaining.
	 */
	public AccordionSection withIcon(String icon) {
		_icon = icon;
		return this;
	}

	/**
	 * The control with the actions displayed in the header, or {@code null} for no actions.
	 */
	public ReactControl getActions() {
		return _actions;
	}

	/**
	 * Sets the control with the actions displayed in the header.
	 *
	 * <p>
	 * The control becomes a child of the accordion, which disposes it together with itself.
	 * </p>
	 *
	 * @param actions
	 *        The actions control (a toolbar, for instance), or {@code null} for no actions.
	 * @return This instance for fluent chaining.
	 */
	public AccordionSection withActions(ReactControl actions) {
		_actions = actions;
		return this;
	}

}
