/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.importer.node.parser.category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Node;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.filter.EqualsFilter;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.reporting.report.importer.node.parser.range.AbstractRangeParser;
import com.top_logic.reporting.report.model.Report;
import com.top_logic.reporting.report.model.ReportConfiguration;
import com.top_logic.reporting.report.model.filter.FilterEntry;
import com.top_logic.reporting.report.model.partition.function.ClassificationPartitionFunction;
import com.top_logic.reporting.report.model.partition.function.PartitionFunction;
import com.top_logic.reporting.report.xmlutilities.ReportReader;

/**
 * The ClassificationFunctionParser parses classification functions from a {@link Node}.
 * A ClassificationFunction can be configured in with tree disjoint options:
 *  1. <list-manual>comma,seperated,list,of,Strings,Dates,01.01.2007,or,Numbers,1,2,3</list-manual>
 *  2. <list-fastlist>tl.yesno</list-fastlist>
 *  3. <ranges>
 *      <!-- range(s) parsed by {@link AbstractRangeParser} --> 
 *      <range>
 *      </range>
 *      <range>
 *      </range>
 *     </ranges>
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 * @deprecated use {@link ReportReader} and {@link ReportConfiguration}
 */
@Deprecated
public class ClassificationFunctionParser extends AbstractPartitionFunctionParser {

    /** The function type this parser can handle. */
    public static final String TYPE = "classification";
    
    /**
     * This option can be set if the user will provide a manual list of values.
     */
    public static final String LIST_MANUAL_TAG    = "list-manual";
    
    /**
     * This option can be set to a {@link FastList} name.
     */
    public static final String LIST_FASTLIST_TAG  = "list-fastlist";

    public ClassificationFunctionParser() {
        
    }
    
    @Override
	public String toString() {
        return getClass() + "[node name=" + TYPE + "]";
    }
    
    // Node parser methods
    /**
	 * This method parses the <code>category-funtion</code> tree and extracts special options for a
	 * {@link ClassificationPartitionFunction}
	 * 
	 * @param aNode
	 *        should be the <code>category-funtion</code> node, will fail otherwise
	 * @return ClassificationFunction
	 * 
	 * @see com.top_logic.reporting.report.importer.node.NodeParser#parse(org.w3c.dom.Node,
	 *      com.top_logic.reporting.report.model.Report)
	 */
    @Override
	public Object parse(Node aNode, Report aReport) {
        super.parse(aNode, aReport);
        
        boolean ignoreEmptyCategories = this.getIgnoreEmptyCategories(aNode, false);
        PartitionFunction theFunction;
        
        if (hasSingleNode(aNode, LIST_MANUAL_TAG)) {
            String   theValue   = getNodeDataAsString(aNode, LIST_MANUAL_TAG);
            String[] theStrings = StringServices.toArray(theValue, ",");
            List     theFilters = new ArrayList();
            for (int i = 0; i < theStrings.length; i++) {
                HashMap theResources = new HashMap();
                String theString     = theStrings[i];
                theResources.put(aReport.getLanguage(), theString);
                FilterEntry theFilter = new FilterEntry(new EqualsFilter(extractObject(theString)), aReport.getLanguage(), theResources);
                theFilters.add(theFilter);
            }
            return new ClassificationPartitionFunction(this.attribute, aReport.getLanguage(), ignoreNullValues, ignoreEmptyCategories, theFilters);
        }
        else if (hasSingleNode(aNode, LIST_FASTLIST_TAG)) {
            String   theValue    = getNodeDataAsString(aNode, LIST_FASTLIST_TAG);
            FastList theFastList = FastList.getFastList(theValue);
            theFunction = new ClassificationPartitionFunction(this.attribute, aReport.getLanguage(), ignoreNullValues, ignoreEmptyCategories, theFastList);
        }
        else if (hasSingleNode(aNode, RANGES_TAG)){
            List theFilters    = this.getRangeEntries(aNode, aReport);
            theFunction = new ClassificationPartitionFunction(this.attribute, aReport.getLanguage(), ignoreNullValues, ignoreEmptyCategories, theFilters);
        }
        else {
            theFunction = new ClassificationPartitionFunction(this.attribute, aReport.getLanguage(), ignoreNullValues, ignoreEmptyCategories);
        }
        theFunction.setAttributeAccessor(this.getAccessor(aNode));
//        theFunction.setDecimalFormat(this.getDecimalFormat(aNode));
        
        return theFunction;
    }
    
}

