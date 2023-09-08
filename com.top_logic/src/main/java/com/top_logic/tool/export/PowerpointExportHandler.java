/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.top_logic.base.office.OfficeException;
import com.top_logic.base.office.ppt.Powerpoint;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Settings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.layout.basic.ThemeImage;

/**
 * Handler for export of powerpoint reports
 *
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class PowerpointExportHandler extends AbstractOfficeExportHandler {

	/** The command provided by this instance. */
    public static final String COMMAND = "exportPowerpoint";

	/** Configuration for the {@link PowerpointExportHandler}. */
	public interface Config extends AbstractOfficeExportHandler.Config {

		@Override
		@FormattedDefault("theme:ICONS_EXPORT_POWERPOINT")
		ThemeImage getImage();

		@Override
		@FormattedDefault("theme:ICONS_EXPORT_POWERPOINT_DISABLED")
		ThemeImage getDisabledImage();

	}

	public PowerpointExportHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

    /**
     * creates the export
     */
    @Override
	protected void doExport(OfficeExportValueHolder aHolder) throws IOException {
		boolean isXFormat = aHolder.getTemplateName().toLowerCase().endsWith(Powerpoint.PPTX_EXT);
		File tmpFile = aHolder.myFile = File.createTempFile("PowerpointExportHandler",
				isXFormat ? Powerpoint.PPTX_EXT : Powerpoint.PPT_EXT, Settings.getInstance().getTempDir());

        Map theValues = this.getValues(aHolder.exportData);
		try (InputStream templateStream = this.getTemplateFileInputStream(aHolder)) {
			Powerpoint ppt = Powerpoint.getInstance(isXFormat);
			ppt.setValues(templateStream, tmpFile, theValues);
		} catch (OfficeException e) {
            Logger.error("Problem creating Powerpoint report",e,this);
            throw new IOException("Problem creating Powerpoint report" + e);
        }
    }

	/**
	 * the path of the templates relative to WEB-INF/reportTemplates
	 */
	@Override
	protected String getRelativeTemplatePath(){
		return "ppt";
	}
	
	/**
     * This method assumes the data is actually a map.
	 */
	protected Map getValues(Object data){
		return (Map) data;
	}

    /**
     * For PowerPoint aValues.exportData must be a Map.
     */
    @Override
	protected boolean checkExportData(OfficeExportValueHolder aValues) {
        return aValues.exportData instanceof Map;
    }
}
