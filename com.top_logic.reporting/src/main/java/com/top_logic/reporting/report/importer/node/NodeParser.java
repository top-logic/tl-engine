/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.importer.node;

import org.w3c.dom.Node;

import com.top_logic.reporting.report.model.Report;
import com.top_logic.reporting.report.model.ReportConfiguration;
import com.top_logic.reporting.report.xmlutilities.ReportReader;

/**
 * See {@link #parse(Node, Report)}.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 * @deprecated use {@link ReportReader} and {@link ReportConfiguration}
 */
@Deprecated
public interface NodeParser {
    
    /**
     * This method parses data from a node and its children and returns it as a
     * object or stores the information direct into the report. The node and the
     * report must not be <code>null</code>.
     * 
     * @param aNode
     *        The node. Must not be <code>null</code>.
     * @param aReport
     *        The report to store the information. Must not be <code>null</code>.
     */
    public Object parse(Node aNode, Report aReport);
    
}

