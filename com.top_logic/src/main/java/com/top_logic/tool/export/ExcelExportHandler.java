/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

import com.top_logic.base.office.OfficeException;
import com.top_logic.base.office.excel.ExcelAccess;
import com.top_logic.base.office.excel.ExcelValue;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Settings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Handler for generating Excel reports.
 *
 * @deprecated Use {@link com.top_logic.layout.table.export.ExcelExportHandler}
 */
@Deprecated
public class ExcelExportHandler extends AbstractOfficeExportHandler {

	/** A Static Set of (a single) READ Command */
	public static final Set READEXPORT_SET = new HashSet(Arrays.asList(new Object[] {
		SimpleBoundCommandGroup.READ, SimpleBoundCommandGroup.EXPORT }));

	/** A Static Set of (a single) READ Command */
	public static final Set READWRITEEXPORT_SET = new HashSet(Arrays.asList(new Object[] {
		SimpleBoundCommandGroup.READ, SimpleBoundCommandGroup.WRITE, SimpleBoundCommandGroup.EXPORT }));

	/** The command provided by this instance. */
    public static final String COMMAND = "exportExcel";

	public interface Config extends AbstractOfficeExportHandler.Config {

		@Override
		@FormattedDefault("theme:ICONS_EXPORT_GRID")
		ThemeImage getImage();

		@Override
		@FormattedDefault("theme:ICONS_EXPORT_GRID_DISABLED")
		ThemeImage getDisabledImage();

	}

	public ExcelExportHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

    /**
	 * Creates the export.
	 */
	@Override
	protected void doExport(OfficeExportValueHolder aHolder) throws IOException {
		String ending = tmpFileExtension(aHolder.getTemplateName());
		File tmpFile = aHolder.myFile =
			File.createTempFile("ExcelExportHandler", ending, Settings.getInstance().getTempDir());

	 	try{
            Object       theData        = aHolder.exportData;
            InputStream  templateStream = this.getTemplateFileInputStream(aHolder);
			try {
				ExcelValue[] theInput = this.getValues(theData);
				ExcelAccess.newInstance().setValuesDirect(templateStream, tmpFile, theInput, aHolder.autoFit);
			} finally {
				templateStream.close();
			}
	 	}
	 	catch(OfficeException e){
	 		Logger.error("Problem creating Excel report",e,this);
	 		throw (IOException) new IOException("Problem creating Excel report").initCause(e);
	 	}

	}

	private String tmpFileExtension(String templateFileName) {
		int extensionIndex = FilenameUtils.indexOfExtension(templateFileName);
		String ending;
		if (extensionIndex == -1) {
			ending = ExcelAccess.XLS_EXT;
		} else {
			ending = templateFileName.substring(extensionIndex);
		}
		return ending;
	}

	/**
	 * the path of the templates relative to WEB-INF/reportTemplates
	 */
	@Override
	protected String getRelativeTemplatePath(){
		return "excel";
	}


    /**
     * Check the aValues.exportData is instanceof ExcelValue[].
     */
    @Override
	protected boolean checkExportData(OfficeExportValueHolder aValues) {
		return (aValues.exportData instanceof TableControl)
			|| aValues.exportData instanceof ExcelValue[];
    }

    /**
     * Override as needed to convert the data into ExcelValue[].
     *
     * This implementation just casts data to ExcelValue[].
     */
	protected ExcelValue[] getValues(Object data){
		if (data instanceof ExcelValue[]) {
			return (ExcelValue[]) data;
		}
		else if (data instanceof TableControl) {
			TableControl theTable = (TableControl) data;
			TableModel theModel = theTable.getApplicationModel();
			return ExcelExportSupport.newInstance().excelValuesFromTable(theTable.getTableData());
		}
		else {
			return new ExcelValue[0];
		}
	}

}
