/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.demo.export;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.base.office.OfficeException;
import com.top_logic.base.office.ppt.POIPowerpointXUtil;
import com.top_logic.base.office.ppt.Powerpoint;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Settings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link AbstractCommandHandler} exporting to PPT and replaces a "FIXEDTABLE", with simple content.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SimpleFixedTablePPTExporter extends AbstractCommandHandler {

	/**
	 * Configuration for {@link SimpleFixedTablePPTExporter}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractCommandHandler.Config {

		@Override
		@FormattedDefault("theme:ICONS_EXPORT_POWERPOINT")
		ThemeImage getImage();

		@Override
		@FormattedDefault("theme:ICONS_EXPORT_POWERPOINT_DISABLED")
		ThemeImage getDisabledImage();

		/**
		 * Name of the path relative to <code>webinf://reportTemplates</code> in which the template
		 * file lives.
		 */
		@StringDefault("ppt")
		String getRelativeTemplatePath();

		/**
		 * Name of the template file used to export.
		 */
		@Mandatory
		String getTemplateFileName();

		/**
		 * Name of the marker in the template that represents the upper left corner of the table to
		 * export to. Must start with {@link POIPowerpointXUtil#PREFIX_FIXEDTABLE}
		 */
		@Mandatory
		String getFixedTableMarker();

		/**
		 * Number of rows to export.
		 */
		@IntDefault(5)
		int getRows();

		/**
		 * Number of columns to export.
		 */
		@IntDefault(8)
		int getColumns();

	}

	/**
	 * Creates a new {@link SimpleFixedTablePPTExporter}.
	 */
	public SimpleFixedTablePPTExporter(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {
		File tmpFile;
		try {
			tmpFile = createExport();
		} catch (IOException ex) {
			throw new IOError(ex);
		}
		int dotIndex = config().getTemplateFileName().lastIndexOf('.');
		String downloadName =
			config().getTemplateFileName().substring(0, dotIndex) + "_filled"
				+ config().getTemplateFileName().substring(dotIndex);
		aContext.getWindowScope().deliverContent(BinaryDataFactory.createBinaryData(tmpFile,
			MimeTypes.getInstance().getMimeType(tmpFile.getName()), downloadName));
		return HandlerResult.DEFAULT_RESULT;
	}

	private File createExport() throws IOException {
		boolean isXFormat = config().getTemplateFileName().endsWith(Powerpoint.PPTX_EXT);
		File tmpFile = File.createTempFile(getClass().getSimpleName(),
			isXFormat ? Powerpoint.PPTX_EXT : Powerpoint.PPT_EXT, Settings.getInstance().getTempDir());

		String[][] content = new String[config().getRows()][config().getColumns()];
		for (int row = 0; row < config().getRows(); row++) {
			String[] rowContent = content[row] = new String[config().getColumns()];
			for (int col = 0; col < config().getColumns(); col++) {
				rowContent[col] = "Table content in row " + row + " column " + col;
			}
		}

		Map<String, Object> values = new HashMap<>();
		values.put(config().getFixedTableMarker(), content);
		try (InputStream templateStream = this.getTemplateFileInputStream()) {
			Powerpoint ppt = Powerpoint.getInstance(isXFormat);
			ppt.setValues(templateStream, tmpFile, values);
		} catch (OfficeException e) {
			Logger.error("Problem creating Powerpoint report", e, this);
			throw new IOException("Problem creating Powerpoint report" + e);
		}
		return tmpFile;
	}

	private InputStream getTemplateFileInputStream() throws IOException {
		try {
			DataAccessProxy dap = new DataAccessProxy("webinf://reportTemplates", config().getRelativeTemplatePath());
			dap = dap.getChildProxy(config().getTemplateFileName());
			return dap.getEntry();
		} catch (Exception e) {
			Logger.error("problem getting template file", e, this);
			throw new IOException("Problem getting template file");
		}
	}

	private Config config() {
		return (Config) getConfig();
	}

}
