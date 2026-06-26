/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.system;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.top_logic.base.administration.LoggerAdminBean;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

/**
 * Table of the files in the application log directory, one row per file with its name, size and last
 * modification time; the selected file is written to the configured {@link Config#getSelection()
 * selection channel} for a content view to tail.
 *
 * <p>
 * App-specific admin widget (referenced by {@code class=}, not a reusable {@code @TagName} element). A
 * live snapshot of the log directory is taken when the view opens.
 * </p>
 *
 * @implNote The directory comes from {@link LoggerAdminBean#getLogDir()}; only plain files are listed.
 */
public class LogFileTable implements UIElement {

	/**
	 * Configuration for {@link LogFileTable}.
	 */
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		@Override
		@ClassDefault(LogFileTable.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Channel the selected log file is written to (or {@code null} when the selection is cleared).
		 */
		@Name(SELECTION)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getSelection();
	}

	private final ChannelRef _selectionRef;

	/**
	 * Creates a new {@link LogFileTable} from configuration.
	 */
	@CalledByReflection
	public LogFileTable(InstantiationContext context, Config config) {
		_selectionRef = config.getSelection();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<Column<File, ?>> columns = new ArrayList<>();
		columns.add(textColumn("name", I18NConstants.LOG_COLUMN_NAME, File::getName, 280));
		columns.add(DefaultColumn.<File, Long> builder("size", File::length)
			.label(I18NConstants.LOG_COLUMN_SIZE)
			.renderer(size -> CellContent.text(formatSize(size)))
			.sort(() -> Comparator.<Long> naturalOrder())
			.width(120)
			.build());
		columns.add(DefaultColumn.<File, Long> builder("modified", File::lastModified)
			.label(I18NConstants.LOG_COLUMN_MODIFIED)
			.renderer(time -> CellContent.text(HTMLFormatter.getInstance().formatDateTime(new Date(time))))
			.sort(() -> Comparator.<Long> naturalOrder())
			.width(200)
			.build());

		// Files keep their identity across rebuilds, so the default identity row key is stable.
		ListRowSource<File> source = new ListRowSource<>(logFiles(), columns);
		DefaultTableView<File> view = DefaultTableView.create(columns, source);
		TableViewControl<File> control = new TableViewControl<>(context, view, false);

		ViewChannel selection = _selectionRef != null ? context.resolveChannel(_selectionRef) : null;
		if (selection != null) {
			control.setSelectionListener(keys -> {
				File file = keys.size() == 1 ? (File) keys.iterator().next() : null;
				selection.set(file);
			});
		}

		return control;
	}

	/**
	 * A live, name-sorted snapshot of the plain files in the log directory.
	 */
	private static List<File> logFiles() {
		List<File> result = new ArrayList<>();
		LoggerAdminBean bean = LoggerAdminBean.getInstance();
		File dir = bean == null ? null : bean.getLogDir();
		File[] files = dir == null ? null : dir.listFiles(File::isFile);
		if (files != null) {
			for (File file : files) {
				result.add(file);
			}
		}
		result.sort(Comparator.comparing(File::getName));
		return result;
	}

	/**
	 * Formats a byte count as a human-readable size using binary units.
	 */
	private static String formatSize(long bytes) {
		if (bytes < 1024) {
			return bytes + " B";
		}
		String[] units = {"KB", "MB", "GB", "TB"};
		double value = bytes;
		int unit = -1;
		do {
			value /= 1024;
			unit++;
		} while (value >= 1024 && unit < units.length - 1);
		return String.format("%.1f %s", value, units[unit]);
	}

	/**
	 * A sortable, text-filterable column reading one {@link String} property of a file.
	 */
	private static Column<File, String> textColumn(String id, ResKey label,
			java.util.function.Function<? super File, String> value, int width) {
		return DefaultColumn.<File, String> builder(id, value)
			.label(label)
			.renderer(CellContent::text)
			.sort(() -> Comparator.<String> naturalOrder())
			.filter(new TextColumnFilter<>(text -> text))
			.width(width)
			.build();
	}
}
