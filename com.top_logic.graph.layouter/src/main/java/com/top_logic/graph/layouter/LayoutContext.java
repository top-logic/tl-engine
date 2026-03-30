/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter;

/**
 * Context for graph layouting.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven F&ouml;rster</a>
 */
public class LayoutContext {

	private final LayoutDirection _direction;

	/**
	 * Creates a {@link LayoutContext} with the given layout direction.
	 */
	public LayoutContext(LayoutDirection direction) {
		_direction = direction;
	}

	/**
	 * the layouting direction.
	 */
	public LayoutDirection getDirection() {
		return _direction;
	}

}
