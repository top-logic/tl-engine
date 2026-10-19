/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

/**
 * The position of the legend of a chart.
 */
public enum ChartLegend implements DataEnum {

	/** Below the plot area. */
	BOTTOM("bottom"),

	/** Above the plot area. */
	TOP("top"),

	/** Left of the plot area. */
	LEFT("left"),

	/** Right of the plot area. */
	RIGHT("right"),

	/** The chart has no legend. */
	NONE("none");

	private final String _id;

	ChartLegend(String id) {
		_id = id;
	}

	@Override
	public String id() {
		return _id;
	}

}
