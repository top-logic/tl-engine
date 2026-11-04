/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.gantt.ui;


/**
 * Possible intersections.
 * 
 * @author <a href="mailto:jes@top-logic.com">jes</a>
 */
public enum IntersectInfo {

	/** The areas do not overlap. */
	NO,

	/** Two horizontal lines in the same vertical area overlap and can be joined. */
	X_JOIN,

	/** Two vertical lines in the same horizontal area overlap and can be joined. */
	Y_JOIN,

	/** The areas touch in x direction. */
	TOUCH_X,

	/** The areas touch in y direction. */
	TOUCH_Y,

	/** The areas overlap. */
	YES,

	/** A horizontal and a vertical line cross each other. */
	CROSS,

	/** A horizontal and a vertical line are connected at a corner. */
	CONNECT_ORTH
}