/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.export;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import com.top_logic.base.office.OfficeException;
import com.top_logic.base.office.excel.ExcelAccess;
import com.top_logic.base.office.excel.ExcelValue;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Settings;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.order.DisplayInherited;
import com.top_logic.basic.config.order.DisplayInherited.DisplayStrategy;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.func.Function0;
import com.top_logic.basic.i18n.log.I18NLog;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.structure.ControlRepresentable;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.model.ExportConfig;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.export.ExcelExportSupport;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * Handler for generating Excel reports from {@link TableData}.
 * 
 * @implNote The component must be {@link ControlRepresentable} and render itself through a
 *           {@link TableControl}.
 */
@InApp(classifiers = { "treegrid", "grid", "table", "treetable" })
public class ExcelExportHandler extends AbstractTableExportHandler {

	/**
	 * Configuration options for {@link ExcelExportHandler}.
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
		Config.TEMPLATE_NAME,
		Config.AUTOFIT_COLUMNS,
		Config.DOWNLOAD_NAME_KEY,
	})
	public interface Config extends AbstractTableExportHandler.Config, ExportConfig {

		@Override
		@Options(fun = ExportTemplates.class)
		String getTemplateName();

		/**
		 * Names of available export templates.
		 * 
		 * @see Config#getTemplateName()
		 */
		class ExportTemplates extends Function0<List<String>> {
			@Override
			public List<String> apply() {
				List<String> result = new ArrayList<>();
				DataAccessProxy dap = new DataAccessProxy("webinf://reportTemplates", "excel");
				DataAccessProxy[] entries = dap.getEntries();
				if (entries != null) {
					for (DataAccessProxy entry : entries) {
						if (entry.isEntry()) {
							result.add(entry.getName());
						}
					}
				}
				return result;
			}
		}
	}

	/**
	 * Creates a {@link ExcelExportHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ExcelExportHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected BinaryData createDownloadData(I18NLog log, LayoutComponent component) {
		log.info(I18NConstants.STARTING_EXPORT);
		Config config = (Config) getConfig();

		String templateName = config.getTemplateName();
		String ext = getFileExtension(templateName);

		File tmpFile = createTmpFile(ext);
		boolean autofitColumns = config.getAutofitColumns();
		log.info(I18NConstants.EXTRACTING_TABLE_DATA);
		TableData tableData = extractTableData(component);

		log.info(I18NConstants.EXPORTING_DATA);
		doExport(tmpFile, templateName, autofitColumns, tableData);

		log.info(I18NConstants.PREPARING_DOWNLOAD);
		String downloadName =
			FileUtilities.removeFileExtension(Resources.getInstance().getString(config.getDownloadNameKey())) + ext;
		return BinaryDataFactory.createBinaryDataWithName(tmpFile, downloadName);
	}

	private String getFileExtension(String templateName) {
		int extensionIndex = FilenameUtils.indexOfExtension(templateName);
		if (extensionIndex == -1) {
			return ExcelAccess.XLS_EXT;
		} else {
			return templateName.substring(extensionIndex);
		}
	}

	/**
	 * Creates the actual binary export data.
	 *
	 * @param tmpFile
	 *        The temporary file to write to.
	 * @param templateName
	 *        The name of the export template to use.
	 * @param autoFit
	 *        Whether to auto-adjust column widths.
	 * @param tableData
	 *        The data to export.
	 */
	protected void doExport(File tmpFile, String templateName, boolean autoFit, TableData tableData) {
		try (InputStream templateStream = this.loadTemplate(templateName)) {
			ExcelValue[] cells = ExcelExportSupport.newInstance().excelValuesFromTable(tableData);
			ExcelAccess.newInstance().setValuesDirect(templateStream, tmpFile, cells, autoFit);
		} catch (IOException | OfficeException ex) {
			throw new TopLogicException(I18NConstants.ERROR_CREATING_EXPORT, ex);
		}
	}

	private File createTmpFile(String ext) {
		try {
			return File.createTempFile("excel-export", ext, Settings.getInstance().getTempDir());
		} catch (IOException ex) {
			throw new TopLogicException(I18NConstants.ERROR_CREATING_EXPORT, ex);
		}
	}

	/**
	 * Retrieves the export template data.
	 */
	protected InputStream loadTemplate(String templateName) throws IOException {
		try {
			DataAccessProxy dap = new DataAccessProxy("webinf://reportTemplates", "excel");
			dap = dap.getChildProxy(templateName);
			return dap.getEntry();
		} catch (Exception e) {
			Logger.error("problem getting template file", e, this);
			throw new IOException("Problem getting template file");
		}
	}

	@Override
	public ResKey getDefaultI18NKey() {
		return com.top_logic.layout.table.renderer.I18NConstants.EXPORT_EXCEL;
	}

}