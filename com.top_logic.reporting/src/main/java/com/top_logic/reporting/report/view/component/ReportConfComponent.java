/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.view.component;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.reporting.report.exception.ReportingException;
import com.top_logic.reporting.report.importer.DefaultReportImporter;
import com.top_logic.reporting.report.importer.ReportImporter;
import com.top_logic.reporting.report.model.Report;

/**
 * This ReportComponent displays {@link Report}s which are defined in report description files
 * (xml). Which report description file is used for this component is defined in the attribute
 * 'report-description-file'. The root path of the report description files is the 'WEB-INF/reports'
 * directory. Please store all report description files into this directory or subdirectories of it.
 * 
 * E.g. <code>
 *   <component
 *          class                  =
 * "com.top_logic.reporting.report.view.component.ReportConfComponent"
 *          name                   ="ReportConfComponent" 
 *          resPrefix              ="demo.reporting."
 *          buttonComponent        ="ReportComponentButtons" 
 *          report-description-file="ReportExample.xml"
 *   />
 * </code>
 * 
 * @author <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
@Deprecated
public class ReportConfComponent extends ReportComponent {

    public interface Config extends ReportComponent.Config {
		@Name(REPORT_DESCRIPTION_FILE_ATTR)
		@Mandatory
		String getReportDescriptionFile();
	}

	/** This attribute specify the report xml description file.  */
    public static final String REPORT_DESCRIPTION_FILE_ATTR = "report-description-file";
    /** The root directory of the report description files.  */
	public static final String REPORT_ROOT_DIR = "/WEB-INF/reports";

    /** The path to the report description file (xml). */
    private String path;
    /**
     * The importer parses the report description file and returns the
     * {@link Report}.
     */
    private transient ReportImporter importer;

    /**
     * Creates a {@link ReportComponent}.
     */
    public ReportConfComponent(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
        
        this.path     = atts.getReportDescriptionFile();
        this.importer = DefaultReportImporter.INSTANCE;
    }

    @Override
	protected void becomingVisible() {
        super.becomingVisible();
        
        try {
			BinaryData theReportDescription =
				FileManager.getInstance().getDataOrNull(REPORT_ROOT_DIR + '/' + this.path);
			if (theReportDescription != null) {
				setModel(this.importer.importReportFrom(theReportDescription));
            } else {
				setModel(null);
            }
        }
        catch (ReportingException re) {
            Logger.error("Could not parse the report description file '" + this.path + "'.", re, this);
        }
    }
    
}
