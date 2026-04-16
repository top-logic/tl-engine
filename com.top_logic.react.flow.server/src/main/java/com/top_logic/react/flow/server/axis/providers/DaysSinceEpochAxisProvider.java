/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.server.axis.providers;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.react.flow.data.GanttTick;
import com.top_logic.react.flow.data.Text;
import com.top_logic.react.flow.server.axis.AxisProvider;

/**
 * {@link AxisProvider} that interprets positions as days since 1970-01-01 and emits month and
 * year ticks. Used by the Gantt demo; applications will usually register their own provider.
 */
public class DaysSinceEpochAxisProvider implements AxisProvider {

	/** Default id under which this provider is looked up. */
	public static final String DEFAULT_ID = "days-since-epoch";

	private final String _id;

	/** Bound by the module framework. */
	public DaysSinceEpochAxisProvider() {
		this(DEFAULT_ID);
	}

	/** For tests. */
	public DaysSinceEpochAxisProvider(String id) {
		_id = id;
	}

	@Override
	public String getId() {
		return _id;
	}

	@Override
	public List<GanttTick> ticksFor(double rangeMin, double rangeMax, double pixelsPerUnit) {
		List<GanttTick> ticks = new ArrayList<>();
		LocalDate min = LocalDate.ofEpochDay((long) Math.floor(rangeMin));
		LocalDate max = LocalDate.ofEpochDay((long) Math.ceil(rangeMax));

		LocalDate firstOfMonth = min.withDayOfMonth(1);
		if (firstOfMonth.isBefore(min)) {
			firstOfMonth = firstOfMonth.plusMonths(1);
		}
		for (LocalDate d = firstOfMonth; !d.isAfter(max); d = d.plusMonths(1)) {
			double emphasis = (d.getMonth() == Month.JANUARY) ? 1.0 : 0.35;
			String label = (d.getMonth() == Month.JANUARY)
				? String.valueOf(d.getYear())
				: d.getMonth().name().substring(0, 3);
			ticks.add(GanttTick.create()
				.setPosition(d.toEpochDay())
				.setLabel(Text.create().setValue(label))
				.setEmphasis(emphasis));
		}
		return ticks;
	}

	@Override
	public double snapGranularity(double pixelsPerUnit) {
		// Snap to whole days — finer granularity could be added when zoomed in.
		return 1.0;
	}
}
