/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.system;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.monitoring.log.LogFile;
import com.top_logic.monitoring.log.LogLine;
import com.top_logic.monitoring.log.LogLineSeverity;
import com.top_logic.monitoring.log.LogParser;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

/**
 * Sortable, per-column filterable table of the parsed log entries of the file held by the configured
 * {@link Config#getInput() input channel}, re-parsing whenever the input or the {@link Config#getReload()
 * reload channel} changes.
 *
 * <p>
 * App-specific admin widget (referenced by {@code class=}, not a reusable {@code @TagName} element).
 * Entries are produced by the reusable {@link LogParser} (one row per log entry, multi-line stack traces
 * collapsed into the entry details); only the last {@link #TAIL_BYTES} bytes are parsed so that large
 * files stay cheap to view. Rows are ordered newest first.
 * </p>
 */
public class LogLineTable implements UIElement {

	/** Maximum number of trailing bytes parsed from a log file. */
	private static final long TAIL_BYTES = 4 * 1024 * 1024;

	private static final LogParser PARSER = TypedConfigUtil.createInstance(LogParser.Config.class);

	/**
	 * Configuration for {@link LogLineTable}.
	 */
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		/** Configuration name for {@link #getReload()}. */
		String RELOAD = "reload";

		@Override
		@ClassDefault(LogLineTable.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Channel holding the selected log {@link File} whose entries are shown.
		 */
		@Name(INPUT)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();

		/**
		 * Channel whose change (a refresh tick) re-parses the current file without changing the selection.
		 */
		@Name(RELOAD)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getReload();
	}

	private final ChannelRef _inputRef;

	private final ChannelRef _reloadRef;

	/**
	 * Creates a new {@link LogLineTable} from configuration.
	 */
	@CalledByReflection
	public LogLineTable(InstantiationContext context, Config config) {
		_inputRef = config.getInput();
		_reloadRef = config.getReload();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<Column<LogLine, ?>> columns = new ArrayList<>();
		columns.add(DefaultColumn.<LogLine, Date> builder("time", LogLine::getTime)
			.label(I18NConstants.LOG_COLUMN_TIME)
			.renderer(time -> CellContent.text(time == null ? "" : HTMLFormatter.getInstance().formatDateTime(time)))
			.sort(() -> Comparator.nullsFirst(Comparator.naturalOrder()))
			.width(160)
			.build());
		columns.add(DefaultColumn.<LogLine, LogLineSeverity> builder("severity", LogLine::getSeverity)
			.label(I18NConstants.LOG_COLUMN_SEVERITY)
			.renderer(severity -> CellContent.text(severity == null ? "" : severity.getName()))
			.sort(() -> Comparator.nullsFirst(Comparator.comparingInt(LogLineSeverity::getSortOrder)))
			.filter(new TextColumnFilter<>(severity -> severity == null ? "" : severity.getName()))
			.width(90)
			.build());
		columns.add(textColumn("category", I18NConstants.LOG_COLUMN_CATEGORY, LogLine::getCategory, 300));
		columns.add(textColumn("thread", I18NConstants.LOG_COLUMN_THREAD, LogLine::getThread, 180));
		columns.add(textColumn("message", I18NConstants.LOG_COLUMN_MESSAGE, LogLine::getMessage, 600));
		columns.add(textColumn("details", I18NConstants.LOG_COLUMN_DETAILS, LogLine::getDetails, 300));

		ViewChannel input = _inputRef != null ? context.resolveChannel(_inputRef) : null;
		ListRowSource<LogLine> source = new ListRowSource<>(lines(input == null ? null : input.get()), columns);
		DefaultTableView<LogLine> view = DefaultTableView.create(columns, source);
		TableViewControl<LogLine> control = new TableViewControl<>(context, view, false);

		if (input != null) {
			ChannelListener listener = (sender, oldValue, newValue) -> {
				source.setElements(lines(newValue));
				control.refreshData();
			};
			input.addListener(listener);
			control.addCleanupAction(() -> input.removeListener(listener));
		}
		if (_reloadRef != null) {
			ViewChannel reload = context.resolveChannel(_reloadRef);
			ChannelListener listener = (sender, oldValue, newValue) -> {
				source.setElements(lines(input == null ? null : input.get()));
				control.refreshData();
			};
			reload.addListener(listener);
			control.addCleanupAction(() -> reload.removeListener(listener));
		}
		return control;
	}

	/**
	 * The parsed entries of the given file (newest first), or an empty list when nothing is selected or
	 * the file cannot be read.
	 */
	private static List<LogLine> lines(Object value) {
		if (!(value instanceof File file)) {
			return new ArrayList<>();
		}
		try {
			List<LogLine> lines = PARSER.parseLog(new LogFile(file.getName(), tail(file)));
			lines.sort(Comparator.comparing(LogLine::getTime, Comparator.nullsFirst(Comparator.naturalOrder()))
				.reversed());
			return lines;
		} catch (IOException | RuntimeException ex) {
			Logger.warn("Failed to read log file '" + file.getName() + "'.", ex, LogLineTable.class);
			return new ArrayList<>();
		}
	}

	/**
	 * Reads up to the last {@link #TAIL_BYTES} bytes of the file, dropping a leading partial entry.
	 */
	private static String tail(File file) throws IOException {
		long length = file.length();
		long start = Math.max(0, length - TAIL_BYTES);
		try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
			raf.seek(start);
			byte[] buffer = new byte[(int) (length - start)];
			raf.readFully(buffer);
			String text = new String(buffer, StandardCharsets.UTF_8);
			if (start > 0) {
				int newline = text.indexOf('\n');
				text = newline >= 0 ? text.substring(newline + 1) : text;
			}
			return text;
		}
	}

	/**
	 * A sortable, text-filterable column reading one {@link String} property of a log entry.
	 */
	private static Column<LogLine, String> textColumn(String id, ResKey label,
			Function<? super LogLine, String> value, int width) {
		return DefaultColumn.<LogLine, String> builder(id, value)
			.label(label)
			.renderer(CellContent::text)
			.sort(() -> Comparator.nullsFirst(Comparator.naturalOrder()))
			.filter(new TextColumnFilter<>(text -> text == null ? "" : text))
			.width(width)
			.build();
	}
}
