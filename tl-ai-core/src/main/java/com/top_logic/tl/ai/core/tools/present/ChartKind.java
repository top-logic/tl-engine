/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.present;

/**
 * The kind of chart a {@link ChartSpec} draws.
 *
 * <p>
 * Each constant is a Chart.js chart type. The set is kept to the kinds that
 * one value per object reads well in; anything that needs several series is out of reach of the
 * one-label, one-value spec anyway.
 * </p>
 */
public enum ChartKind {

	/** Bars, one per object. */
	BAR("bar"),

	/** A line through the objects' values, in row order. */
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
