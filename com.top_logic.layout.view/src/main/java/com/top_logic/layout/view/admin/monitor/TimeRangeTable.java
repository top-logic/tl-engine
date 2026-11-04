/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.monitor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.listener.Registration;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.table.TableExportCommand;
import com.top_logic.table.Column;
import com.top_logic.table.SortSpec;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

/**
 * Read-only table of the records of a time range, e.g. the user sessions begun within it.
 *
 * <p>
 * The range is given by the {@link Config#getFrom() from} and {@link Config#getTo() to}
 * channels; the rows are read again whenever one of them or the {@link Config#getReload() reload}
 * channel changes. The selected record is written to the {@link Config#getSelection() selection}
 * channel, and an {@link Config#getExport() export} offers the table as Excel file.
 * </p>
 *
 * @param <R>
 *        The type of the records.
 */
public abstract class TimeRangeTable<R> implements UIElement {

	/**
	 * Configuration for {@link TimeRangeTable}.
	 */
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getFrom()}. */
		String FROM = "from";

		/** Configuration name for {@link #getTo()}. */
		String TO = "to";

		/** Configuration name for {@link #getReload()}. */
		String RELOAD = "reload";

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		/** Configuration name for {@link #getExport()}. */
		String EXPORT = "export";

		/**
		 * Channel holding the {@link Date} the range starts at (inclusive).
		 *
		 * <p>
		 * Without a channel, or while it holds no date, the range has no start.
		 * </p>
		 */
		@Name(FROM)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getFrom();

		/**
		 * Channel holding the {@link Date} the range ends at (exclusive).
		 *
		 * <p>
		 * Without a channel, or while it holds no date, the range ends now.
		 * </p>
		 */
		@Name(TO)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getTo();

		/**
		 * Channel whose change reads the records again, e.g. written by a refresh command.
		 */
		@Name(RELOAD)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getReload();

		/**
		 * Channel the selected record is written to, <code>null</code> while no single record is
		 * selected.
		 */
		@Name(SELECTION)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getSelection();

		/**
		 * The export of the table, offered in the toolbar of the enclosing element.
		 */
		@Name(EXPORT)
		TableExportCommand.Config getExport();
	}

	private final Config _config;

	/**
	 * Creates a {@link TimeRangeTable} from configuration.
	 */
	protected TimeRangeTable(Config config) {
		_config = config;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ViewChannel from = channel(context, _config.getFrom());
		ViewChannel to = channel(context, _config.getTo());
		ViewChannel reload = channel(context, _config.getReload());

		List<Column<R, ?>> columns = columns();
		ListRowSource<R> source = new ListRowSource<>(read(from, to), columns);
		DefaultTableView<R> view = DefaultTableView.create(columns, source, null, null, defaultSort());
		TableViewControl<R> control = new TableViewControl<>(context, view, false);

		for (ViewChannel channel : new ViewChannel[] { from, to, reload }) {
			if (channel == null) {
				continue;
			}
			Registration registration = channel.addListener((sender, oldValue, newValue) -> {
				source.setElements(read(from, to));
				control.refreshData();
			});
			control.addCleanupAction(registration::dispose);
		}

		MonitorTables.bindSelection(context, control, _config.getSelection());
		MonitorTables.offerExport(context, control, view, _config.getExport());
		control.setCssClass(_config.getCssClass());
		return control;
	}

	private List<R> read(ViewChannel from, ViewChannel to) {
		Date start = date(from);
		Date end = date(to);
		return new ArrayList<>(records(start == null ? new Date(0) : start, end == null ? new Date() : end));
	}

	private static ViewChannel channel(ViewContext context, ChannelRef ref) {
		return ref == null ? null : context.resolveChannel(ref);
	}

	private static Date date(ViewChannel channel) {
		return channel != null && channel.get() instanceof Date date ? date : null;
	}

	/**
	 * The columns of the table, in their initial order.
	 */
	protected abstract List<Column<R, ?>> columns();

	/**
	 * The order of the rows until the user sorts the table.
	 */
	protected abstract SortSpec defaultSort();

	/**
	 * The records of the given range.
	 *
	 * @param start
	 *        The start of the range (inclusive).
	 * @param end
	 *        The end of the range.
	 */
	protected abstract List<? extends R> records(Date start, Date end);

}
