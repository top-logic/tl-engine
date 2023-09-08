/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter;

/**
 * Direction of the layout.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public enum LayoutDirection {
	/**
	 * Vertical layout direction from sources to sinks.
	 */
	VERTICAL_FROM_SOURCE,

	/**
	 * Vertical layout direction from sinks to sources.
	 */
	VERTICAL_FROM_SINK,

	/**
	 * Horizontal layout direction from sources to sinks.
	 */
	HORIZONTAL_FROM_SOURCE,

	/**
	 * Horizontal layout direction from sinks to sources.
	 */
	HORIZONTAL_FROM_SINK
}
