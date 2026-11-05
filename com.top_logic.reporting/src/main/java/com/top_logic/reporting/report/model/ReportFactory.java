/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model;

import com.top_logic.basic.config.ConfigurationException;

/**
 * Factory for {@link ReportConfiguration}s and {@link RevisedReport}s.
 *
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
@Deprecated
public class ReportFactory extends AbstractConfigurationBasedFactory {

    private static ReportFactory instance;
    
	/** Creates a new configuration for the given report class. */
	public ReportConfiguration createReportConfiguration(Class<? extends RevisedReport> anImplementationClass)
			throws ConfigurationException {
        assert RevisedReport.class.isAssignableFrom(anImplementationClass);
        
        return (ReportConfiguration) super.createConfiguration(anImplementationClass);
    }
    
    /** Creates the report for the given configuration. */
    public RevisedReport getReport(ReportConfiguration aConfig) {
		return super.createImplementation(aConfig);
    }
    
    /** The singleton {@link ReportFactory} instance. */
    public static ReportFactory getInstance() {
        if (instance == null) {
            instance = new ReportFactory();
        }
        return instance;
    }
}

