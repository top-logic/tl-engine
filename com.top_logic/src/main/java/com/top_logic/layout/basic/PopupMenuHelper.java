/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.util.function.Consumer;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.mig.html.layout.VisibilityListener;

/**
 * Helper that observes a list of {@link CommandModel}s and forwards the combined visibility
 * (visible iff at least one command is visible) for a callback.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class PopupMenuHelper extends AbstractAttachable {

	/**
	 * {@link VisibilityListener} adapting the {@link PopupMenuHelper#updateVisibility()} when
	 * anything changes.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private final class VisibilityChanged implements VisibilityListener {

		VisibilityChanged() {
		}

		@Override
		public Bubble handleVisibilityChange(Object sender, Boolean oldVisibility, Boolean newVisibility) {
			updateVisibility();
			return Bubble.BUBBLE;
		}
	}

	private final Menu _menu;

	private final VisibilityListener _listener = new VisibilityChanged();

	private final Consumer<Boolean> _visibilitySink;

	/**
	 * Creates a new {@link PopupMenuHelper}.
	 * 
	 * @param visibilitySink
	 *        {@link Consumer} that is filled with {@link Boolean#TRUE} when any of the given
	 *        commands is visible and with {@link Boolean#FALSE} otherwise.
	 * @param menu
	 *        Menu to observe.
	 * 
	 */
	public PopupMenuHelper(Consumer<Boolean> visibilitySink, Menu menu) {
		_visibilitySink = visibilitySink;
		_menu = menu;
	}

	/**
	 * Starts observing all {@link CommandModel} in {@link #getMenu()}.
	 */
	@Override
	protected void internalAttach() {
		_menu.buttons().forEach(this::registerListener);
		updateVisibility();
	}

	private void registerListener(CommandModel model) {
		model.addListener(CommandModel.VISIBLE_PROPERTY, _listener);
	}

	/**
	 * Stops observing all {@link CommandModel} in {@link #getMenu()}.
	 */
	@Override
	protected void internalDetach() {
		_menu.buttons().forEach(this::unregisterListener);
	}

	private void unregisterListener(CommandModel model) {
		model.removeListener(CommandModel.VISIBLE_PROPERTY, _listener);
	}

	void updateVisibility() {
		_visibilitySink.accept(anyCommandVisible());
	}

	private boolean anyCommandVisible() {
		return getMenu().hasVisibleEntries();
	}

	/**
	 * The observed commands.
	 */
	public Menu getMenu() {
		return _menu;
	}

}

