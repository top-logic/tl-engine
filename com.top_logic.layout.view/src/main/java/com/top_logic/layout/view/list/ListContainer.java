/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.list;

import com.top_logic.model.TLObject;

/**
 * The container an {@link ObjectListElement} lists the elements of.
 */
final class ListContainer {

	private ListContainer() {
		// Static utility.
	}

	/**
	 * The given container while it is alive, {@code null} for a deleted one.
	 *
	 * <p>
	 * A deleted container is no container: it holds no elements, and the element function cannot
	 * be evaluated on it. Reading the container through this mapping makes a deleted container
	 * differ from the alive one it was, so that a list built for it is rebuilt empty and the
	 * element being composed for it is dropped.
	 * </p>
	 *
	 * @param container
	 *        The container as delivered by the container channel, may be {@code null}.
	 * @return The given container, or {@code null} if it is no longer
	 *         {@link TLObject#tValid() valid}.
	 */
	static Object aliveOrNull(Object container) {
		if (container instanceof TLObject object && !object.tValid()) {
			return null;
		}
		return container;
	}

}
