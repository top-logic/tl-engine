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

import com.top_logic.react.flow.data.GanttItem;
import com.top_logic.react.flow.data.GanttPoint;
import com.top_logic.react.flow.data.GanttRow;
import com.top_logic.react.flow.data.GanttSpan;
import com.top_logic.react.flow.data.Text;
import com.top_logic.react.flow.server.axis.AxisContent;
import com.top_logic.react.flow.server.axis.AxisProvider;

/**
 * {@link AxisProvider} that interprets positions as days since 1970-01-01 and emits year rows with
 * span items and month rows with point items. Used by the Gantt demo; applications will usually
 * register their own provider.
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
	public AxisContent buildAxis(double rangeMin, double rangeMax, double pixelsPerUnit) {
		List<GanttRow> rows = new ArrayList<>();
		List<GanttItem> items = new ArrayList<>();

		// Row user objects used to link items to rows.
		String yearRowModel = "axis:years";
		String monthRowModel = "axis:months";

		GanttRow yearRow = GanttRow.create()
			.setUserObject(yearRowModel)
			.setLabel(Text.create().setValue("Year"));
		GanttRow monthRow = GanttRow.create()
			.setUserObject(monthRowModel)
			.setLabel(Text.create().setValue("Month"));

		rows.add(yearRow);
		rows.add(monthRow);

		LocalDate min = LocalDate.ofEpochDay((long) Math.floor(rangeMin));
		LocalDate max = LocalDate.ofEpochDay((long) Math.ceil(rangeMax));

		// Year spans: one bar per year covering Jan 1 to Dec 31.
		int firstYear = min.getYear();
		int lastYear = max.getYear();
		for (int year = firstYear; year <= lastYear; year++) {
			LocalDate janFirst = LocalDate.of(year, Month.JANUARY, 1);
			LocalDate decLast = LocalDate.of(year, Month.DECEMBER, 31);
			GanttSpan yearSpan = GanttSpan.create()
				.setUserObject("year:" + year)
				.setRowModel(yearRowModel)
				.setStart(janFirst.toEpochDay())
				.setEnd(decLast.toEpochDay())
				.setBox(Text.create().setValue(String.valueOf(year)))
				.setCanMoveTime(false)
				.setCanMoveRow(false)
				.setCanResizeStart(false)
				.setCanResizeEnd(false)
				.setCanBeEdgeSource(false)
				.setCanBeEdgeTarget(false);
			items.add(yearSpan);
		}

		// Month points: one point per month start within range.
		LocalDate firstOfMonth = min.withDayOfMonth(1);
		if (firstOfMonth.isBefore(min)) {
			firstOfMonth = firstOfMonth.plusMonths(1);
		}
		for (LocalDate d = firstOfMonth; !d.isAfter(max); d = d.plusMonths(1)) {
			String abbr = d.getMonth().name().substring(0, 3);
			GanttPoint monthPoint = GanttPoint.create()
				.setUserObject("month:" + d.getYear() + "-" + d.getMonthValue())
				.setRowModel(monthRowModel)
				.setAt(d.toEpochDay())
				.setBox(Text.create().setValue(abbr))
				.setCanMoveTime(false)
				.setCanMoveRow(false)
				.setCanBeEdgeSource(false)
				.setCanBeEdgeTarget(false);
			items.add(monthPoint);
		}

		return new AxisContent(rows, items);
	}

	@Override
	public double snapGranularity(double pixelsPerUnit) {
		// Snap to whole days -- finer granularity could be added when zoomed in.
		return 1.0;
	}
}
