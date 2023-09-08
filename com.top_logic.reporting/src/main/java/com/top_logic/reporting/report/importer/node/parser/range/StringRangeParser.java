/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.importer.node.parser.range;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Node;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.PatternFilter;
import com.top_logic.basic.col.filter.StartsWithFilter;
import com.top_logic.reporting.report.importer.node.NodeParser;
import com.top_logic.reporting.report.model.Report;
import com.top_logic.reporting.report.model.ReportConfiguration;
import com.top_logic.reporting.report.model.filter.FilterEntry;
import com.top_logic.reporting.report.model.partition.function.StringPartitionFunction;
import com.top_logic.reporting.report.xmlutilities.ReportReader;

/**
 * This parser is used to extrat some special options for a {@link StringPartitionFunction}.
 * 
 * @author     <a href="mailto:fsc@top-logic.com">Friedemann Schneider</a>
 * @deprecated use {@link ReportReader} and {@link ReportConfiguration}
 */
@Deprecated
public class StringRangeParser extends DefaultRangeParser implements NodeParser {

    /**
     * String objects will be inserted to this category if they 
     * starts with the given String. 
     */
    public static final String STARTS_WITH_TAG = "startsWith";
    
    /**
     * String objects will be inserted to this category if they 
     * match the given pattern
     */
    public static final String MATCHES_TAG     = "matches";

    /** The single instance of this class. */
    private static StringRangeParser instance;
    
    
    protected StringRangeParser() {
        
    }
    
    public static DefaultRangeParser getInstance() {
        if (instance == null) {
            instance = new StringRangeParser();
        }
        return instance;
    }
    
    @Override
	public Object parse(Node aNode, Report aReport) {
        HashMap theResources = this.getI18N(aNode, aReport);
        if (hasSingleNode(aNode, STARTS_WITH_TAG)) {
            return createStartsWithFilter(aNode, aReport, theResources);
        } 
        else if (hasSingleNode(aNode, MATCHES_TAG)) {
            return createPatternFilter(aNode, aReport, theResources);
        } 
        else {
            return super.parse(aNode, aReport);
        }
    }

    private List createStartsWithFilter(Node aNode, Report aReport, HashMap someResources) {
        String theString = getNodeDataAsString(aNode, STARTS_WITH_TAG);
        Filter theFilter = new StartsWithFilter(theString);
        List   theReturn = new ArrayList(1);
        
        theReturn.add(new FilterEntry(theFilter, aReport.getLanguage(), someResources));
        
        return theReturn;
    }
    
    /**
     * @see #MATCHES_TAG
     */
    private List createPatternFilter(Node aNode, Report aReport, HashMap someResources) {
        String theString = getNodeDataAsString(aNode, MATCHES_TAG);
        Filter theFilter = new PatternFilter(theString);
        List   theReturn = new ArrayList(1);
        
        theReturn.add(new FilterEntry(theFilter, aReport.getLanguage(), someResources));
        
        return theReturn;
    }
}
