/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.importer.node.parser.category;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Node;

import com.top_logic.layout.Accessor;
import com.top_logic.reporting.report.importer.AbstractXMLImporter;
import com.top_logic.reporting.report.importer.DefaultReportImporter;
import com.top_logic.reporting.report.importer.node.NodeParser;
import com.top_logic.reporting.report.importer.node.parser.range.AbstractRangeParser;
import com.top_logic.reporting.report.importer.node.parser.range.DefaultRangeParser;
import com.top_logic.reporting.report.importer.node.parser.range.RangeParserFactory;
import com.top_logic.reporting.report.model.Report;
import com.top_logic.reporting.report.model.ReportConfiguration;
import com.top_logic.reporting.report.model.accessor.AccessorFactory;
import com.top_logic.reporting.report.model.filter.FilterEntry;
import com.top_logic.reporting.report.xmlutilities.ReportReader;

/**
 * The AbstractPartitionFunctionParser is the super class for all partition function parsers.
 * It provides common attributes and methods.
 * It parses the &lt;category-function> tree  
 *  
 * Example XML:
 * <xmp>
 * <category-function>
 *      <type>date</type>
 *      <attribute>startDateOfProject</attribute>
 *      <value-accessor>default</value-accessor>
 *      <ignore-null-values>true</ignore-null-values>
 *      <ignore-empty-categories>false</ignore-empty-categories>
 *      <ranges>
 *          <!-- range is parsed by AbstractRangeParser -->
 *          <range>
 *              <i18n>
 *                  <de></de>
 *                  <en></en>
 *                  <begin includeBorder="true">currentYear -2</begin>
 *                  <end   includeBorder="false">currentYear -1</end>
 *                  <granularity>month</granularity>
 *              </i18n>
 *          </range>  
 *          <!-- range is parsed by AbstractRangeParser -->    
 *      </ranges>
 * </category-function>
 * </xmp>
 * 
 * @author     <a href="mailto:fsc@top-logic.com">Friedemann Schneider</a>
 * @deprecated use {@link ReportReader} and {@link ReportConfiguration}
 */
@Deprecated
public abstract class AbstractPartitionFunctionParser extends AbstractXMLImporter implements NodeParser {

    /** 
     * the root tag.
     * MANDATORY 
     */
    public final static String PARTITION_FUNCTION_TAG = "category-function";
    
    /**
     * The tag for the attribute.
     * The CategoryFunction will create the categories based on the values of this attribute.
     * The attribute-values will be obtained throug the defined value-accessor.
     * MANDATORY 
     */
    public final static String ATTRIBUTE_TAG         = "attribute";
    
    /** 
     * The tag for the funtion type. 
     * Its value determins the function 
     */
    public final static String TYPE_TAG              = "type";
    
    /** 
     * the ranges tag. 
     * Within this tag the user can provide different manually defined
     * ranges/categories
     * @see DefaultRangeParser
     */
    public final static String RANGES_TAG            = "ranges";
    
    /**
     * This tag set a flag to the CategoryFunction
     * Default value is <code>false</code>
     */
    public final static String IGNORE_NULL_TAG       = "ignore-null-values";
    
    /**
     * This tag set a flag to the CategoryFunction
     * Its used later on by the {@link Report#getPartitions()}
     * Default value is <code>false</code>
     */
    public final static String IGNORE_PARTITIONS_TAG = "ignore-empty-categories";
    
    /**
     * The tag for the value-accessor.
     * The value is passed to the CategoryFunction and to the {@link AccessorFactory}
     * to obtain an {@link Accessor} for the attribute values.
     * The value should be a full qualified class name. 
     * This option overwrites a globally defined value accessor.
     */
    public final static String ACCESSOR_TAG          = "accessor";
    
    public final static String DECIMAL_FORMAT_TAG    = "decimal-format";
    
    /** local holders for the options */
    protected boolean ignoreNullValues;
    protected String  attribute;
    protected String  type;
    protected Accessor accessor;
    protected String  decimalFormat;
    
    /**
	 * This method parses the &lt;category-funtion> tree and extracts the mandatory values Each
	 * subclass should call this method within its own {@link #parse(Node, Report)}-method
	 * 
	 * @param aNode
	 *        should be the &lt;category-funtion> node, will fail otherwise
	 * @return always true
	 * 
	 * @see com.top_logic.reporting.report.importer.node.NodeParser#parse(org.w3c.dom.Node,
	 *      com.top_logic.reporting.report.model.Report)
	 */
	@Deprecated
	@Override
	public Object parse(Node aNode, Report aReport) {
        this.attribute          = getAttributeName(aNode);
        this.type               = getType(aNode);
        this.ignoreNullValues   = getIgnoreNullValues(aNode, false);
        this.accessor           = getAccessor(aNode);
        this.decimalFormat      = getDecimalFormat(aNode);
        return Boolean.TRUE;
    }

    /**
     * @see #ATTRIBUTE_TAG
     */
    protected String getAttributeName(Node aNode) {
        return getNodeDataAsString(aNode, ATTRIBUTE_TAG); 
    }
    
    /**
     * @see #ACCESSOR_TAG
     */
    protected Accessor getAccessor(Node aNode) {
        try {
            return DefaultReportImporter.INSTANCE.getAccessor(getSingleNode(aNode, ACCESSOR_TAG));
        }
        catch (Exception ex){
            // its ok, will return default
        }
        return null;
    }
    
    protected String getDecimalFormat(Node aNode) {
        try {
            return getNodeDataAsString(aNode, DECIMAL_FORMAT_TAG);
        } catch (Exception ex) {
            // TODO FSC care about the Exception !
        }
        return null;
    }
    
    /**
	 * This method parses the {@link #RANGES_TAG} subtree and creates a list of {@link FilterEntry}s
	 * for each found &lt;range> tag. Therefore it passes the &lt;range>-subtree to a RangeParser
	 * obtained from the {@link RangeParserFactory}.
	 * 
	 * @param aNode
	 *        should be the &lt;ranges> node, will fail otherwise
	 * @return list of {@link FilterEntry}
	 */
    protected List getRangeEntries(Node aNode, Report aReport) {
        if (hasSingleNode(aNode, RANGES_TAG)) {
            Node theRangesNode = getSingleNode(aNode, RANGES_TAG);
            
            ArrayList theReturn  = new ArrayList();
            List theElementNodes = getChildElementNodes(theRangesNode);
            
            for (Iterator theIt = theElementNodes.iterator(); theIt.hasNext();) {
                Node theNode = (Node) theIt.next();
                
                if (theNode.getNodeName().equalsIgnoreCase(AbstractRangeParser.RANGE_TAG)) {
                    NodeParser theParser = RangeParserFactory.getInstance().getParser(this.type);
                    Object theFilterEntries = theParser.parse(theNode, aReport);
                    if (theFilterEntries != null) {
                        List theList = (List) theFilterEntries;
                        theReturn.addAll(theList);
                    }
                }
            }
            
            return theReturn;
        }
        return new ArrayList(0);
    }
    
    /**
     * @see #IGNORE_NULL_TAG
     */
    protected boolean getIgnoreNullValues(Node aNode, boolean aDefault) {
        try {
            return getNodeDataAsBoolean(aNode, IGNORE_NULL_TAG);
        }
        catch (Exception ex){
            // its ok, will return default
        }
        return aDefault;
    }
    
    /**
     * @see #IGNORE_PARTITIONS_TAG 
     */
    protected boolean getIgnoreEmptyCategories(Node aNode, boolean aDefault) {
        try {
            return getNodeDataAsBoolean(aNode, IGNORE_PARTITIONS_TAG);
        }
        catch (Exception ex){
           // its ok, will return default
        }
        return aDefault;
    }
    
    protected String getType(Node aNode) {
        return getNodeDataAsString(aNode, TYPE_TAG);
    }
}
