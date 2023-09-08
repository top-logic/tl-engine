/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.importer.node.parser.category;

import java.util.List;

import org.w3c.dom.Node;

import com.top_logic.reporting.report.model.Report;
import com.top_logic.reporting.report.model.ReportConfiguration;
import com.top_logic.reporting.report.model.partition.function.DatePartitionFunction;
import com.top_logic.reporting.report.model.partition.function.PartitionFunction;
import com.top_logic.reporting.report.xmlutilities.ReportReader;

/**
 * The DateFunctionParser parses date functions from a {@link Node}.
 * 
 * @author     <a href="mailto:fsc@top-logic.com">Friedemann Schneider</a>
 * @deprecated use {@link ReportReader} and {@link ReportConfiguration}
 */
@Deprecated
public class DateFunctionParser extends AbstractPartitionFunctionParser {

    /** The function type this parser can handle. */
    public static final String TYPE            = "date";
    
    /**
     * Creates a {@link NumberFunctionParser}. 
     */
    public DateFunctionParser() {
        
    }
    
    @Override
	public Object parse(Node aNode, Report aReport) {
        super.parse(aNode, aReport);
        
        PartitionFunction theFunction = null; 
        boolean ignoreEmptyCategories = this.getIgnoreEmptyCategories(aNode, false);
        List theFilters               = this.getRangeEntries(aNode, aReport);
        
        if (!theFilters.isEmpty()) {
        	//TODO tbe: is not working at all 
            theFunction =  new DatePartitionFunction(this.attribute, aReport.getLanguage(),ignoreNullValues, ignoreEmptyCategories, null, null, null, null);
            theFunction.setAttributeAccessor(this.getAccessor(aNode));
        }
        return theFunction;
    }

}
