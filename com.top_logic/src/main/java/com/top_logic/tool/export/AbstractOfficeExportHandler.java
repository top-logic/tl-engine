/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.layout.progress.DefaultProgressInfo;
import com.top_logic.layout.table.model.ExportConfig;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractDownloadHandler;
import com.top_logic.util.Resources;

/**
 * Common superclass for Donwloading office-files.
 *
 * @author    <a href="mailto:skr@top-logic.com">Sylwester Kras</a>
 */
public abstract class AbstractOfficeExportHandler extends AbstractDownloadHandler {

	public interface Config extends AbstractDownloadHandler.Config {

		// no properties here

	}

	public AbstractOfficeExportHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	@Deprecated
	public ResKey getDefaultI18NKey() {
		return I18NConstants.EXPORT_COMMAND;
	}
	
	@Override
	protected Object prepareDownload(LayoutComponent aComponent, DefaultProgressInfo progressInfo, Map<String, Object> arguments) throws Exception {
		if (aComponent instanceof ExportAware) {
			OfficeExportValueHolder theValues = ((ExportAware) aComponent).getExportValues(progressInfo, arguments);
			if (progressInfo.shouldStop()) {
				return null;
			}

			if(theValues == null) {
				throw new IOException("Export values missing");
			}

			if (theValues.myFile == null) {
    			// check for parameter
				if (theValues.exportData == null){
					throw new IOException("Export values missing");
				}					

    			if(! checkExportData(theValues)){
    				throw new IOException("EXPORT_DATA not valid");
    			}

                this.doExport(theValues);
			}

            return theValues;
		}
		throw new IOException("Only ExportAware components can be exported");
	}

    /**
	 * if the export data are valid for the exporter.
	 */
	protected abstract boolean checkExportData(OfficeExportValueHolder values);

	/**
	 * Convert the (raw) exportData found into a file and store it in in in aHolder.myFile
	 */
	protected abstract void doExport(OfficeExportValueHolder aHolder) throws IOException;

	/**
	 * the path of the templates relative to WEB-INF/reportTemplates
	 */
	protected abstract String getRelativeTemplatePath();

	/**
	 * an input stream for the template file. never <code>null</code>
	 */
	protected InputStream getTemplateFileInputStream(OfficeExportValueHolder aContext) throws IOException {
		if (aContext.templateData != null) {
			return aContext.templateData.getStream();
		}
		try{
			DataAccessProxy dap = new DataAccessProxy("webinf://reportTemplates",getRelativeTemplatePath());
			dap = dap.getChildProxy(aContext.templateFileName);
			return dap.getEntry();
		}
		catch(Exception e){
			Logger.error("problem getting template file",e,this);
			throw new IOException("Problem getting template file");
		}
	}

	@Override
	public BinaryDataSource getDownloadData(Object aPrepareResult) throws Exception {
		return BinaryDataFactory.createBinaryData(((OfficeExportValueHolder) aPrepareResult).myFile);
	}

    @Override
	public String getDownloadName(LayoutComponent aComponent, Object aPrepareResult) {
       return ((OfficeExportValueHolder) aPrepareResult).downloadFileName;
    }

	@Override
	public void cleanupDownload(Object model, Object aContext) {
		if(aContext!=null){ //in case of a previous exception (which is not always logged btw.) this method will be called with null context
			File file = ((OfficeExportValueHolder) aContext).myFile;
			if(file != null){
				file.delete();
			}
		}

	}

    /**
     * Value Holder for the Data to export to Office.
     */
    public static class OfficeExportValueHolder {

        /**
         * holds the temporary export file for download
         */
        public File myFile;

		/**
		 * The name of the template file to be used if no {@link #templateData} is set.
		 * 
		 * The template file is searched relative to
		 * {@link AbstractOfficeExportHandler#getRelativeTemplatePath()}.
		 */
        public String templateFileName = null;

        /** The template as binary data. If this is set, {@link #templateFileName} is ignored. */
        public BinaryData templateData = null;

        /**
         * the name the user is shown when "save as " is selected in the browser.
         */
        public String downloadFileName = null;

        /**
         * Either a Map or some other Object to extract the values from.
         */
        public Object exportData = null;

		/** Try to auto fit, esp. in Excel */
        public boolean autoFit = true;

        /** Flag whether to use layout of given template instead of target template in slide replacement in Powerpoint. */
        public boolean useTemplateLayout = true;

		public OfficeExportValueHolder(ExportConfig exportConfig, Object exportData) {
			this(exportConfig.getTemplateName(), Resources.getInstance().getString(exportConfig.getDownloadNameKey()),
				exportData, exportConfig.getAutofitColumns());
		}

        /**
         * Create a new OfficeExportValueHolder ...
         *
         * @param aTemplateFileName Name of the template file
         */
        public OfficeExportValueHolder(String aTemplateFileName, String aDownloadFileName, Object aExportData) {
			this(aTemplateFileName, aDownloadFileName, aExportData, true);
        }

        /**
         * Create a new OfficeExportValueHolder ...
         *
         * @param aTemplateFileName Name of the template file
         */
        public OfficeExportValueHolder(String aTemplateFileName, String aDownloadFileName, Object aExportData, boolean autoFit) {
			this(aTemplateFileName, aDownloadFileName, aExportData, autoFit, true);
        }

        /**
         * Creates a new {@link OfficeExportValueHolder}.
         */
        public OfficeExportValueHolder(String aTemplateFileName, String aDownloadFileName, Object aExportData, boolean autoFit, boolean useTemplateLayout) {
        	this(null, aTemplateFileName, aDownloadFileName, aExportData, autoFit, useTemplateLayout);
        }

        /**
         * Creates a new {@link OfficeExportValueHolder}.
         */
        public OfficeExportValueHolder(BinaryData templateData, String aTemplateFileName, String aDownloadFileName, Object aExportData, boolean autoFit, boolean useTemplateLayout) {
        	this.templateData = templateData;
        	this.templateFileName = aTemplateFileName;
        	this.downloadFileName = aDownloadFileName;
        	this.exportData = aExportData;
        	this.autoFit = autoFit;
        	this.useTemplateLayout = useTemplateLayout;
        }

    	/**
    	 * Gets the name of the template.
    	 */
    	public String getTemplateName() {
    		if (templateData != null) {
    			return templateData.getName();
    		}
    		if (templateFileName != null) {
    			return templateFileName;
    		}
    		return null;
    	}

        /**
         * Return some reasonable String for debugging.
         */
        @Override
		public String toString() {
            return this.getClass().toString()
				+ "b:" + templateData
                + "t:" + templateFileName
                + "d:" + downloadFileName
				+ "e:" + exportData
				+ "a:" + autoFit
				+ "u:" + useTemplateLayout;
        }

    }

}
