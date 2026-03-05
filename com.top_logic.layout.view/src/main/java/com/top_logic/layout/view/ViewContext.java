/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import com.top_logic.layout.react.ViewDisplayContext;

/**
 * Hierarchical context for UIElement control creation.
 *
 * <p>
 * Provides session-scoped infrastructure needed to create and wire controls. Container elements may
 * create derived contexts that add scoped information for their children.
 * </p>
 */
public class ViewContext {

	private final ViewDisplayContext _displayContext;

	private final String _personalizationPath;

	/**
	 * Creates a root {@link ViewContext}.
	 *
	 * @param displayContext
	 *        The view display context providing ID allocation, SSE queue and other rendering
	 *        infrastructure.
	 */
	public ViewContext(ViewDisplayContext displayContext) {
		this(displayContext, "view");
	}

	private ViewContext(ViewDisplayContext displayContext, String personalizationPath) {
		_displayContext = displayContext;
		_personalizationPath = personalizationPath;
	}

	/**
	 * Creates a child context with an appended path segment.
	 *
	 * @param segment
	 *        The segment to append (e.g. "sidebar", "split-panel").
	 * @return A new context with the extended personalization path.
	 */
	public ViewContext childContext(String segment) {
		return new ViewContext(_displayContext, _personalizationPath + "." + segment);
	}

	/**
	 * The personalization key for the current position in the view tree.
	 *
	 * <p>
	 * Auto-derived from the tree path (e.g. "view.sidebar", "view.split-panel"). UIElements may
	 * override this with an explicit personalization key from configuration.
	 * </p>
	 */
	public String getPersonalizationKey() {
		return _personalizationPath;
	}

	/**
	 * The {@link ViewDisplayContext} for the current session.
	 */
	public ViewDisplayContext getDisplayContext() {
		return _displayContext;
	}
}
