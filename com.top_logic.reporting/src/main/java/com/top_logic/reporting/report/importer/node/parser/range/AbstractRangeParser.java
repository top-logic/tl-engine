/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.importer.node.parser.range;

import java.util.HashMap;

import org.w3c.dom.Node;

import com.top_logic.reporting.report.importer.AbstractXMLImporter;
import com.top_logic.reporting.report.importer.node.NodeParser;
import com.top_logic.reporting.report.importer.node.NodeParserManager;
import com.top_logic.reporting.report.importer.node.parser.I18NParser;
import com.top_logic.reporting.report.model.Report;
import com.top_logic.reporting.report.model.ReportConfiguration;
import com.top_logic.reporting.report.model.filter.FilterEntry;
import com.top_logic.reporting.report.xmlutilities.ReportReader;

/**
 * The AbstractRangeParser is the super class for all range parsers. These parsers should be
 * obtained from the {@link RangeParserFactory}.
 * 
 * This Class provides common attributes and methods for all RangeParsers.
 * 
 * It is invoked by a PartitionFunctionParser and parses a &lt;range> tree
 * 
 * @author <a href="mailto:fsc@top-logic.com">Friedemann Schneider</a>
 * @deprecated use {@link ReportReader} and {@link ReportConfiguration}
 */
@Deprecated
public abstract class AbstractRangeParser extends AbstractXMLImporter {
    
    /** 
     * the root tag.
     * MANDATORY 
     */
    public static final String NODE_NAME       = "range";
    
    /** 
     * the root tag.
     * MANDATORY 
     */
    public static final String RANGE_TAG       = "range";
    
    /**
     * the i18n tag
     * this node will be passed to a {@link I18NParser}
     * this node contains i18n names for this range 
     * MANDATORY
     */
    public static final String I18N_TAG        = "i18n";

    /**
	 * This method parses the &lt;range> tree and extracts {@link FilterEntry}s based on the given
	 * option.
	 * 
	 * @param aNode
	 *        should be a &lt;range> node, will fail otherwise
	 * @return List of {@link FilterEntry}, never <code>null</code>
	 * 
	 * @see com.top_logic.reporting.report.importer.node.NodeParser#parse(org.w3c.dom.Node,
	 *      com.top_logic.reporting.report.model.Report)
	 */
    public abstract Object parse(Node aNode, Report aReport);
    
    /**
     * @see #I18N_TAG
     * @return a map of i18n messages
     */
    protected HashMap getI18N(Node aNode, Report aReport) {
        NodeParser theI18NParser = NodeParserManager.getInstance().getParser(I18N_TAG);
        Node theI18NNode         = getSingleNode(aNode, I18N_TAG);
        return (HashMap) theI18NParser.parse(theI18NNode, aReport);
    }
}
