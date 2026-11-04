/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.monitor;

import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.table.TableExportCommand;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.TableView;
import com.top_logic.table.filter.ComparableColumnFilter;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.impl.DefaultColumn;

/**
 * Building blocks of the tables of the monitoring views: typed columns, the selection channel and
 * the export.
 */
public class MonitorTables {

	/**
	 * A column of a point in time, displayed as date and time of the current user, sorted and
	 * filtered (by a range of points in time typed as date and time) by the point in time itself.
	 *
	 * @param id
	 *        The name of the column.
	 * @param label
	 *        The column header.
	 * @param value
	 *        The point in time of a row, <code>null</code> for none.
	 * @param width
	 *        The default width of the column in pixels.
	 */
	public static <R> Column<R, Date> dateColumn(String id, ResKey label, Function<? super R, Date> value,
			int width) {
		return DefaultColumn.<R, Date> builder(id, value)
			.label(label)
			.renderer(time -> CellContent.text(time == null ? "" : HTMLFormatter.getInstance().formatDateTime(time)))
			.sort(() -> Comparator.nullsFirst(Comparator.naturalOrder()))
			.filter(new ComparableColumnFilter<>(Comparator.<Date> naturalOrder(), MonitorTables::parseDateTime))
			.width(width)
			.build();
	}

	/**
	 * A sortable, text-filterable column of a text, the empty text for <code>null</code>.
	 *
	 * @param id
	 *        The name of the column.
	 * @param label
	 *        The column header.
	 * @param value
	 *        The text of a row.
	 * @param width
	 *        The default width of the column in pixels.
	 */
	public static <R> Column<R, String> textColumn(String id, ResKey label, Function<? super R, String> value,
			int width) {
		return DefaultColumn.<R, String> builder(id, value)
			.label(label)
			.renderer(CellContent::text)
			.sort(() -> Comparator.nullsFirst(Comparator.naturalOrder()))
			.filter(new TextColumnFilter<>(text -> text == null ? "" : text))
			.width(width)
			.build();
	}

	/**
	 * Writes the single selected row of the given table to the given channel, <code>null</code>
	 * when no or several rows are selected.
	 *
	 * @param selectionRef
	 *        The channel to write to, <code>null</code> for none.
	 */
	public static void bindSelection(ViewContext context, TableViewControl<?> control, ChannelRef selectionRef) {
		if (selectionRef == null) {
			return;
		}
		ViewChannel selection = context.resolveChannel(selectionRef);
		control.addSelectionListener(keys -> selection.set(keys.size() == 1 ? keys.iterator().next() : null));
	}

	/**
	 * Offers the export of the given table in the toolbar of the enclosing element.
	 *
	 * <p>
	 * Each column is exported by the default renderer from its value.
	 * </p>
	 *
	 * @param export
	 *        The configuration of the export, <code>null</code> for a table without one.
	 */
	public static void offerExport(ViewContext context, TableViewControl<?> control, TableView<?> view,
			TableExportCommand.Config export) {
		if (export == null) {
			return;
		}
		new TableExportCommand(export).contribute(context, control, () -> view, Map::of, () -> new Object[0]);
	}

	/**
	 * Parses a point in time typed as date and time of the current user, <code>null</code> if
	 * the text is none.
	 */
	private static Date parseDateTime(String text) {
		try {
			return HTMLFormatter.getInstance().getDateTimeFormat().parse(text.trim());
		} catch (ParseException ex) {
			return null;
		}
	}

}
