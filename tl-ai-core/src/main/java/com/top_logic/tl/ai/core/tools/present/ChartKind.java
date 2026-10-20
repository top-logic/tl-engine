/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.present;

/**
 * The kind of chart a {@link ChartSpec} draws.
 *
 * <p>
 * Each constant is a Chart.js chart type. The set is kept to the kinds the short forms need - one
 * value per object, a number per label, a few series side by side. Every other chart - stacked, a
 * line over bars, a second axis, a doughnut - is a whole Chart.js configuration the query returns,
 * which names its type itself; see {@link ChartData}.
 * </p>
 */
public enum ChartKind {

	/** Bars, one per object or label, side by side per series. */
	BAR("bar"),

	/** A line through the values, in row order; one line per series. */
	LINE("line"),

	/** Slices of a whole. */
	PIE("pie");

	private final String _chartJsType;

	ChartKind(String chartJsType) {
		_chartJsType = chartJsType;
	}

	/**
	 * The {@code type} a Chart.js configuration names this kind by.
	 */
	public String chartJsType() {
		return _chartJsType;
	}
}
