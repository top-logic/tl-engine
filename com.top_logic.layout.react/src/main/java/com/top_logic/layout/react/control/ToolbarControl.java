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
		putState(TOOLBAR_BUTTONS, _toolbarButtons);
	}

	/**
	 * Adds a toolbar button.
	 */
	public void addToolbarButton(ReactControl button) {
		_toolbarButtons.add(button);
		putState(TOOLBAR_BUTTONS, _toolbarButtons);
		if (isAttached()) {
			button.attach();
		}
	}

	/**
	 * Removes a toolbar button.
	 *
	 * @return {@code true} if the button was found and removed.
	 */
	public boolean removeToolbarButton(ReactControl button) {
		boolean removed = _toolbarButtons.remove(button);
		if (removed) {
			putState(TOOLBAR_BUTTONS, _toolbarButtons);
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

	@Override
	protected void propagateAttach() {
		super.propagateAttach();
		for (ReactControl btn : _toolbarButtons) {
			btn.attach();
		}
	}

	@Override
	protected void propagateDetach() {
		super.propagateDetach();
		for (ReactControl btn : _toolbarButtons) {
			btn.detach();
		}
	}
}
