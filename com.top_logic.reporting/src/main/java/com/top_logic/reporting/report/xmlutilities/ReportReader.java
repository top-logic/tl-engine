/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.xmlutilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.FileBasedBinaryContent;
import com.top_logic.basic.io.character.CharacterContent;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.reporting.report.model.ReportConfiguration;
import com.top_logic.reporting.report.model.ReportFactory;
import com.top_logic.reporting.report.model.RevisedReport;

/**
 * The ReportReader imports {@link RevisedReport}s from report description files (xml).
 * 
 * @author <a href="mailto:tbe@top-logic.com">Till Bentz</a>
 */
public class ReportReader {

	public static RevisedReport readFile(File aFile) throws XMLStreamException, ConfigurationException, FileNotFoundException {
		ConfigurationDescriptor theDesc = TypedConfiguration.getConfigurationDescriptor(ReportConfiguration.class); 
		
		Map<String, ConfigurationDescriptor> globalDescriptors = Collections.singletonMap("report", theDesc);
		BinaryContent content = FileBasedBinaryContent.createBinaryContent(aFile);
		ReportConfiguration theConf =
			(ReportConfiguration) new ConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
		globalDescriptors).setSource(content).read();
        return ReportFactory.getInstance().getReport(theConf);
	}
	
	public static RevisedReport readString(String aString) throws XMLStreamException, ConfigurationException {
		ConfigurationDescriptor theDesc = TypedConfiguration.getConfigurationDescriptor(ReportConfiguration.class); 
		
		Map<String, ConfigurationDescriptor> globalDescriptors = Collections.singletonMap("report", theDesc);
		CharacterContent content = CharacterContents.newContent(aString);
		ReportConfiguration theConf =
			(ReportConfiguration) new ConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
		globalDescriptors).setSource(content).read();
        return ReportFactory.getInstance().getReport(theConf);
	}
	
	public static ReportConfiguration readConfig(File aFile) throws XMLStreamException, ConfigurationException {
		ConfigurationDescriptor theDesc = TypedConfiguration.getConfigurationDescriptor(ReportConfiguration.class);

		Map<String, ConfigurationDescriptor> globalDescriptors = Collections.singletonMap("report", theDesc);
		BinaryContent content = FileBasedBinaryContent.createBinaryContent(aFile);
		return (ReportConfiguration) new ConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			globalDescriptors).setSource(content).read();
	}

	public static ReportConfiguration readConfig(String aString) throws XMLStreamException, ConfigurationException {
		ConfigurationDescriptor theDesc = TypedConfiguration.getConfigurationDescriptor(ReportConfiguration.class); 
		
		Map<String, ConfigurationDescriptor> globalDescriptors = Collections.singletonMap("report", theDesc);
		CharacterContent content = CharacterContents.newContent(aString);
		return (ReportConfiguration) new ConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			globalDescriptors).setSource(content).read();
	}
	
}
