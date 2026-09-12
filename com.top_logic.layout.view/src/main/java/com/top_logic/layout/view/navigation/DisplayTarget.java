/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.navigation;

import java.util.List;

import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;

/**
 * Where the application displays an object of one model type.
 *
 * <p>
 * Showing an object means displaying the {@link #shows() views} in order, each with the values its
 * {@link ShowStep#bindings() bindings} compute from that object: the enclosing displays first, the
 * one holding the object itself last.
 * </p>
 *
 * @param type
 *        The model type whose objects are displayed here.
 * @param isDefault
 *        Whether this target is preferred over the other targets declared for the same
 *        {@link #type()}.
 * @param shows
 *        The views to display, outermost first. Never empty.
 */
public record DisplayTarget(TLType type, boolean isDefault, List<ShowStep> shows) {

	/**
	 * Creates a {@link DisplayTarget} with an unmodifiable copy of the given views.
	 */
	public DisplayTarget {
		shows = List.copyOf(shows);
		if (shows.isEmpty()) {
			throw new IllegalArgumentException(
				"A display target for type '" + TLModelUtil.qualifiedName(type) + "' displays no view.");
		}
	}

	/**
	 * The view the display of an object starts with, deciding where the target is located.
	 */
	public ShowStep first() {
		return shows.get(0);
	}

	@Override
	public String toString() {
		return TLModelUtil.qualifiedName(type) + "->" + shows;
	}
}
