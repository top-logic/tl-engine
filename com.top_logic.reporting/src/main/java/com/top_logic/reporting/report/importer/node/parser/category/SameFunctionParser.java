/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.importer.node.parser.category;

import org.w3c.dom.Node;

import com.top_logic.reporting.report.model.Report;
import com.top_logic.reporting.report.model.ReportConfiguration;
import com.top_logic.reporting.report.model.partition.function.PartitionFunction;
import com.top_logic.reporting.report.model.partition.function.SamePartitionFunction;
import com.top_logic.reporting.report.xmlutilities.ReportReader;

/**
 * @author     <a href="mailto:fsc@top-logic.com">Friedemann Schneider</a>
 * @deprecated use {@link ReportReader} and {@link ReportConfiguration}
 */
@Deprecated
public class SameFunctionParser extends AbstractPartitionFunctionParser {

 // Constants
    
    /** The function type this parser can handle. */
    public static final String TYPE = "same";

    /**
     * Creates a {@link StringFunctionParser}. 
     */
    public SameFunctionParser() {
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
        
        theFunction = new SamePartitionFunction(this.attribute, aReport.getLanguage(), ignoreNullValues, ignoreEmptyCategories);
        theFunction.setAttributeAccessor(this.getAccessor(aNode));
//        theFunction.setDecimalFormat(this.getDecimalFormat(aNode));
        
        return theFunction;
    }
}
