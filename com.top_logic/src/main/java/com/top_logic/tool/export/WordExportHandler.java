/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.top_logic.base.office.OfficeException;
import com.top_logic.base.office.word.WordAccess;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Settings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.layout.basic.ThemeImage;

/**
 * Handler for export of word reports.
 * 
 * @author     <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class WordExportHandler extends AbstractOfficeExportHandler {

	/** The unique command identifier. */
    public static final String COMMAND_ID = "exportWord";
	
	/** Configuration for the {@link WordExportHandler}. */
	public interface Config extends AbstractOfficeExportHandler.Config {

		@Override
		@FormattedDefault("theme:ICONS_EXPORT_WORD")
		ThemeImage getImage();

		@Override
		@FormattedDefault("theme:ICONS_EXPORT_WORD_DISABLED")
		ThemeImage getDisabledImage();

	}

	public WordExportHandler(InstantiationContext context, Config config) {
		super(context, config);
	}
    
	/** This methods sets the values into the download word file. */
	@Override
	protected void doExport(OfficeExportValueHolder aHolder) throws IOException {
	 	File tmpFile = aHolder.myFile = File.createTempFile("WordExportHandler",".doc", Settings.getInstance().getTempDir());

	 	try{
			Object theData = aHolder.exportData;
			InputStream templateStream = this.getTemplateFileInputStream(aHolder);
			try {
				Map theDataMap = getValues(theData);

				WordAccess.getInstance().setValues(templateStream, tmpFile, theDataMap);
			} finally {
				templateStream.close();
			}
	 	}
	 	catch(OfficeException oe){
			String message = "Couldn't start the word export because an office exception has occured.";
			Logger.error(message, oe, WordExportHandler.class);
			throw new IOException(message, oe);
	 	}
	}

	/** Returns the path of the templates relative to WEB-INF/reportTemplates. */
	@Override
	protected String getRelativeTemplatePath(){
		return "word";
	}
	
    /** Returns <code>true</code> if the export data is an instance of {@link Map}, 
     * <code>false</code> otherwise. */
    @Override
	protected boolean checkExportData(OfficeExportValueHolder aValues) {
        return aValues.exportData instanceof Map;
    }
    
    /** This is a hook for subclasses to convert the given data 
     * (as object) to an map which the word access can understand. */
	protected Map getValues(Object data){
		return (Map) data;
	}

}
