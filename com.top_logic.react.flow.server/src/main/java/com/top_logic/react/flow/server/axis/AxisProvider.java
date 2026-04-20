/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.server.axis;

/**
 * Server-side axis implementation for a {@link com.top_logic.react.flow.data.GanttLayout}.
 *
 * <p>
 * A provider encapsulates application-specific time semantics by computing axis rows and items for
 * a given visible range and zoom level, plus a snap granularity for client-side drag rounding.
 * Applications register providers in the {@link AxisProviderService} and reference them from
 * {@link com.top_logic.react.flow.data.GanttAxis#getProviderId()}.
 * </p>
 */
public interface AxisProvider {

	/**
	 * Identifier used in {@code GanttAxis.providerId}.
	 */
	String getId();

	/**
	 * Build axis rows and items for the given range and zoom. The returned rows become part of the
	 * chart's row forest; items become regular Gantt items. The provider decides how many levels to
	 * produce based on the zoom.
	 *
	 * @param rangeMin
	 *        minimum position (layout units at zoom 100%)
	 * @param rangeMax
	 *        maximum position
	 * @param pixelsPerUnit
	 *        current zoom factor (1.0 = 100%)
	 */
	AxisContent buildAxis(double rangeMin, double rangeMax, double pixelsPerUnit);

	/**
	 * Snap granularity (layout units) for the given zoom level. Client uses this during drag to
	 * round positions to the nearest multiple. Defaults to {@code 1.0}.
	 */
	default double snapGranularity(double pixelsPerUnit) {
		return 1.0;
	}
}
