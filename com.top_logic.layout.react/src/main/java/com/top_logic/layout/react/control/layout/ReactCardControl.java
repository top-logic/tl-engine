/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.react.ReactControl;

/**
 * A {@link ReactControl} that renders an elevated content container via the {@code TLCard} React
 * component.
 *
 * <p>
 * A card is lighter than {@link ReactPanelControl} - it provides visual grouping with an optional
 * header but no minimize/maximize/pop-out behavior.
 * </p>
 *
 * <p>
 * State:
 * </p>
 * <ul>
 * <li>{@code title} - optional header text</li>
 * <li>{@code variant} - "outlined" or "elevated"</li>
 * <li>{@code padding} - "none", "compact", or "default"</li>
 * <li>{@code headerActions} - optional buttons in the header</li>
 * <li>{@code child} - the content child</li>
 * </ul>
 */
public class ReactCardControl extends ReactControl {

	private static final String REACT_MODULE = "TLCard";

	private static final String TITLE = "title";

	private static final String VARIANT = "variant";

	private static final String PADDING = "padding";

	private static final String HEADER_ACTIONS = "headerActions";

	private static final String CHILD = "child";

	private final ReactControl _child;

	private final List<ReactControl> _headerActions;

	/**
	 * Creates a card with full configuration.
	 *
	 * @param title
	 *        The header text, or {@code null} for no header.
	 * @param variant
	 *        "outlined" or "elevated".
	 * @param padding
	 *        "none", "compact", or "default".
	 * @param headerActions
	 *        Optional action buttons in the header.
	 * @param child
	 *        The content child control.
	 */
	public ReactCardControl(String title, String variant, String padding,
			List<? extends ReactControl> headerActions, ReactControl child) {
		super(null, REACT_MODULE);
		_child = child;
		_headerActions = new ArrayList<>(headerActions);
		if (title != null) {
			putState(TITLE, title);
		}
		putState(VARIANT, variant);
		putState(PADDING, padding);
		putState(HEADER_ACTIONS, _headerActions);
		putState(CHILD, child);
	}

	/**
	 * Creates an outlined card with default padding and no header actions.
	 *
	 * @param title
	 *        The header text, or {@code null} for no header.
	 * @param child
	 *        The content child control.
	 */
	public ReactCardControl(String title, ReactControl child) {
		this(title, "outlined", "default", List.of(), child);
	}

	/**
	 * Updates the card title.
	 *
	 * @param title
	 *        The new title, or {@code null} to remove the header.
	 */
	public void setTitle(String title) {
		putState(TITLE, title);
	}

	@Override
	protected void cleanupChildren() {
		_child.cleanupTree();
		for (ReactControl action : _headerActions) {
			action.cleanupTree();
		}
	}

}
