/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.tabbar;

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

	private boolean _finalTitle;

	private boolean _hidden;

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
	 * Whether the label of this tab is the final title of the page while the tab is active.
	 *
	 * <p>
	 * Only meaningful for a tab with a {@link #getRoute() route}: a tab without one names no page.
	 * </p>
	 *
	 * @see com.top_logic.layout.react.routing.RoutingParticipant#titlesContent()
	 */
	public boolean isFinalTitle() {
		return _finalTitle;
	}

	/**
	 * Sets whether the label of this tab is the final title of the page.
	 *
	 * @param finalTitle
	 *        {@code true} to keep the labels of the routes inside the content of this tab out of the
	 *        title of the page.
	 * @return This instance for fluent chaining.
	 *
	 * @see #isFinalTitle()
	 */
	public TabDefinition withFinalTitle(boolean finalTitle) {
		_finalTitle = finalTitle;
		return this;
	}

	/**
	 * Whether this tab is left out of the strip when the tab bar is built.
	 *
	 * @see ReactTabBarControl#setTabHidden(String, boolean)
	 */
	public boolean isHidden() {
		return _hidden;
	}

	/**
	 * Sets whether this tab is left out of the strip when the tab bar is built.
	 *
	 * @param hidden
	 *        {@code true} to leave the tab out.
	 * @return This instance for fluent chaining.
	 */
	public TabDefinition withHidden(boolean hidden) {
		_hidden = hidden;
		return this;
	}

}
