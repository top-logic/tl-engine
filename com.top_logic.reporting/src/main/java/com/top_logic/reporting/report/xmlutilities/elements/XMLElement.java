/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.xmlutilities.elements;

import com.top_logic.reporting.report.model.partition.function.PartitionFunction;
import com.top_logic.reporting.report.util.ReportConstants;
import com.top_logic.reporting.report.xmlutilities.XMLReportTags;

/**
 * The XMLElement is the basic interface for creating XML representations of {@link PartitionFunction}s.
 * 
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public interface XMLElement extends XMLReportTags, ReportConstants {

//	public Element createElement(Document aDocument);
}
