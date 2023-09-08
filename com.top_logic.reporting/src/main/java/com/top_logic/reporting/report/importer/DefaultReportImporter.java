/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.importer;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.element.meta.query.MetaAttributeFilter;
import com.top_logic.knowledge.wrap.mapBasedPersistancy.MapBasedPersistancyFactory;
import com.top_logic.layout.Accessor;
import com.top_logic.reporting.report.exception.ImportException;
import com.top_logic.reporting.report.importer.node.NodeParser;
import com.top_logic.reporting.report.importer.node.NodeParserManager;
import com.top_logic.reporting.report.model.Axis;
import com.top_logic.reporting.report.model.Legend;
import com.top_logic.reporting.report.model.Report;
import com.top_logic.reporting.report.model.ReportConfiguration;
import com.top_logic.reporting.report.model.Title;
import com.top_logic.reporting.report.model.accessor.AccessorFactory;
import com.top_logic.reporting.report.model.partition.function.PartitionFunction;
import com.top_logic.reporting.report.util.ReportConstants;
import com.top_logic.reporting.report.util.ReportUtilities;
import com.top_logic.reporting.report.xmlutilities.ReportReader;

/**
 * The DefaultReportImporter parses report description files (xml) to {@link Report}s.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 * @deprecated use {@link ReportConfiguration} and {@link ReportReader}
 */
@Deprecated
public class DefaultReportImporter extends AbstractXMLImporter implements ReportImporter, ReportConstants {

    /** The delimiter for the different elements in the XPath. */
    private static final String S                           = "/";
    private static final String ROOT_TAG                    = "root";
    private static final String I18N_TAG                    = "i18n";
    private static final String TITLE_TAG                   = "title";
    private static final String SELECTED_LANGUAGE_TAG       = "selected-language";
    private static final String REPORT_TYPE_TAG             = "report-type";
    private static final String PRESENTATION_TYPE_TAG       = "presentation";
    private static final String VISIBILITY_TAG              = "visibility";
    private static final String ALIGN_TAG                   = "align";
    private static final String ORIENTATION_TAG             = "orientation";
    private static final String FONT_TAG                    = "font";
    private static final String NAME_TAG                    = "name";
    private static final String STYLE_TAG                   = "style";
    private static final String SIZE_TAG                    = "size";
    private static final String LEGEND_TAG                  = "legend";
    private static final String ANTI_ALIAS_TAG              = "anti-alias";
    private static final String RANGE_AXIS_TAG              = "range-axis";
    private static final String DOMAIN_AXIS_TAG             = "domain-axis";
    private static final String GRID_LINE_TAG               = "grid-line";
    private static final String BACKGROUND_COLOR_TAG        = "background-color";
    private static final String VALUE_FONT_TAG              = "values";
    private static final String RELEVANT_BUSINESS_OBJS_TAG  = "relevant-business-objects";
    private static final String META_ATTR_FILTERS_TAG       = "meta-attribute-filters";
    private static final String BUSINESS_OBJECT_PRODUCER_TAG= "business-object-producer";
    private static final String ACCESSOR_CLASS_TAG          = "class";
    private static final String USE_SAME_CATEGORY_COLOR_TAG = "use-same-category-color";
    private static final String USE_GRADIENT_PAINT_TAG      = "use-gradient-paint";
    private static final String USE_INTS_FOR_RANGE_AXIS_TAG = "use-ints-for-range-axis";
    private static final String CATEGORY_TAG                = "categories";
    private static final String MAIN_TAG                    = "main-category";
    private static final String SUB_TAG                     = "sub-category";
    private static final String CATEGORY_FUNCTION_TAG       = "category-function";
    private static final String CATEGORY_FUNCTION_TYPE_TAG  = "type";
    private static final String SHOW_ITEM_LABELS            = "show-item-labels";

    /** The single instance of this component. */
    public static final DefaultReportImporter INSTANCE = new DefaultReportImporter();

    private DefaultReportImporter() {
        // Do nothing.
    }

    @Override
	public Report importReportFrom(BinaryData aReportDescription) throws ImportException {
        Report   theReport    = new Report();
        Document theDocument  = parseDocument(aReportDescription);

        parseAndSetBusinessObjectProducer(theDocument, theReport);
        parseAndSetBackgroundColor(theDocument, theReport);
        parseAndSetAntiAlias(theDocument, theReport);
        parseAndSetTitles(theDocument, theReport);
        parseAndSetSelectedLanguage(theDocument, theReport);
        parseAndSetRenderer(theDocument, theReport);
        parseAndSetOrientations(theDocument, theReport);
        parseAndSetLegend(theDocument, theReport);
        parseAndSetRangeAxis(theDocument, theReport);
        parseAndSetDomainAxis(theDocument, theReport);
        parseAndSetFilters(theDocument, theReport);
        parseAndSetSameCategoryColorNode(theDocument, theReport);
        parseAndSetUseGradientPaintNode(theDocument, theReport);
        parseAndSetUseIntsForRangeAxisNode(theDocument, theReport);
        parseAndSetShowItemLabelsNode(theDocument, theReport);
        
        parseAndSetCategories(theDocument, theReport);
        
        return theReport;
    }

    private void parseAndSetShowItemLabelsNode(Document aDocument, Report aReport) {
         Node theShowItemLabelsNode = getShowItemLabelsNode(aDocument);
         
         if (!exists(theShowItemLabelsNode)) { return; }
         
         aReport.setShowItemLabels(getDataAsBoolean(theShowItemLabelsNode));
    }

    private void parseAndSetCategories(Document aDocument, Report aReport) {
        
        Node theMainNode                     = getCategoryMainNode(aDocument);
        Node theCategoryFunctionNode         = getSingleNode(theMainNode, CATEGORY_FUNCTION_TAG);
        PartitionFunction theCategoryFunction = getCategoryFunction(theCategoryFunctionNode, aReport);
//        theCategoryFunction.setRangeFunction(theRangeFunction);
        
        
//        if (!theRangeFunction.hasAttributeAccessor()) {
//            theRangeFunction.setAttributeAccessor(theGlobalAccessor);
//        }
//        if (!theCategoryFunction.hasAttributeAccessor()) {
//            theCategoryFunction.setAttributeAccessor(theGlobalAccessor);
//        }
        aReport.setCategoryMainFunction(theCategoryFunction);
        
        try {
            Node theSubNode         = getCategorySubNode(aDocument);
            if (theSubNode != null) {
                theCategoryFunctionNode = getSingleNode(theSubNode, CATEGORY_FUNCTION_TAG);
                theCategoryFunction     = getCategoryFunction(theCategoryFunctionNode, aReport);
//                theCategoryFunction.setRangeFunction(theRangeFunction);
                
//                if (!theRangeFunction.hasAttributeAccessor()) {
//                    theRangeFunction.setAttributeAccessor(theGlobalAccessor);
//                }
//                if (!theCategoryFunction.hasAttributeAccessor()) {
//                    theCategoryFunction.setAttributeAccessor(theGlobalAccessor);
//                }
                
                aReport.setCategorySubFunction(theCategoryFunction);
            }
        } catch (ImportException iex) {
            //
        }
    }

    private PartitionFunction getCategoryFunction(Node aNode, Report aReport) {
        
        String     theType   = getNodeDataAsString(aNode, CATEGORY_FUNCTION_TYPE_TAG);
        NodeParser theParser = NodeParserManager.getInstance().getParser(theType);
        
        return (PartitionFunction) theParser.parse(aNode, aReport);
    }
    
    public Accessor getAccessor(Node aNode) {
        String theClass  = getNodeDataAsString(aNode, ACCESSOR_CLASS_TAG);
        
        return AccessorFactory.INSTANCE.getAccessor(theClass, false);
    }
    
    private void parseAndSetSameCategoryColorNode(Document aDocument, Report aReport) {
        Node theSameColorNode = getSameCategoryColorNode(aDocument);
            
        if(!exists(theSameColorNode)) { return; }
        
        aReport.setUseSameCategoryColor(getDataAsBoolean(theSameColorNode));
    }
    
    private void parseAndSetUseIntsForRangeAxisNode(Document aDocument, Report aReport) {
        Node theIntsForRangeAxisNode = getUseIntsForRangeAxisNode(aDocument);
            
        if(!exists(theIntsForRangeAxisNode)) { return; }
        
        aReport.setUseIntValuesForRangeAxis(getDataAsBoolean(theIntsForRangeAxisNode));
    }
    
    private void parseAndSetUseGradientPaintNode(Document aDocument, Report aReport) {
        Node theGradientPaintNode = getGradientPaintNode(aDocument);
            
        if(!exists(theGradientPaintNode)) { return; }
        
        aReport.setUseGradientPaint(getDataAsBoolean(theGradientPaintNode));
    }

    private void parseAndSetBusinessObjectProducer(Document aDocument, Report aReport) {
        String theXPath = S + ROOT_TAG + S + BUSINESS_OBJECT_PRODUCER_TAG;
        
        if (hasSingleNode(aDocument, theXPath)) {
            
        }
    }

    private void parseAndSetFilters(Document aDocument, Report aReport) {
        Node theFiltersNode = getFiltersNode(aDocument);
        
        if (!exists(theFiltersNode)) {
            return;
        }
        
        List theFilterNodeList = getChildElementNodes(theFiltersNode);
        for (Iterator iter = theFilterNodeList.iterator(); iter.hasNext();) {
            Node theFilterNode = (Node) iter.next();
            
            MapBasedPersistancyFactory theFactory        = MapBasedPersistancyFactory.getInstance();
            Map                        theEntryDataAsMap = getEntryDataAsMap(theFilterNode);
            MetaAttributeFilter        theFilter         = (MetaAttributeFilter) theFactory.getObject(theEntryDataAsMap);
            aReport.addFilter(theFilter);
        }
        
    }

    private void parseAndSetBackgroundColor(Document aDocument, Report aReport) {
        Node theBackgroundNode = getBackgroundColorNode(aDocument);
        
        if(exists(theBackgroundNode)) {
            Color theBGColor = getDataAsColor(theBackgroundNode);
            aReport.setChartBackground(theBGColor);
        }
    }

    private void parseAndSetRangeAxis(Document aDocument, Report aReport) {
        Node theRangeAxisNode = getRangeAxisNode(aDocument);
        
        if (!exists(theRangeAxisNode)) { return; }
        
        Axis theRangeAxis = extractAxis(theRangeAxisNode);
        if(theRangeAxis != null) {
            aReport.setRangeAxis(theRangeAxis);
        }
    }

    private void parseAndSetDomainAxis(Document aDocument, Report aReport) {
        Node theDomainAxisNode = getDomainAxisNode(aDocument);
        
        if (!exists(theDomainAxisNode)) { return; }
        
        Axis theDomainAxis = extractAxis(theDomainAxisNode);
        if(theDomainAxis != null) {
            aReport.setDomainAxis(theDomainAxis);
        }
    }
    
    private Axis extractAxis(Node aAxisNode) {
        Axis theAxis = new Axis();
        
        // Messages
        Node theI18NNode = getSingleNode(aAxisNode, I18N_TAG);
        if (exists(theI18NNode)) {
            theAxis.setMessages((HashMap) getI18NHashMap(theI18NNode));
        }
        
        // Visibility
        Node theVisibilityNode = getSingleNode(aAxisNode, VISIBILITY_TAG);
        if (exists(theVisibilityNode)) {
            boolean  theVisibility = getNodeDataAsBoolean(aAxisNode, VISIBILITY_TAG);
            theAxis.setVisible(theVisibility);
        }
        
        // Grid line
        Node theGridLineNode1 = getSingleNode(aAxisNode, GRID_LINE_TAG);
        if (exists(theGridLineNode1)) {
            boolean theGridLine = getNodeDataAsBoolean(aAxisNode, GRID_LINE_TAG);
            theAxis.setGridline(theGridLine);
        }
        
        // Font
        Node theFontNode = getSingleNode(aAxisNode, FONT_TAG);
        if(theFontNode != null) {
            theAxis.setFont(parseFont(theFontNode));
        }
        
        // Value font
        Node theValueFontNode = getSingleNode(aAxisNode, VALUE_FONT_TAG + S + FONT_TAG);
        if(theValueFontNode != null) {
            theAxis.setValueFont(parseFont(theValueFontNode));
        }
        
        return theAxis;
    }

    private Object getI18NHashMap(Node anI18NNode) {
        return NodeParserManager.getInstance().getParser(anI18NNode.getNodeName()).parse(anI18NNode, null);
    }
    
    private void parseAndSetAntiAlias(Document aDocument, Report aReport) {
        Node theAntiAliasNode = getAntiAliasNode(aDocument);
        
        if (exists(theAntiAliasNode)) {
            boolean isAntiAlias = getDataAsBoolean(theAntiAliasNode);
            aReport.setAntiAlias(isAntiAlias);
        }
    }

    private void parseAndSetLegend(Document aDocument, Report aReport) {
        Node theLegendNode = getLegendNode(aDocument);
        
        if(!exists(theLegendNode)) { return; }

        Legend theLegend = new Legend();
        
        // Visibility
        Node theVisibilityNode = getSingleNode(theLegendNode, VISIBILITY_TAG);
        if (exists(theVisibilityNode)) {
            boolean  theVisibility = getNodeDataAsBoolean(theLegendNode, VISIBILITY_TAG);
            theLegend.setVisible(theVisibility);
        }
        
        // Alignment
        Node theAlignNode = getSingleNode(theLegendNode, ALIGN_TAG);
        if (exists(theAlignNode)) {
            String theAlign = getNodeDataAsString (theLegendNode, ALIGN_TAG);
            theLegend.setAlign(theAlign);
        }
        
        // Font
        Node theFontNode = getSingleNode(theLegendNode, FONT_TAG);
        if(theFontNode != null) {
            theLegend.setFont(parseFont(theFontNode));
        }
        
        aReport.setLegend(theLegend);
    }

    private void parseAndSetOrientations(Document aDocument, Report aReport) {
        Node     thePresentationNode     = getRepresentationNode(aDocument);
        List     theSupportedReportTypes = ReportUtilities.getSupportedReportTypes();
        Iterator theIter                 = theSupportedReportTypes.iterator(); 
        while(theIter.hasNext()) {
            String theReportType      = (String) theIter.next();
            Node   theOrientationNode = getSingleNode(thePresentationNode, theReportType + S + ORIENTATION_TAG);
            
            if(theOrientationNode == null) { continue; }
            
            aReport.setOrientation(theReportType, getDataAsString(theOrientationNode));
        }
    }

    private void parseAndSetRenderer(Document aDocument, Report aReport) throws ImportException {
        Node   theReportTypeNode = getSingleNode(aDocument, S + ROOT_TAG + S + REPORT_TYPE_TAG);
        String theReportType     = getDataAsString(theReportTypeNode);
        
        aReport.setProducer(ReportUtilities.getRendererFor(theReportType));
    }

    private void parseAndSetSelectedLanguage(Document aDocument, Report aReport) {
        Node   theSelectedLanguageNode = getSingleNode(aDocument, S + ROOT_TAG + S + SELECTED_LANGUAGE_TAG);
        String theSelectedLanguage     = getDataAsString(theSelectedLanguageNode);
        
        aReport.setLanguage(theSelectedLanguage);
    }

    private void parseAndSetTitles(Document aDocument, Report aReport) {
        Node     thePresentationNode     = getRepresentationNode(aDocument);
        List     theSupportedReportTypes = ReportUtilities.getSupportedReportTypes();
        Iterator theIter                 = theSupportedReportTypes.iterator(); 
        while(theIter.hasNext()) {
            String theReportType = (String) theIter.next();
            
            Node theTitleNode = getSingleNode(thePresentationNode, theReportType + S + TITLE_TAG);
            if (!exists(theTitleNode)) { continue; }

            Title theTitle = new Title();
            
            // Visibility
            Node theVisibilityNode = getSingleNode(theTitleNode, VISIBILITY_TAG);
            if (exists(theVisibilityNode)) {
                boolean  theVisibility = getNodeDataAsBoolean(theTitleNode, VISIBILITY_TAG);
                theTitle.setVisible(theVisibility);
            }

            // Alignment
            Node theAlignNode = getSingleNode(theTitleNode, ALIGN_TAG);
            if (exists(theAlignNode)) {
                String theAlign = getNodeDataAsString (theTitleNode, ALIGN_TAG);
                theTitle.setAlign(theAlign);
            }
            
            // I18N messages
            setTitleMessages(aDocument, theReportType, theTitle);
            
            // Font
            Node theFontNode = getSingleNode(theTitleNode, FONT_TAG);
            if(theFontNode != null) {
                theTitle.setFont(parseFont(theFontNode));
            }
            
            // Add the title to the report
            aReport.setTitle(theReportType, theTitle);
        }
    }

    private void setTitleMessages(Document aDocument, String theType, Title theTitle) {
        Node     theI18NNode  = getSingleNode(aDocument, S + ROOT_TAG + S + PRESENTATION_TYPE_TAG + S + theType + S + TITLE_TAG + S + I18N_TAG);
        NodeList theLanguages = theI18NNode.getChildNodes();
        for (int i = 0; i < theLanguages.getLength(); i++ ) {
            Node theNode = theLanguages.item(i);
            
            if (!(theNode.getNodeType() == Node.ELEMENT_NODE)) { continue; }
            
            String theLanguage     = theNode.getNodeName();
            String theTitleMessage = getDataAsString(theNode);
            theTitle.setMessage(theLanguage, theTitleMessage);
            
        }
    }
    
    private Font parseFont(Node aNode) {
        String theName  = getNodeDataAsString(aNode, NAME_TAG);
        int    theStyle = ReportUtilities.getFontStyle(getNodeDataAsString(aNode, STYLE_TAG));
        int    theSize  = getNodeDataAsInt   (aNode, SIZE_TAG);
        
        return new Font(theName, theStyle, theSize);
    }
    
    private boolean exists(Node aNode) {
        return aNode != null;
    }
    
    private Node getCategoryMainNode(Document aDocument) {
        return getSingleNode(aDocument, S + ROOT_TAG + S + CATEGORY_TAG + S + MAIN_TAG);
    }
    
    private Node getCategorySubNode(Document aDocument) {
        return getSingleNode(aDocument, S + ROOT_TAG + S + CATEGORY_TAG + S + SUB_TAG);
    }
    
    private Node getFiltersNode(Document aDocument) {
        return getSingleNode(aDocument, S + ROOT_TAG + S + RELEVANT_BUSINESS_OBJS_TAG + S + META_ATTR_FILTERS_TAG);
    }
    
    private Node getAntiAliasNode(Document aDocument) {
        return getSingleNode(aDocument, S + ROOT_TAG + S + PRESENTATION_TYPE_TAG + S + REPORT_TYPE_CHART + S + ANTI_ALIAS_TAG);
    }
    
    private Node getShowItemLabelsNode(Document aDocument) {
        return getSingleNode(aDocument, S + ROOT_TAG + S + PRESENTATION_TYPE_TAG + S + REPORT_TYPE_CHART + S + SHOW_ITEM_LABELS);
    }
    
    private Node getSameCategoryColorNode(Document aDocument) {
        return getSingleNode(aDocument, S + ROOT_TAG + S + PRESENTATION_TYPE_TAG + S + REPORT_TYPE_CHART + S + USE_SAME_CATEGORY_COLOR_TAG);
    }
    
    private Node getUseIntsForRangeAxisNode(Document aDocument) {
        return getSingleNode(aDocument, S + ROOT_TAG + S + PRESENTATION_TYPE_TAG + S + REPORT_TYPE_CHART + S + USE_INTS_FOR_RANGE_AXIS_TAG);
    }
    
    private Node getGradientPaintNode(Document aDocument) {
        return getSingleNode(aDocument, S + ROOT_TAG + S + PRESENTATION_TYPE_TAG + S + REPORT_TYPE_CHART + S + USE_GRADIENT_PAINT_TAG);
    }
    
    private Node getBackgroundColorNode(Document aDocument) {
        return getSingleNode(aDocument, S + ROOT_TAG + S + PRESENTATION_TYPE_TAG + S + REPORT_TYPE_CHART + S + BACKGROUND_COLOR_TAG);
    }
    
    private Node getRangeAxisNode(Document aDocument) {
        return getSingleNode(aDocument, S + ROOT_TAG + S + PRESENTATION_TYPE_TAG + S + REPORT_TYPE_CHART + S + RANGE_AXIS_TAG);
    }
    
    private Node getDomainAxisNode(Document aDocument) {
        return getSingleNode(aDocument, S + ROOT_TAG + S + PRESENTATION_TYPE_TAG + S + REPORT_TYPE_CHART + S + DOMAIN_AXIS_TAG);
    }
    
    private Node getLegendNode(Document aDocument) {
        return getSingleNode(aDocument, S + ROOT_TAG + S + PRESENTATION_TYPE_TAG + S + REPORT_TYPE_CHART + S + LEGEND_TAG);
    }
    
    private Node getRepresentationNode(Document aDocument) {
        return getSingleNode(aDocument, S + ROOT_TAG + S + PRESENTATION_TYPE_TAG);
    }
    
}

