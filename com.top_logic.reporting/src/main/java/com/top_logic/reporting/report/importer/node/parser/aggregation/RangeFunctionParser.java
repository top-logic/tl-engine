/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.importer.node.parser.aggregation;

import org.w3c.dom.Node;

import com.top_logic.layout.Accessor;
import com.top_logic.reporting.report.importer.AbstractXMLImporter;
import com.top_logic.reporting.report.importer.DefaultReportImporter;
import com.top_logic.reporting.report.model.Report;
import com.top_logic.reporting.report.model.ReportConfiguration;
import com.top_logic.reporting.report.model.accessor.AccessorFactory;
import com.top_logic.reporting.report.model.aggregation.AggregationFunction;
import com.top_logic.reporting.report.model.aggregation.AggregationFunctionFactory;
import com.top_logic.reporting.report.xmlutilities.ReportReader;

/**
 * The range funtion parser is used to parse the &lt;aggregation-funtion&gt; tree  
 * 
 * @author     <a href="mailto:fsc@top-logic.com">Friedemann Schneider</a>
 * @deprecated use {@link ReportReader} and {@link ReportConfiguration}
 */
@Deprecated
public class RangeFunctionParser extends AbstractXMLImporter {

    /** 
     * the root tag.
     * MANDATORY 
     */
    public final static String AGGREGATE_FUNCTION_TAG   = "aggregation-function";
    
    /** 
     * The tag for the funtion type. 
     * Its value determins the function to get from the {@link AggregationFunctionFactory}
     * MANDATORY 
     */
    public final static String FUNTION_TYPE_TAG         = "type";
    
    
    /**
     * The tag for the attribute.
     * The RangeFunction will caluclate with the values of this attribute.
     * The attribute-values will be obtained throug the defined value-accessor.
     * MANDATORY 
     */
    public final static String ATTRIBUTE_TYPE_TAG       = "attribute";

    /**
     * The tag for the value-accessor.
     * The value is passed to the RangeFunction and to the {@link AccessorFactory}
     * to obtain an {@link Accessor} for the attribute values.
     * The value should be a full qualified class name. 
     * This option overwrites a globally defined value accessor.
     */
    public final static String ACCESSOR_TAG             = "accessor";
    
    /**
     * This tag set a flag to the RangeFunction
     * Default value is <code>false</code>
     */
    public final static String IGNORE_NULL_TAG          = "ignore-null-values";
    
    /**
     * Singleton
     */
    public static final RangeFunctionParser INSTANCE = new RangeFunctionParser();
    
    /**
     * C'Tor 
     */
    private RangeFunctionParser() {
        
    }
    
    /**
     * This method parses the &lt;aggregation-funtion&gt; tree and created a RangeFunction
     * 
     * @param aNode   should be the &lt;aggregation-funtion&gt; node, will fail otherwise
     * @return a {@link AggregationFunction}
     * 
     * @see com.top_logic.reporting.report.importer.node.NodeParser#parse(org.w3c.dom.Node, com.top_logic.reporting.report.model.Report)
     */
    public Object parse(Node aNode, Report aReport) {

        String theType              = getNodeDataAsString(aNode, FUNTION_TYPE_TAG);
        String theAttribute         = getNodeDataAsString(aNode, ATTRIBUTE_TYPE_TAG);
//        NumberFormat theFormat      = getFormat(aNode);
        
        
        AggregationFunction theFunction   = AggregationFunctionFactory.getInstance().getFunction(theType, theAttribute);
        theFunction.setIgnoreNullValues(this.getIgnoreNullValues(aNode, false));
        theFunction.setAttributeAccessor(this.getAccessor(aNode));
//        theFunction.setFormat(theFormat);
        return theFunction;
    }

    protected boolean getIgnoreNullValues(Node aNode, boolean aDefault) {
        try {
            return getNodeDataAsBoolean(aNode, IGNORE_NULL_TAG);
        }
        catch (Exception ex){
            // its ok, will return default
        }
        return aDefault;
    }
    
    protected Accessor getAccessor(Node aNode) {
        try {
            return DefaultReportImporter.INSTANCE.getAccessor(getSingleNode(aNode, ACCESSOR_TAG));
        }
        catch (Exception ex){
            // its ok, will return default
        }
        return null;
    }
    
    
//    protected NumberFormat getFormat(Node aNode) {
//        try {
//            String theString = getNodeDataAsString(aNode, FORMAT_TAG);
//            NumberFormat theFormat = new DecimalFormat(theString);
//            return theFormat;
//        }
//        catch (Exception ex){
//            // its ok, will return default
//        }
//        return null;
//    }
}
