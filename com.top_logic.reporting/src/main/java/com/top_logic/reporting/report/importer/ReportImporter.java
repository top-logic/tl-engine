/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.importer;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.reporting.report.exception.ImportException;
import com.top_logic.reporting.report.model.Report;
import com.top_logic.reporting.report.xmlutilities.ReportReader;

/**
 * The ReportImporter imports {@link Report}s from report description files (xml). 
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
@Deprecated
public interface ReportImporter {

    /**
     * This method returns a {@link Report} which was imported from the given
     * file (report description file - xml) or throws an {@link ImportException}. 
     * 
     * This method returns never <code>null</code>.
     * 
     * @param aReportDescription
     *        A report description file (xml). 
     *        Must not be <code>null</code> and the file must exist.
     * @return A {@link Report}.
     * 
     * @deprecated use {@link ReportReader}
     */
    @Deprecated
	public Report importReportFrom(BinaryData aReportDescription) throws ImportException;
    
}

