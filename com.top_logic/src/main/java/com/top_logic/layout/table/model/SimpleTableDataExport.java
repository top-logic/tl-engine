/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

import org.apache.commons.io.FilenameUtils;

import com.top_logic.base.office.OfficeException;
import com.top_logic.base.office.excel.ExcelAccess;
import com.top_logic.base.office.excel.ExcelValue;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Settings;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.impl.NonNegative;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.knowledge.gui.layout.upload.DefaultDataItem;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.table.TableData;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.export.ExcelExportSupport;

/**
 * {@link ExecutableTableDataExport} that exports the table using {@link ExcelAccess}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SimpleTableDataExport extends AbstractConfiguredInstance<SimpleTableDataExport.Config>
		implements TableDataExport {

	/**
	 * Configuration of a {@link SimpleTableDataExport}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<SimpleTableDataExport>, ExportConfig {

		/**
		 * Number of the first row (zero based) in the template file in which application data are
		 * stored, e.g. with value of <code>0</code> the data export fills the first row.
		 */
		@Hidden
		@Constraint(NonNegative.class)
		int getStartRow();

		/**
		 * Setter for {@link #getStartRow()}.
		 */
		void setStartRow(int value);

		/**
		 * Whether all information should be exported (not only thus currently displayed).
		 */
		@Hidden
		boolean getIncludeHidden();

		/**
		 * Setter for {@link #getIncludeHidden()}.
		 */
		void setIncludeHidden(boolean value);

		@Override
		@Hidden
		String getTemplateName();

		/**
		 * Function that computes the {@link ExecutableState} for a given {@link TableData}.
		 * 
		 * @return May be <code>null</code> which means "always executable".
		 */
		@Hidden
		PolymorphicConfiguration<Function<TableData, ExecutableState>> getExecutability();

		/**
		 * Setter for {@link #getExecutability()}.
		 */
		void setExecutability(PolymorphicConfiguration<Function<TableData, ExecutableState>> value);
	}

	private Function<TableData, ExecutableState> _executability;

	/**
	 * Creates a new {@link SimpleTableDataExport} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link SimpleTableDataExport}.
	 */
	public SimpleTableDataExport(InstantiationContext context, Config config) {
		super(context, config);
		_executability = createExecutability(context, getConfig());

	}

	private static Function<TableData, ExecutableState> createExecutability(InstantiationContext ctx, Config config) {
		Function<TableData, ExecutableState> executability = ctx.getInstance(config.getExecutability());
		if (executability == null) {
			executability = data -> ExecutableState.EXECUTABLE;
		}
		return executability;
	}

	@Override
	public ExecutableState getExecutability(TableData table) {
		return _executability.apply(table);
	}

	@Override
	public HandlerResult exportTableData(DisplayContext context, TableData table) {
		String ending = excelExtension(templateFilename());
		String downloadFileName = context.getResources().getString(getConfig().getDownloadNameKey()) + ending;
		BinaryData downloadItem;
		try {
			downloadItem = createExportData(table, downloadFileName);
		} catch (IOException ex) {
			return HandlerResult.error(I18NConstants.ERROR_TABLE_EXPORT, ex);
		}

		context.getWindowScope().deliverContent(downloadItem);
		return HandlerResult.DEFAULT_RESULT;
	}

	private String excelExtension(String filename) {
		int extensionIndex = FilenameUtils.indexOfExtension(filename);
		String ending;
		if (extensionIndex == -1) {
			ending = ExcelAccess.XLS_EXT;
		} else {
			ending = filename.substring(extensionIndex);
		}
		return ending;
	}

	@Override
	public BinaryData createExportData(TableData table, String name) throws IOException {
		File tmpFile =
			File.createTempFile("SimpleTableDataExport", excelExtension(name), Settings.getInstance().getTempDir());

		doExport(tmpFile, table);

		return createBinaryData(tmpFile, name);
	}

	private DefaultDataItem createBinaryData(File tempExcelFile, String name) {
		BinaryData dataItem = BinaryDataFactory.createBinaryData(tempExcelFile);
		String contentType = MimeTypes.getInstance().getMimeType(tempExcelFile.getName());
		// Needs to wrap BinaryData created from temporary file to get correct name.
		return new DefaultDataItem(name, dataItem, contentType);
	}

	private void doExport(File outFile, TableData table) throws IOException {
		try (InputStream templateStream = this.getTemplateFileInputStream()) {
			Config config = getConfig();
			ExcelValue[] input = ExcelExportSupport.newInstance().excelValuesFromTable(table, config.getIncludeHidden(),
				config.getStartRow());
			ExcelAccess.newInstance().setValuesDirect(templateStream, outFile, input, config.getAutofitColumns());
		} catch (OfficeException e) {
			Logger.error("Problem creating Excel report", e, SimpleTableDataExport.class);
			throw new IOException("Problem creating Excel report", e);
		}
	}

	/**
	 * an input stream for the template file. never <code>null</code>
	 */
	protected InputStream getTemplateFileInputStream() throws IOException {
		try {
			DataAccessProxy dap = new DataAccessProxy("webinf://reportTemplates", getRelativeTemplatePath());
			dap = dap.getChildProxy(templateFilename());
			return dap.getEntry();
		} catch (Exception e) {
			Logger.error("problem getting template file", e, this);
			throw new IOException("Problem getting template file");
		}
	}

	private String templateFilename() {
		return getConfig().getTemplateName();
	}

	protected String getRelativeTemplatePath() {
		return "excel";
	}

}

