/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.util.Arrays;
import java.util.List;

/**
 * A group of content a {@link UIElement} holds according to its configuration.
 *
 * <p>
 * A container reports its content as groups through {@link UIElement#getChildGroups()}. A container
 * that addresses parts of its content by a key of its own (a sidebar item id, a tab id, the
 * selector or detail side of a master-detail element) reports each part under that key; a container
 * that displays all of its content at once reports it under {@link #NO_KEY}.
 * </p>
 *
 * <p>
 * The content is either a list of nested {@link UIElement}s ({@link Elements}) or the reference to
 * another view file ({@link EmbeddedView}). A reference is reported by its path only: whoever
 * traverses the element tree decides how a view file is resolved.
 * </p>
 */
public sealed interface ChildGroup {

	/** The key of a group whose content is not separately addressable within its container. */
	String NO_KEY = "";

	/**
	 * The key under which the container addresses this group, or {@link #NO_KEY}.
	 */
	String key();

	/**
	 * A group of nested elements.
	 *
	 * @param key
	 *        See {@link ChildGroup#key()}.
	 * @param children
	 *        The nested elements, in configuration order.
	 */
	record Elements(String key, List<UIElement> children) implements ChildGroup {
		// Pure data.
	}

	/**
	 * A group whose content is the referenced view file.
	 *
	 * @param key
	 *        See {@link ChildGroup#key()}.
	 * @param viewPath
	 *        Path of the referenced view file, relative to {@link ViewLoader#VIEW_BASE_PATH}, as
	 *        written in the configuration.
	 */
	record EmbeddedView(String key, String viewPath) implements ChildGroup {
		// Pure data.
	}

	/**
	 * A group of nested elements displayed as part of their container.
	 *
	 * @param children
	 *        The nested elements, in configuration order.
	 * @return The group.
	 */
	static ChildGroup elements(List<UIElement> children) {
		return new Elements(NO_KEY, children);
	}

	/**
	 * A group of nested elements displayed as part of their container.
	 *
	 * @param children
	 *        The nested elements, in configuration order. A {@code null} entry is dropped, so that a
	 *        container with an optional content element can pass it directly.
	 * @return The group.
	 */
	static ChildGroup elements(UIElement... children) {
		return elements(Arrays.stream(children).filter(child -> child != null).toList());
	}

	/**
	 * A group of nested elements the container addresses by the given key.
	 *
	 * @param key
	 *        See {@link ChildGroup#key()}.
	 * @param children
	 *        The nested elements, in configuration order.
	 * @return The group.
	 */
	static ChildGroup keyed(String key, List<UIElement> children) {
		return new Elements(key, children);
	}

	/**
	 * A reference to another view file, displayed as part of its container.
	 *
	 * @param viewPath
	 *        See {@link EmbeddedView#viewPath()}.
	 * @return The group.
	 */
	static ChildGroup view(String viewPath) {
		return new EmbeddedView(NO_KEY, viewPath);
	}

	/**
	 * A reference to another view file the container addresses by the given key.
	 *
	 * @param key
	 *        See {@link ChildGroup#key()}.
	 * @param viewPath
	 *        See {@link EmbeddedView#viewPath()}.
	 * @return The group.
	 */
	static ChildGroup view(String key, String viewPath) {
		return new EmbeddedView(key, viewPath);
	}
}
