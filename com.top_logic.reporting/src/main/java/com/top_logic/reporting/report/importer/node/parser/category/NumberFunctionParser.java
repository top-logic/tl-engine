/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.importer.node.parser.category;

import java.util.List;

import org.w3c.dom.Node;

import com.top_logic.reporting.report.model.Report;
import com.top_logic.reporting.report.model.ReportConfiguration;
import com.top_logic.reporting.report.model.partition.function.NumberPartitionFunction;
import com.top_logic.reporting.report.model.partition.function.PartitionFunction;
import com.top_logic.reporting.report.xmlutilities.ReportReader;

/**
 * The NumberFunctionParser parses number functions from a {@link Node}.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 * @deprecated use {@link ReportReader} and {@link ReportConfiguration}
 */
@Deprecated
public class NumberFunctionParser extends AbstractPartitionFunctionParser {

    /** The function type this parser can handle. */
    public static final String TYPE       = "number";

    /**
     * Creates a {@link NumberFunctionParser}. 
     */
    public NumberFunctionParser() {
        // Do nothing.
    }
    
    @Override
	public String toString() {
        return getClass() + "[node name=" + TYPE + "]";
    }
    
    // Node parser methods
    
    @Override
	public Object parse(Node aNode, Report aReport) {
        super.parse(aNode, aReport);
        PartitionFunction theFunction;
        boolean ignoreEmptyCategories = this.getIgnoreEmptyCategories(aNode, false);
        List theFilters    = this.getRangeEntries(aNode, aReport);
        
        theFunction = new NumberPartitionFunction(this.attribute, aReport.getLanguage(), ignoreNullValues, ignoreEmptyCategories, theFilters);
        theFunction.setAttributeAccessor(this.getAccessor(aNode));
//        theFunction.setDecimalFormat(this.getDecimalFormat(aNode));
        
        return theFunction;
    }
    
    
}

