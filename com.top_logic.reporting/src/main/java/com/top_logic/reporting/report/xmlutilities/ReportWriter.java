/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.xmlutilities;

import java.io.StringWriter;
import java.io.Writer;

import javax.xml.stream.XMLStreamException;

import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.reporting.report.model.ReportConfiguration;
import com.top_logic.reporting.report.model.RevisedReport;

/**
 * Interface that defines the necessary methods for all ReportWriters.
 * 
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class ReportWriter {
	
	public static String writeReport(RevisedReport aReport) throws XMLStreamException {
	    StringWriter theWriter = new StringWriter(10240);
	    
	    writeReport(theWriter, aReport.getConfiguration());
	    theWriter.flush();
	    
	    return theWriter.getBuffer().toString();
	}

	public static void writeReport(Writer aWriter, ReportConfiguration aConfig) throws XMLStreamException {
	    ConfigurationWriter theWriter = new ConfigurationWriter(aWriter);
	    
	    theWriter.write("report", ReportConfiguration.class, aConfig);
	}
	
	public static String writeReportConfig(ReportConfiguration aConfig) throws XMLStreamException {
		 StringWriter theWriter = new StringWriter(10240);
		    
		 writeReport(theWriter, aConfig);
		 theWriter.flush();

		 return theWriter.getBuffer().toString();
	}
}
