/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DefaultValueProviderShared;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.ComplexDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.impl.NonNegative;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.common.ReactJobStatusControl;
import com.top_logic.layout.react.control.layout.ReactInsetControl;
import com.top_logic.layout.react.control.overlay.DialogHandle;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.react.control.overlay.ReactWindowControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.table.export.ExcelExportHandler;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.layout.view.command.CommandCliques;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.element.JobStatusElement;
import com.top_logic.layout.view.job.JobMonitor;
import com.top_logic.layout.view.job.JobRunner;
import com.top_logic.layout.view.job.JobStatus;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.table.TableView;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.export.ExcelCellRenderer;
import com.top_logic.tool.export.tableview.ExportMonitor;
import com.top_logic.tool.export.tableview.TableViewExcelExport;
import com.top_logic.tool.export.tableview.TableViewExcelExport.ExportFile;
import com.top_logic.tool.export.tableview.TableViewExcelExport.Snapshot;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * The command exporting a &lt;table&gt; to Excel, configured as the table's
 * {@link com.top_logic.layout.view.element.TableElement.Config#getExport() export}.
 *
 * <p>
 * The export holds what the user sees: the displayed columns in their order - without the ones that
 * hold an action rather than data, and without those declared {@code export="false"} - and the
 * displayed rows, filtered, searched and sorted as the table is. A grouped table is written with its
 * groups as an outline of the sheet. Every cell is written from the typed value of its column, by
 * the column's {@link ColumnExportConfig#getExportRenderer() export renderer}.
 * </p>
 *
 * <p>
 * The command is offered in the toolbar of the element the table is displayed in, in the
 * {@link CommandCliques#EXPORT export} group. A table of up to
 * {@link Config#getProgressThreshold() progress-threshold} rows is exported right away; a larger
 * one in the background, with a dialog showing the progress and offering to cancel. The browser then
 * saves the file.
 * </p>
 */
public class TableExportCommand implements ViewCommand {

	/**
	 * Configuration of the {@link TableExportCommand}.
	 */
	@DisplayOrder({
		Config.LABEL,
		Config.IMAGE,
		Config.CLIQUE,
		Config.DOWNLOAD_NAME,
		Config.SHEET_NAME,
		Config.TEMPLATE,
		Config.AUTOFIT_COLUMNS,
		Config.FREEZE_HEADER,
		Config.AUTO_FILTER,
		Config.STREAMING,
		Config.PROGRESS_THRESHOLD,
	})
	public interface Config extends ViewCommand.Config {

		/** @see #getDownloadName() */
		String DOWNLOAD_NAME = "download-name";

		/** @see #getSheetName() */
		String SHEET_NAME = "sheet-name";

		/** @see #getTemplate() */
		String TEMPLATE = "template";

		/** @see #getAutofitColumns() */
		String AUTOFIT_COLUMNS = "autofit-columns";

		/** @see #getFreezeHeader() */
		String FREEZE_HEADER = "freeze-header";

		/** @see #getAutoFilter() */
		String AUTO_FILTER = "auto-filter";

		/** @see #getStreaming() */
		String STREAMING = "streaming";

		/** @see #getProgressThreshold() */
		String PROGRESS_THRESHOLD = "progress-threshold";

		@Override
		@ClassDefault(TableExportCommand.class)
		Class<? extends TableExportCommand> getImplementationClass();

		@Override
		@ComplexDefault(DefaultLabel.class)
		ResKey getLabel();

		@Override
		@ComplexDefault(DefaultTooltip.class)
		ResKey getTooltip();

		@Override
		@FormattedDefault("css:bi bi-file-earmark-excel")
		ThemeImage getImage();

		@Override
		@StringDefault(CommandCliques.EXPORT)
		String getClique();

		/**
		 * TL-Script function computing the name the exported file is saved under.
		 *
		 * <p>
		 * Called with the values of the table's {@code inputs}, in declaration order - the same
		 * values its rows are computed from. The result is a text or an internationalized text; a
		 * name without extension gets the one of the Excel format. Without a function, or for an
		 * empty result, the name consists of the date, "Export data" and the name of the
		 * application.
		 * </p>
		 */
		@Name(DOWNLOAD_NAME)
		Expr getDownloadName();

		/**
		 * The name of the sheet the table is written to.
		 *
		 * <p>
		 * Without a name, a new workbook names it "Data", and a {@link #getTemplate() template} is
		 * filled in its first sheet.
		 * </p>
		 */
		@Name(SHEET_NAME)
		@Nullable
		ResKey getSheetName();

		/**
		 * The Excel workbook the table is written into, instead of an empty one.
		 *
		 * <p>
		 * The name of a file in the {@code WEB-INF/reportTemplates/excel} folder of the application,
		 * whose styles, further sheets and formulas the export keeps. The table is written into the
		 * sheet of the {@link #getSheetName() sheet name}, or into the first sheet, from its first
		 * row on. A template is never written {@link #getStreaming() streaming}.
		 * </p>
		 */
		@Name(TEMPLATE)
		@Nullable
		@Options(fun = ExcelExportHandler.Config.ExportTemplates.class)
		String getTemplate();

		/**
		 * Whether the column widths are fitted to the exported values.
		 *
		 * <p>
		 * Otherwise every column gets the width it is displayed with in the table.
		 * </p>
		 */
		@Name(AUTOFIT_COLUMNS)
		@BooleanDefault(true)
		boolean getAutofitColumns();

		/**
		 * Whether the header row - and the columns the table keeps fixed - stay in place while
		 * scrolling through the sheet.
		 */
		@Name(FREEZE_HEADER)
		@BooleanDefault(true)
		boolean getFreezeHeader();

		/**
		 * Whether the header row offers Excel's filter for every column.
		 */
		@Name(AUTO_FILTER)
		@BooleanDefault(true)
		boolean getAutoFilter();

		/**
		 * Whether the file is written streaming.
		 *
		 * <p>
		 * Streaming keeps only a few rows in memory, so a table of any size can be exported, but it
		 * does not support formatting within a cell (structured text). A {@link #getTemplate()
		 * template} is always written without streaming.
		 * </p>
		 */
		@Name(STREAMING)
		@BooleanDefault(true)
		boolean getStreaming();

		/**
		 * The number of rows from which on the export runs in the background, with a dialog
		 * showing its progress and offering to cancel it.
		 *
		 * <p>
		 * A smaller table is exported right away. {@code 0} shows the dialog for every export.
		 * </p>
		 */
		@Name(PROGRESS_THRESHOLD)
		@IntDefault(1000)
		@Constraint(NonNegative.class)
		int getProgressThreshold();

		/**
		 * @see #getProgressThreshold()
		 */
		void setProgressThreshold(int value);

		/**
		 * Default of {@link Config#getTooltip()}: what the export holds, rather than the label once
		 * more.
		 */
		class DefaultTooltip extends DefaultValueProviderShared {
			@Override
			public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
				return com.top_logic.layout.view.I18NConstants.TABLE_EXPORT_EXCEL.tooltip();
			}
		}

		/**
		 * Default of {@link Config#getLabel()}.
		 */
		class DefaultLabel extends DefaultValueProviderShared {
			@Override
			public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
				return com.top_logic.layout.view.I18NConstants.TABLE_EXPORT_EXCEL;
			}
		}
	}

	/** Shortest time in milliseconds between two progress reports reaching the browser. */
	private static final long PROGRESS_INTERVAL = 200;

	/** Width of the progress dialog. */
	private static final DisplayDimension DIALOG_WIDTH = DisplayDimension.px(450);

	private final Config _config;

	private final QueryExecutor _downloadName;

	/**
	 * Reports a configuration that uses the export as a command of its own.
	 *
	 * <p>
	 * The export exports the table it belongs to, so it is configured as the
	 * {@link com.top_logic.layout.view.element.TableElement.Config#getExport() export} of a
	 * &lt;table&gt;, which creates it through {@link #TableExportCommand(Config)}. Configured
	 * anywhere else - among the commands of a panel, say - it has no table to export, which is
	 * reported when the configuration is read rather than when the button is pressed.
	 * </p>
	 */
	@CalledByReflection
	public TableExportCommand(InstantiationContext context, Config config) {
		this(config);
		context.error("The table export can only be configured as <"
			+ com.top_logic.layout.view.element.TableElement.Config.EXPORT
			+ "> of a <table>, not as a command of its own.");
	}

	/**
	 * Creates the export of a table from its configuration.
	 */
	public TableExportCommand(Config config) {
		_config = config;
		Expr downloadName = config.getDownloadName();
		_downloadName = downloadName == null ? null : QueryExecutor.compile(downloadName);
	}

	/**
	 * The configuration of this command.
	 */
	public Config getConfig() {
		return _config;
	}

	/**
	 * Not executable on its own: the command exports the table it is {@link #bind(Supplier, Supplier,
	 * Supplier) bound} to. A configuration placing it elsewhere is refused when it is read, see
	 * {@link #TableExportCommand(InstantiationContext, Config)}.
	 */
	@Override
	public HandlerResult execute(ReactContext context, Object input) {
		throw new IllegalStateException("The table export runs bound to its table.");
	}

	/**
	 * The command exporting the given table.
	 *
	 * @param view
	 *        The table to export, as it is displayed when the command runs.
	 * @param renderers
	 *        The export renderers of the table's columns as displayed when the command runs, by
	 *        column name; a column without one is written by the default renderer.
	 * @param inputs
	 *        The current values of the table's input channels, the arguments of the
	 *        {@link Config#getDownloadName() download name} function.
	 */
	public ViewCommand bind(Supplier<? extends TableView<?>> view,
			Supplier<? extends Map<String, ExcelCellRenderer>> renderers, Supplier<Object[]> inputs) {
		return (context, input) -> export(context, view.get(), renderers.get(), inputs.get());
	}

	/**
	 * Exports the given table and hands the file to the user.
	 */
	HandlerResult export(ReactContext context, TableView<?> view, Map<String, ExcelCellRenderer> renderers,
			Object[] inputs) {
		TableViewExcelExport export = createExport(renderers);
		Snapshot snapshot = export.snapshot(view);
		String downloadName = downloadName(inputs);

		DialogManager dialogs = context.getDialogManager();
		if (dialogs == null || snapshot.rowCount() < _config.getProgressThreshold()) {
			deliver(context, write(export, snapshot, downloadName, ExportMonitor.NONE));
		} else {
			exportInBackground(context, dialogs, export, snapshot, downloadName);
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	private TableViewExcelExport createExport(Map<String, ExcelCellRenderer> renderers) {
		ResKey sheetName = _config.getSheetName();
		String template = _config.getTemplate();
		TableViewExcelExport export = new TableViewExcelExport(renderers::get)
			.setSheetName(sheetName == null ? null : Resources.getInstance().getString(sheetName))
			.setStreaming(_config.getStreaming())
			.setAutoFit(_config.getAutofitColumns())
			.setFreezeHeader(_config.getFreezeHeader())
			.setAutoFilter(_config.getAutoFilter());
		if (!StringServices.isEmpty(template)) {
			export.setTemplate(() -> openTemplate(template));
		}
		return export;
	}

	private static InputStream openTemplate(String template) throws IOException {
		try {
			return new DataAccessProxy("webinf://reportTemplates", "excel").getChildProxy(template).getEntry();
		} catch (Exception ex) {
			throw new IOException("Excel template '" + template + "' not found.", ex);
		}
	}

	/**
	 * The name the file is saved under: what the configured function computes, or the date, the
	 * default title and the application name - the name the export of a classic table gets.
	 */
	private String downloadName(Object[] inputs) {
		String name = null;
		if (_downloadName != null) {
			Object computed = _downloadName.execute(inputs);
			if (computed instanceof ResKey key) {
				name = Resources.getInstance().getString(key);
			} else if (computed != null) {
				name = computed.toString();
			}
		}
		if (StringServices.isEmpty(name)) {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", TLContext.getLocale());
			format.setTimeZone(TLContext.getTimeZone());
			name = Resources.getInstance().getString(
				com.top_logic.layout.table.export.I18NConstants.COMPONENT_DOWNLOAD_NAME__DATE_TITLE_APP.fill(
					format.format(new Date()),
					com.top_logic.layout.table.export.I18NConstants.DEFAULT_EXPORT_NAME,
					com.top_logic.layout.I18NConstants.APPLICATION_TITLE));
		}
		// The extension is the one of the format written, see TableViewExcelExport#write().
		return name;
	}

	private static ExportFile write(TableViewExcelExport export, Snapshot snapshot, String name,
			ExportMonitor monitor) {
		try {
			return export.write(snapshot, name, monitor);
		} catch (IOException ex) {
			throw new TopLogicException(com.top_logic.layout.table.export.I18NConstants.ERROR_CREATING_EXPORT, ex);
		}
	}

	/**
	 * Hands the written file to the browser; its temporary file is deleted once it is sent or given
	 * up.
	 */
	private static void deliver(ReactContext context, ExportFile file) {
		SSEUpdateQueue queue = context.getSSEQueue();
		if (queue == null) {
			Logger.info("No window to deliver the export '" + file.data().getName() + "' to.",
				TableExportCommand.class);
			file.discard();
			return;
		}
		queue.deliverDownload(file.data(), file::discard);
	}

	/**
	 * Writes the file on a worker thread, while a dialog shows the progress and offers to cancel;
	 * the dialog closes and the browser saves the file once it is written.
	 */
	private static void exportInBackground(ReactContext context, DialogManager dialogs, TableViewExcelExport export,
			Snapshot snapshot, String downloadName) {
		ViewChannel progress = new DefaultViewChannel("export");
		ReactJobStatusControl status = new ReactJobStatusControl(context, null);
		ChannelListener listener = (sender, oldValue, newValue) -> status.setJob(JobStatusElement.display(newValue));
		progress.addListener(listener);
		status.addCleanupAction(() -> progress.removeListener(listener));

		DialogHandle[] dialog = new DialogHandle[1];
		// The file the job wrote, also when the job ends as cancelled after writing it, so that its
		// temporary file is deleted in any case.
		AtomicReference<ExportFile> written = new AtomicReference<>();
		JobRunner runner = new JobRunner(context, progress, List.of(), true, PROGRESS_INTERVAL, state -> {
			if (state.status() == JobStatus.COMPLETED) {
				dialog[0].close(DialogResult.ok(null));
				deliver(context, written.get());
				return;
			}
			ExportFile abandoned = written.getAndSet(null);
			if (abandoned != null) {
				abandoned.discard();
			}
			if (state.status() == JobStatus.CANCELLED) {
				dialog[0].close(DialogResult.cancelled());
			}
			// A failed export keeps the dialog open: it shows what went wrong.
		});

		String title = Resources.getInstance().getString(
			com.top_logic.layout.table.export.I18NConstants.PERFORMING_EXPORT);
		// Closing the window only closes the dialog; giving up the export is the business of the
		// dialog's result handler, which every way of leaving the dialog passes through.
		ReactWindowControl window = new ReactWindowControl(context, title, DIALOG_WIDTH,
			() -> dialog[0].close(DialogResult.cancelled()));
		window.setResizable(false);
		window.setChild(new ReactInsetControl(context, status));
		dialog[0] = dialogs.openDialog(false, window, result -> runner.cancel());

		runner.start((job, arguments) -> {
			ExportFile file = write(export, snapshot, downloadName, monitor(job));
			written.set(file);
			return file.data();
		}, List.of());
	}

	/**
	 * The {@link ExportMonitor} reporting to the given job.
	 */
	private static ExportMonitor monitor(JobMonitor job) {
		return new ExportMonitor() {
			@Override
			public void progress(int done, int total) {
				job.progress(done, total);
				job.message(com.top_logic.layout.table.export.I18NConstants.EXPORTING_ROW__NUM_TOTAL.fill(done, total));
			}

			@Override
			public void checkCancelled() {
				job.checkCancelled();
			}
		};
	}

}
