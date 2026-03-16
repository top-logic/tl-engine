/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for {@link ReactControl}s that support toolbar buttons in their header area.
 *
 * <p>
 * Provides a managed list of toolbar button controls with add/remove operations that automatically
 * synchronize the {@code toolbarButtons} state key. Subclasses inherit the toolbar button management
 * and must call {@link #cleanupToolbarButtons()} from their {@link #cleanupChildren()} override.
 * </p>
 */
public abstract class ToolbarControl extends ReactControl {

	/** State key for toolbar buttons. */
	protected static final String TOOLBAR_BUTTONS = "toolbarButtons";

	private final List<ReactControl> _toolbarButtons = new ArrayList<>();

	/**
	 * Creates a new {@link ToolbarControl}.
	 *
	 * @param context
	 *        The React context.
	 * @param controlId
	 *        Optional fixed control ID, or {@code null} for auto-allocation.
	 * @param reactModule
	 *        The React component name.
	 */
	protected ToolbarControl(com.top_logic.layout.react.ReactContext context, String controlId, String reactModule) {
		super(context, controlId, reactModule);
		getReactState().put(TOOLBAR_BUTTONS, new ArrayList<>());
	}

	/**
	 * Adds a toolbar button.
	 */
	public void addToolbarButton(ReactControl button) {
		_toolbarButtons.add(button);
		toolbarButtonList().add(button);
	}

	/**
	 * Removes a toolbar button.
	 *
	 * @return {@code true} if the button was found and removed.
	 */
	public boolean removeToolbarButton(ReactControl button) {
		boolean removed = _toolbarButtons.remove(button);
		if (removed) {
			toolbarButtonList().remove(button);
			button.cleanupTree();
		}
		return removed;
	}

	/**
	 * Cleans up all toolbar button controls. Must be called from {@link #cleanupChildren()}.
	 */
	protected void cleanupToolbarButtons() {
		for (ReactControl button : _toolbarButtons) {
			button.cleanupTree();
		}
	}

	@SuppressWarnings("unchecked")
	private List<Object> toolbarButtonList() {
		return (List<Object>) getReactState().get(TOOLBAR_BUTTONS);
	}
}
