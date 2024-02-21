/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.export;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import com.top_logic.base.office.POIUtil;
import com.top_logic.base.office.excel.ExcelValue;
import com.top_logic.base.office.excel.streaming.ExcelWriter;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.col.MutableInteger;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DefaultValueProvider;
import com.top_logic.basic.config.annotation.DefaultValueProviderShared;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ComplexDefault;
import com.top_logic.basic.config.order.DisplayInherited;
import com.top_logic.basic.config.order.DisplayInherited.DisplayStrategy;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.i18n.log.I18NLog;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.shared.collection.factory.CollectionFactoryShared;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.basic.SimpleConstantControl;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.AbstractFormMemberControl;
import com.top_logic.layout.form.control.BlockControl;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.model.Column;
import com.top_logic.layout.table.renderer.ColumnLabelProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.export.ExcelCellRenderer;
import com.top_logic.tool.export.ExcelExportSupport;
import com.top_logic.tool.export.RowContext;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link CommandHandler} exporting {@link TableData} to Excel format.
 */
@InApp(classifiers = { "treegrid", "grid", "table", "treetable" })
public class StreamingExcelExportHandler extends AbstractTableExportHandler {

	/**
	 * Configuration options for {@link StreamingExcelExportHandler}.
	 */
	@DisplayInherited(DisplayStrategy.IGNORE)
	@DisplayOrder({
		Config.RESOURCE_KEY_PROPERTY_NAME,
		Config.IMAGE_PROPERTY,
		Config.DISABLED_IMAGE_PROPERTY,
		Config.CLIQUE_PROPERTY,
		Config.GROUP_PROPERTY,
		Config.EXECUTABILITY_PROPERTY,
		Config.CONFIRM_PROPERTY,
		Config.CONFIRM_MESSAGE,
		Config.EXPORT_NAME_KEY,
		Config.EXPORT_SHEET_KEY,
	})
	public interface Config extends AbstractTableExportHandler.Config {
		/** @see #getExportNameKey() */
		String EXPORT_NAME_KEY = "exportNameKey";

		/** I18N key for the export sheet name. */
		String EXPORT_SHEET_KEY = "exportSheetKey";

		/**
		 * Name of the Excel sheet that is filled with data.
		 */
		@Label("Sheet name")
		@Name(EXPORT_SHEET_KEY)
		@ComplexDefault(ExportSheetKeyDefault.class)
		@InstanceFormat
		ResKey getExportSheetKey();

		/**
		 * File name of the created download.
		 */
		@Label("Download name")
		@Name(Config.EXPORT_NAME_KEY)
		@ComplexDefault(ExportNameKeyDefault.class)
		@InstanceFormat
		ResKey getExportNameKey();

		/** {@link DefaultValueProvider} for {@link Config#getExportNameKey()}. */
		class ExportNameKeyDefault extends DefaultValueProviderShared {
			@Override
			public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
				return com.top_logic.layout.table.model.I18NConstants.DOWNLOAD_FILE_KEY;
			}
		}

		/** {@link DefaultValueProvider} for {@link Config#getExportNameKey()}. */
		class ExportSheetKeyDefault extends DefaultValueProviderShared {
			@Override
			public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
				return I18NConstants.DEFAULT_EXPORT_SHEET_NAME;
			}
		}

	}

	/** I18N key for the export sheet name. */
	private ResKey _exportSheetKey;

	/** I18N key for the name of export file. */
	private final ResKey _exportNameKey;

	/**
	 * Creates a {@link StreamingExcelExportHandler}.
	 */
	public StreamingExcelExportHandler(InstantiationContext context, Config config) {
		super(context, config);

		_exportNameKey = config.getExportNameKey();
		_exportSheetKey = config.getExportSheetKey();
	}

	@Override
	protected BinaryData createDownloadData(Runnable progressIncrementer, I18NLog log, LayoutComponent component) {
		return createExporter(progressIncrementer, log, component).createData();
	}

	/**
	 * Creates the export algorithm.
	 */
	protected Exporter createExporter(Runnable progressIncrementer, I18NLog log, LayoutComponent component) {
		return new Exporter(progressIncrementer, log, component);
	}

	/**
	 * The algorithm creating the export data.
	 */
	protected class Exporter {

		private RowContext _exportRowContext;

		private Map<Column, AdjustableCellValueContext> _exportRenderContexts;

		private final I18NLog _log;

		private final LayoutComponent _component;

		private final TableData _tableData;

		private final Runnable _progressIncrementer;

		/**
		 * Creates a {@link Exporter}.
		 */
		public Exporter(Runnable progressIncrementer, I18NLog log, LayoutComponent component) {
			_log = log;
			_component = component;
			_progressIncrementer = progressIncrementer;
			_tableData = extractTableData(getComponent());
		}

		/**
		 * The log output that allows the user to observe the progress of the export.
		 */
		public final I18NLog log() {
			return _log;
		}

		/**
		 * The table to export.
		 */
		public final TableData getTableData() {
			return _tableData;
		}

		/**
		 * The context component.
		 */
		public final LayoutComponent getComponent() {
			return _component;
		}

		/**
		 * Entry point for creating the export data.
		 */
		public BinaryData createData() {
			try {
				String downloadName = Resources.getInstance().getString(_exportNameKey);
				boolean xFormat = !downloadName.endsWith(POIUtil.XLS_SUFFIX);
				if (xFormat && !downloadName.endsWith(POIUtil.XLSX_SUFFIX)) {
					downloadName += POIUtil.XLSX_SUFFIX;
				}
				ExcelWriter writer = new ExcelWriter(xFormat);
				writer.newTable(Resources.getInstance().getString(_exportSheetKey));

				exportHeaders(writer);
				exportRows(writer);

				File tmpFile = writer.close();
				return BinaryDataFactory.createBinaryDataWithName(tmpFile, downloadName);
			} catch (IOException ex) {
				throw new TopLogicException(I18NConstants.ERROR_CREATING_EXPORT, ex);
			} finally {
				exportFinished();
			}
		}

		/**
		 * Exports the headers from the given table into the writer.
		 * @param writer
		 *        the writer to use to write excel cells
		 */
		protected void exportHeaders(ExcelWriter writer) {
			TableData tableData = getTableData();
			TableViewModel viewModel = tableData.getViewModel();

			log().info(I18NConstants.EXPORTING_HEADER);

			ExcelExportSupport exportSupport = ExcelExportSupport.newInstance();

			MutableInteger row = new MutableInteger(writer.currentRow());
			LabelProvider labels = ColumnLabelProvider.newInstance(tableData);
			exportSupport.exportColumnHeaders(consumer(writer, viewModel, row), viewModel, labels, row);
		}

		private BiConsumer<ExcelValue, Column> consumer(ExcelWriter writer, TableViewModel viewModel,
				MutableInteger row) {
			return new BiConsumer<>() {

				int excelRow = row.intValue();

				@Override
				public void accept(ExcelValue val, Column column) {
					try {
						if (val.getRow() != excelRow) {
							writer.newRow();
							excelRow = val.getRow();
						}
						writer.write(formatHeaderExcelValueForExport(val, viewModel, column));
					} catch (IOException ex) {
						throw new UncheckedIOException(ex);
					}
				}
			};
		}

		/**
		 * Exports the data rows from the given table into the writer
		 * @param writer
		 *        the writer to use to write excel cells
		 */
		protected void exportRows(ExcelWriter writer) throws IOException {
			TableViewModel viewModel = getTableData().getViewModel();
			int theRows = viewModel.getRowCount();
			ExcelExportSupport exportSupport = ExcelExportSupport.newInstance();
			List<Column> exportColumns =
				exportSupport.filterExportColumns(viewModel.getHeader().getAllElementaryColumns());
			int rowId = 0;
			_exportRowContext = new RowContext(viewModel, rowId, rowId);
			_exportRenderContexts = CollectionFactoryShared.map();
			for (; rowId < theRows; rowId++) {
				_exportRowContext.updateRows(rowId, rowId);
				_progressIncrementer.run();
				log().info(I18NConstants.EXPORTING_ROW__NUM_TOTAL.fill(rowId + 1, theRows));

				if (shouldExportRow(viewModel, rowId)) {
					writer.newRow();

					for (Column column : exportColumns) {
						if (exportSupport.excludeColumnFromExport(column)) {
							continue;
						}

						Object rawValue = loadValue(viewModel, rowId, column);
						Object exportValue = toExportValue(rawValue);
						if (!(exportValue instanceof Control)) {
							Object formattedValue = formatValue(exportValue, viewModel, rowId, column);
							writer.write(formattedValue);
						}
					}
				}
			}
			_exportRowContext = null;
			_exportRenderContexts = null;
		}

		/**
		 * Loads the value to export from the given table model.
		 * 
		 * @param viewModel
		 *        The component's table view model.
		 * @param rowId
		 *        The view model row being processed.
		 * @param column
		 *        Is not allowed to be null.
		 * 
		 * @return the value to export for the given of position of the grid.
		 */
		protected Object loadValue(TableViewModel viewModel, int rowId, Column column) {
			return viewModel.getValueAt(rowId, column.getName());
		}

		/**
		 * Maps a raw value retrieved from {@link #loadValue(TableViewModel, int, Column)} to a
		 * value usable for Excel export.
		 */
		protected Object toExportValue(Object value) {
			if (value instanceof Control) {
				return toExportValue((Control) value);
			} else if (value instanceof FormField) {
				return toExportValue((FormField) value);
			} else if (value instanceof FormMember) {
				return toExportValue((FormMember) value);
			} else {
				return value;
			}
		}

		/**
		 * Maps a {@link Control} to an value to export.
		 */
		protected Object toExportValue(Control control) {
			if (control instanceof BlockControl) {
				return toExportValue((BlockControl) control);
			} else if (control instanceof ButtonControl) {
				return toExportValue((ButtonControl) control);
			} else {
				return toExportValue(control.getModel());
			}
		}

		/**
		 * Maps a {@link BlockControl} to an value to export.
		 */
		protected Object toExportValue(BlockControl block) {
			for (HTMLFragment content : block.getChildren()) {
				if ((content instanceof SimpleConstantControl<?>)
					|| (content instanceof ButtonControl)
					|| (content instanceof AbstractFormMemberControl)) {
					return toExportValue(content);
				}
			}
			return null;
		}

		/**
		 * Special hook for exporting cells, which have a button control as content.
		 * 
		 * @param aButton
		 *        The button to get the value from, must not be <code>null</code>.
		 * @return The requested value to be exported.
		 */
		protected Object toExportValue(ButtonControl aButton) {
			return aButton.getLabel();
		}

		/**
		 * Maps a {@link FormMember} to an value to export.
		 */
		protected Object toExportValue(FormMember member) {
			return member.get(FormMember.BUSINESS_MODEL_PROPERTY);
		}

		/**
		 * Maps a {@link FormField} to an value to export.
		 */
		protected Object toExportValue(FormField field) {
			return field.getValue();
		}

		/**
		 * Formats a value retrieved from {@link #toExportValue(Object)} into the value to be
		 * written to the export. Can be used as a hook for subclasses. These should call super as
		 * the last statement.
		 * 
		 * @param aValue
		 *        The raw value from the table, may be <code>null</code>.
		 * @param model
		 *        the table model
		 * @param row
		 *        the row to put the value for
		 * @param column
		 *        Is not allowed to be null.
		 * @return the value formated for the export
		 */
		protected Object formatValue(Object aValue, TableViewModel model, int row, Column column) {
			ExcelCellRenderer excelRenderer = column.getConfig().getExcelRenderer();
			if (excelRenderer != null) {
				AdjustableCellValueContext cellContext =
					_exportRenderContexts.computeIfAbsent(column, ignored -> createCellValueContext(model, column));
				cellContext.setCellValue(aValue);
				return excelRenderer.renderCell(cellContext);
			} else if ((aValue instanceof Number) || (aValue instanceof Date)) {
				return aValue;
			} else {
				return MetaLabelProvider.INSTANCE.getLabel(aValue);
			}
		}

		/**
		 * Hook for sub classes to filter rows while exporting.
		 * 
		 * @param aModel
		 *        The component's table view model.
		 * @param aRow
		 *        The view model row being processed.
		 * @return <code>true</code> if it is permissible to export the given row.
		 */
		protected boolean shouldExportRow(TableViewModel aModel, int aRow) {
			return true;
		}

		/**
		 * Hook for sub classes to perform cleanup after exporting.
		 */
		protected void exportFinished() {
			// Hook.
		}

		/**
		 * This method formats an {@link ExcelValue} from the table into the value to be written to
		 * the header of the export.
		 * 
		 * <p>
		 * Note: When the {@link ExcelValue} has column <tt>i</tt>, it is not necessarily the
		 * <tt>i^th</tt> column of the given {@link TableViewModel}.
		 * </p>
		 * 
		 * @param headerValue
		 *        The {@link ExcelValue} containing the header value from the table.
		 * @param model
		 *        the table model
		 * @param applicationColumn
		 *        {@link Column} of the given {@link TableViewModel} currently exported.
		 * @return The value formated for the export. Typically the given {@link ExcelValue}.
		 */
		protected ExcelValue formatHeaderExcelValueForExport(ExcelValue headerValue, TableViewModel model,
				Column applicationColumn) {
			headerValue.setBold();
			headerValue.setFontSize(12.0);
			return headerValue;
		}

		private AdjustableCellValueContext createCellValueContext(TableViewModel model, Column column) {
			int resultColumn = model.getColumnIndex(column.getName());
			return new AdjustableCellValueContext(_exportRowContext,
				column.getConfig().getExcelRenderer().newCustomContext(model, column), column, resultColumn);
		}
	}

}
