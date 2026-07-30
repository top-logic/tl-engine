/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.tabbar;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.dirty.DirtyChannel;

/**
 * Definition of a single tab in a {@link ReactTabBarControl}.
 *
 * <p>
 * Each tab has a unique identifier, a display label, and a factory for lazily creating the tab's
 * content control.
 * </p>
 */
public class TabDefinition {

	private final String _id;

	private final String _label;

	private final Supplier<ReactControl> _contentFactory;

	private final DirtyChannel _dirtyChannel;

	private String _route;

	private String _icon;

	private Consumer<Boolean> _activationListener;

	/**
	 * Creates a new {@link TabDefinition}.
	 *
	 * @param id
	 *        The unique identifier for this tab.
	 * @param label
	 *        The display label.
	 * @param contentFactory
	 *        Factory to lazily create the tab's content control.
	 * @param dirtyChannel
	 *        The dirty channel tracking unsaved changes in this tab's content, or {@code null}.
	 */
	public TabDefinition(String id, String label, Supplier<ReactControl> contentFactory, DirtyChannel dirtyChannel) {
		_id = id;
		_label = label;
		_contentFactory = contentFactory;
		_dirtyChannel = dirtyChannel;
	}

	/**
	 * The unique identifier for this tab.
	 */
	public String getId() {
		return _id;
	}

	/**
	 * The display label.
	 */
	public String getLabel() {
		return _label;
	}

	/**
	 * Factory to lazily create the tab's content control.
	 */
	public Supplier<ReactControl> getContentFactory() {
		return _contentFactory;
	}

	/**
	 * The dirty channel tracking unsaved changes in this tab's content, or {@code null}.
	 */
	public DirtyChannel getDirtyChannel() {
		return _dirtyChannel;
	}

	/**
	 * The route pattern for this tab, or {@code null} if this tab is not route-forming.
	 */
	public String getRoute() {
		return _route;
	}

	/**
	 * Sets the route pattern for this tab.
	 *
	 * @param route
	 *        The route pattern (e.g. "/settings"), or {@code null} for no route.
	 * @return This instance for fluent chaining.
	 */
	public TabDefinition withRoute(String route) {
		_route = route;
		return this;
	}

	/**
	 * The CSS icon class for this tab (e.g. {@code "fa-solid fa-tags"}), or {@code null} for no
	 * icon.
	 */
	public String getIcon() {
		return _icon;
	}

	/**
	 * Sets the CSS icon class for this tab.
	 *
	 * @param icon
	 *        The CSS icon class, or {@code null} for no icon.
	 * @return This instance for fluent chaining.
	 */
	public TabDefinition withIcon(String icon) {
		_icon = icon;
		return this;
	}

	/**
	 * Informs the {@link #withActivation(Consumer) activation listener} that this tab became the
	 * displayed one, or stopped being it.
	 *
	 * <p>
	 * Called for the tab losing and the tab gaining the display, in that order, so that a listener
	 * moving something into a shared place (e.g. the enclosing toolbar) never has both tabs'
	 * contributions there at once.
	 * </p>
	 *
	 * @param active
	 *        Whether this tab is now the displayed one.
	 */
	public void notifyActivation(boolean active) {
		if (_activationListener != null) {
			_activationListener.accept(active);
		}
	}

	/**
	 * Sets the listener informed whenever this tab becomes the displayed one or stops being it.
	 *
	 * <p>
	 * Unlike the attach/detach lifecycle of the content control, this is signaled even for a tab bar
	 * that is itself not attached, so it is the reliable hook for content that must not contribute to
	 * its surroundings while hidden.
	 * </p>
	 *
	 * @param listener
	 *        The listener receiving whether this tab is the displayed one, or {@code null} for none.
	 * @return This instance for fluent chaining.
	 */
	public TabDefinition withActivation(Consumer<Boolean> listener) {
		_activationListener = listener;
		return this;
	}

}
