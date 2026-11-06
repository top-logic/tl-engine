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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.base.administration.LoggerAdminBean;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.listener.Registration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.table.CellControlFactory;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.field.FieldControlRegistry;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.admin.monitor.MonitorTables;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.table.TableExportCommand;
import com.top_logic.monitoring.log.LogFile;
import com.top_logic.monitoring.log.LogFileCollector;
import com.top_logic.monitoring.log.LogLine;
import com.top_logic.monitoring.log.LogLineSeverity;
import com.top_logic.monitoring.log.LogParser;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.Option;
import com.top_logic.table.SortColumn;
import com.top_logic.table.SortSpec;
import com.top_logic.table.filter.OptionsColumnFilter;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.util.Resources;

/**
 * Sortable, per-column filterable table of parsed log entries: those of the file held by the
 * configured {@link Config#getInput() input channel}, or, while it holds no file, those of all log
 * files of the application. Optionally only the entries of one session are shown, the ones carrying
 * the mark held by the {@link Config#getSessionMark() session mark channel}. The entries are read
 * again whenever one of the channels or the {@link Config#getReload() reload channel} changes.
 *
 * <p>
 * App-specific admin widget (referenced by {@code class=}, not a reusable {@code @TagName} element).
 * Entries are produced by the reusable {@link LogParser} (one row per log entry, multi-line stack traces
 * collapsed into the entry details). Of a single file only the last {@link #TAIL_BYTES} bytes are
 * parsed so that large files stay cheap to view; all log files are read by the {@link LogFileCollector}.
 * Until the user sorts the table, the rows are ordered by severity, the strongest first, and then by
 * time, the newest first ({@link Config#getSeverityFirst()}), or by time only.
 * </p>
 *
 * <p>
 * The details of an entry, usually a stack trace of many lines, are shown in their cell by their
 * first line and a button opening the full details read-only in a dialog, preformatted: in a font of
 * fixed width, with the line breaks and the indentation of the trace and without wrapping its
 * lines, with a button copying them. Sorting, filtering and searching the column use the full
 * details text.
 * </p>
 *
 * <p>
 * The selected entry is written to the {@link Config#getSelection() selection channel}, e.g. for
 * {@link LogFunctions#dump(LogLine)}, and an {@link Config#getExport() export} offers the table as
 * Excel file.
 * </p>
 *
 * @see FieldSpec#isCompact()
 * @see FieldControlRegistry#createControl(ReactContext, FieldSpec,
 *      com.top_logic.layout.form.model.FieldModel,
 *      com.top_logic.layout.react.field.ReactFieldControlProvider)
 */
public class LogLineTable implements UIElement {

	/** Maximum number of trailing bytes parsed from a log file. */
	private static final long TAIL_BYTES = 4 * 1024 * 1024;

	/** Number of rows of the text area displaying the details of an entry in the dialog. */
	private static final int DETAILS_ROWS = 20;

	private static final LogParser PARSER = TypedConfigUtil.createInstance(LogParser.Config.class);

	private static final LogFileCollector COLLECTOR = TypedConfigUtil.createInstance(LogFileCollector.Config.class);

	/** Name of the column of the time of an entry. */
	public static final String TIME = "time";

	/** Name of the column of the severity of an entry. */
	public static final String SEVERITY = "severity";

	/** Name of the column of the kind of log file an entry was read from. */
	public static final String FILE_CATEGORY = "file-category";

	/**
	 * Configuration for {@link LogLineTable}.
	 */
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		/** Configuration name for {@link #getReload()}. */
		String RELOAD = "reload";

		/** Configuration name for {@link #getSessionMark()}. */
		String SESSION_MARK = "session-mark";

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		/** Configuration name for {@link #getSeverityFirst()}. */
		String SEVERITY_FIRST = "severity-first";

		/** Configuration name for {@link #getExport()}. */
		String EXPORT = "export";

		@Override
		@ClassDefault(LogLineTable.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Channel holding the selected log {@link File} whose entries are shown.
		 *
		 * <p>
		 * Without a channel, or while it holds no file, the entries of all log files are shown.
		 * </p>
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

		/**
		 * Channel holding the mark of the session whose entries are shown, see
		 * {@link LogFunctions#currentSessionMark()} and
		 * {@link LogFunctions#sessionMark(com.top_logic.knowledge.monitor.UserSession)}.
		 *
		 * <p>
		 * Without a channel, the entries of all sessions are shown; while the channel holds no
		 * mark, none.
		 * </p>
		 */
		@Name(SESSION_MARK)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getSessionMark();

		/**
		 * Channel the selected {@link LogLine} is written to, <code>null</code> while no single
		 * entry is selected.
		 */
		@Name(SELECTION)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getSelection();

		/**
		 * Whether the rows are ordered by severity, the strongest first, before they are ordered by
		 * time, until the user sorts the table.
		 *
		 * <p>
		 * Otherwise they are ordered by time only, the newest first.
		 * </p>
		 */
		@Name(SEVERITY_FIRST)
		@BooleanDefault(true)
		boolean getSeverityFirst();

		/**
		 * The export of the table, offered in the toolbar of the enclosing element.
		 */
		@Name(EXPORT)
		TableExportCommand.Config getExport();
	}

	private final ChannelRef _inputRef;

	private final ChannelRef _reloadRef;

	private final ChannelRef _sessionMarkRef;

	private final Config _config;

	private final String _cssClass;

	/**
	 * Creates a new {@link LogLineTable} from configuration.
	 */
	@CalledByReflection
	public LogLineTable(InstantiationContext context, Config config) {
		_inputRef = config.getInput();
		_reloadRef = config.getReload();
		_sessionMarkRef = config.getSessionMark();
		_config = config;
		_cssClass = config.getCssClass();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<Column<LogLine, ?>> columns = new ArrayList<>();
		columns.add(MonitorTables.dateColumn(TIME, I18NConstants.LOG_COLUMN_TIME, LogLine::getTime, 160));
		columns.add(DefaultColumn.<LogLine, LogLineSeverity> builder(SEVERITY, LogLine::getSeverity)
			.label(I18NConstants.LOG_COLUMN_SEVERITY)
			.renderer(severity -> CellContent.text(severity == null ? "" : severity.getName()))
			.sort(() -> Comparator.nullsFirst(Comparator.comparingInt(LogLineSeverity::getSortOrder)))
			.filter(new OptionsColumnFilter<>(severityOptions()))
			.width(90)
			.build());
		columns.add(MonitorTables.textColumn(FILE_CATEGORY, I18NConstants.LOG_COLUMN_FILE_CATEGORY,
			LogLine::getFileCategory, 150));
		columns.add(MonitorTables.textColumn("file-name", I18NConstants.LOG_COLUMN_FILE_NAME,
			LogLine::getFileName, 200));
		columns.add(MonitorTables.textColumn("category", I18NConstants.LOG_COLUMN_CATEGORY, LogLine::getCategory, 300));
		columns.add(MonitorTables.textColumn("thread", I18NConstants.LOG_COLUMN_THREAD, LogLine::getThread, 180));
		columns.add(MonitorTables.textColumn("message", I18NConstants.LOG_COLUMN_MESSAGE, LogLine::getMessage, 600));
		columns.add(DefaultColumn.<LogLine, String> builder("details", LogLine::getDetails)
			.label(I18NConstants.LOG_COLUMN_DETAILS)
			.renderer(LogLineTable::detailsCell)
			.searchText(text -> text == null ? "" : text)
			.sort(() -> Comparator.nullsFirst(Comparator.naturalOrder()))
			.filter(new TextColumnFilter<>(text -> text == null ? "" : text))
			.width(300)
			.build());

		ViewChannel input = _inputRef != null ? context.resolveChannel(_inputRef) : null;
		ViewChannel sessionMark = _sessionMarkRef != null ? context.resolveChannel(_sessionMarkRef) : null;
		ViewChannel reload = _reloadRef != null ? context.resolveChannel(_reloadRef) : null;

		ListRowSource<LogLine> source = new ListRowSource<>(lines(input, sessionMark), columns);
		DefaultTableView<LogLine> view =
			DefaultTableView.create(columns, source, null, null, defaultSort(), Set.of(FILE_CATEGORY));
		TableViewControl<LogLine> control = new TableViewControl<>(context, view, false);

		for (ViewChannel channel : new ViewChannel[] { input, sessionMark, reload }) {
			if (channel == null) {
				continue;
			}
			ChannelListener listener = (sender, oldValue, newValue) -> {
				source.setElements(lines(input, sessionMark));
				control.refreshData();
			};
			Registration registration = channel.addListener(listener);
			control.addCleanupAction(registration::dispose);
		}

		MonitorTables.bindSelection(context, control, _config.getSelection());
		MonitorTables.offerExport(context, control, view, _config.getExport());
		control.setCssClass(_cssClass);
		return control;
	}

	private SortSpec defaultSort() {
		SortColumn newestFirst = new SortColumn(TIME, false);
		if (_config.getSeverityFirst()) {
			return new SortSpec(List.of(new SortColumn(SEVERITY, false), newestFirst));
		}
		return new SortSpec(List.of(newestFirst));
	}

	/**
	 * The parsed entries to show: those of the file in the given input channel, or of all log
	 * files without one, restricted to the session of the mark in the given channel, if any. An
	 * empty list when a session channel holds no mark or the files cannot be read.
	 */
	private static List<LogLine> lines(ViewChannel input, ViewChannel sessionMark) {
		String mark = null;
		if (sessionMark != null) {
			mark = sessionMark.get() instanceof String text ? text : null;
			if (StringServices.isEmpty(mark)) {
				return new ArrayList<>();
			}
		}
		Object value = input == null ? null : input.get();
		List<LogLine> lines = value instanceof File file ? fileLines(file) : allLines();
		if (mark != null) {
			String sessionMarker = mark;
			lines.removeIf(line -> line.getMessage() == null || !line.getMessage().contains(sessionMarker));
		}
		return lines;
	}

	/**
	 * The parsed entries of the given file, an empty list if it cannot be read.
	 */
	private static List<LogLine> fileLines(File file) {
		try {
			return new ArrayList<>(PARSER.parseLog(new LogFile(file.getName(), tail(file))));
		} catch (IOException | RuntimeException ex) {
			Logger.warn("Failed to read log file '" + file.getName() + "'.", ex, LogLineTable.class);
			return new ArrayList<>();
		}
	}

	/**
	 * The parsed entries of all log files of the application, an empty list if they cannot be
	 * read.
	 */
	private static List<LogLine> allLines() {
		LoggerAdminBean bean = LoggerAdminBean.getInstance();
		File dir = bean == null ? null : bean.getLogDir();
		if (dir == null) {
			return new ArrayList<>();
		}
		try {
			return COLLECTOR.collect(dir.toPath()).parallelStream()
				.map(PARSER::parseLog)
				.flatMap(List::stream)
				.collect(Collectors.toCollection(ArrayList::new));
		} catch (RuntimeException ex) {
			Logger.warn("Failed to read the log files in '" + dir + "'.", ex, LogLineTable.class);
			return new ArrayList<>();
		}
	}

	/**
	 * The cell displaying the details of an entry: a read-only preformatted text field of several
	 * rows, shown in the cell by its first line and a button opening the full text in a dialog.
	 */
	private static CellContent detailsCell(String details) {
		if (details == null || details.isEmpty()) {
			return CellContent.text(details);
		}
		return new CellContent.Raw((CellControlFactory) context -> detailsControl(context, details));
	}

	/**
	 * The read-only control displaying the given details of an entry in a table cell.
	 */
	private static ReactControl detailsControl(ReactContext context, String details) {
		String label = Resources.getInstance().getString(I18NConstants.LOG_COLUMN_DETAILS);
		FieldSpec field = FieldControlService.cellSpec(
			FieldSpec.of(String.class, label).setMultilineRows(DETAILS_ROWS).setPreformatted(true));
		AbstractFieldModel model = new AbstractFieldModel(details);
		model.setEditable(false);
		return FieldControlRegistry.getInstance().createControl(context, field, model, FieldControlRegistry.TEXT);
	}

	/**
	 * The severity filter options, strongest severity first.
	 */
	private static List<Option> severityOptions() {
		List<LogLineSeverity> severities = new ArrayList<>(LogLineSeverity.STANDARD_SEVERITIES.values());
		severities.sort(Comparator.comparingInt(LogLineSeverity::getSortOrder).reversed());
		List<Option> options = new ArrayList<>();
		for (LogLineSeverity severity : severities) {
			options.add(new Option(severity, ResKey.text(severity.getName())));
		}
		return options;
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
}
