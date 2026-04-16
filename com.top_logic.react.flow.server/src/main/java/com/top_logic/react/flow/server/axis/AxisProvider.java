/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.server.axis;

import java.util.List;

import com.top_logic.react.flow.data.GanttTick;

/**
 * Server-side axis implementation for a {@link com.top_logic.react.flow.data.GanttLayout}.
 *
 * <p>
 * A provider encapsulates application-specific time semantics by computing axis ticks for a given
 * visible range and zoom level, plus a snap granularity for client-side drag rounding. Applications
 * register providers in the {@link AxisProviderService} and reference them from
 * {@link com.top_logic.react.flow.data.GanttAxis#getProviderId()}.
 * </p>
 *
 * <p>
 * Phase 1 of the Gantt chart calls only {@link #ticksFor} and {@link #snapGranularity}. The
 * broader axis interface (toPosition/fromPosition/formatLong) is added in later phases.
 * </p>
 */
public interface AxisProvider {

	/**
	 * Identifier used in {@code GanttAxis.providerId}.
	 */
	String getId();

	/**
	 * Compute axis ticks covering the given position range at the given zoom level.
	 *
	 * @param rangeMin
	 *        minimum position (layout units at zoom 100%)
	 * @param rangeMax
	 *        maximum position
	 * @param pixelsPerUnit
	 *        current zoom factor (1.0 = 100%)
	 */
	List<GanttTick> ticksFor(double rangeMin, double rangeMax, double pixelsPerUnit);

	/**
	 * Snap granularity (layout units) for the given zoom level. Client uses this during drag to
	 * round positions to the nearest multiple. Defaults to {@code 1.0}.
	 */
	default double snapGranularity(double pixelsPerUnit) {
		return 1.0;
	}
}
