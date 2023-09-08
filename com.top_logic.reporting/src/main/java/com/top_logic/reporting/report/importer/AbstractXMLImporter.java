/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.importer;

import java.awt.Color;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.reporting.report.exception.ImportException;
import com.top_logic.reporting.report.importer.entry.Entry;
import com.top_logic.reporting.report.importer.entry.EntryParser;
import com.top_logic.reporting.report.importer.entry.EntryParserManager;
import com.top_logic.reporting.report.importer.node.parser.DateTokenHandler;
import com.top_logic.reporting.report.model.ReportConfiguration;
import com.top_logic.reporting.report.util.ReportUtilities;
import com.top_logic.reporting.report.xmlutilities.ReportReader;

/**
 * The AbstractXMLImporter is an abstract class to load and parse xml files. 
 *  
 * This AbstractXMLImporter provides useful method to get nodes, node lists or 
 * extract data from nodes or attributes.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 * @deprecated use {@link ReportReader} and {@link ReportConfiguration}
 */
@Deprecated
public abstract class AbstractXMLImporter {

    // Load document methods

    
    /**
	 * This method parses the given xml file and returns it.
	 * 
	 * @param aReportDescription
	 *        A xml file. Must not be <code>null</code>.
	 */
	protected Document parseDocument(BinaryData aReportDescription) throws ImportException {
		try (InputStream in = aReportDescription.getStream()) {
			DocumentBuilder theBuilder = DOMUtil.newDocumentBuilder();
			Document theDocument = theBuilder.parse(in);
            return theDocument;
//            return theBuilder.parse(aFile);
        }
        catch (Exception e) {
			throw new ImportException(this.getClass(),
				"Could not parse the file '" + aReportDescription.getName() + "'.", e);
        }
    }
    
    // Select node or node list methods
    
    /**
     * This method returns a {@link Node} which is selected by the given xpath
     * for the document.
     * 
     * @param aDocument
     *        The document. Must not be <code>null</code>.
     * @param aXPath
     *        A valid XPath string.
     */
    protected Node getSingleNode(Document aDocument, String aXPath) throws ImportException {
        return getSingleNode(aDocument.getDocumentElement(), aXPath);
    }
    
    /**
     * This method returns a {@link Node} which is selected by the given xpath
     * for the context node.
     * 
     * @param aXPath
     *        A valid XPath string.
     */
    protected Node getSingleNode(Node aContextNode, String aXPath) {
        try {
            return DOMUtil.selectSingleNode(aContextNode, aXPath);
        }
        
        catch (XPathExpressionException xe) {
            throw new ImportException(this.getClass(), "Could not get the single node for the xpath '" + aXPath + "'.", xe);
        }
    }
    
    /**
     * This method checks the existance of a node identified by the given path
     * 
     * @param aContextNode
     *          A node to query the xpath from.
     * @param aXPath
     *          A XPath string.
     * @return <code>true</code> if the node exists, false otherwise.
     */
    protected boolean hasSingleNode(Node aContextNode, String aXPath) {
        try {
            return (getSingleNode(aContextNode, aXPath) != null);
        } catch (Exception ex) {
            // its ok, node not present, return false
        }
        return false;
    }
    
    /**
     * This method returns a {@link NodeList} which is selected by the given
     * xpath for the document.
     * 
     * @param aDocument
     *        The document. Must not be <code>null</code>.
     * @param aXPath
     *        A valid XPath string.
     */
    protected NodeList getNodeList(Document aDocument, String aXPath) throws ImportException {
       return getNodeList(aDocument.getDocumentElement(), aXPath);
    }
    
    /**
     * This method returns a {@link NodeList} which is selected by the given
     * xpath for the context node.
     * 
     * @param aNode
     *        The document. Must not be <code>null</code>.
     * @param aXPath
     *        A valid XPath string.
     */
    protected NodeList getNodeList(Node aNode, String aXPath) throws ImportException {
        try {
            return DOMUtil.selectNodeList(aNode, aXPath);
        }
        
        catch (XPathExpressionException xe) {
        	throw new ImportException(this.getClass(), "Could not get the node list for the xpath '" + aXPath + "'.", xe);
        }
    }
    
    /**
     * Returns a list with the child element nodes of the given node or an empty
     * list but never null.
     * 
     * @param aNode
     *        The node. Must not be <code>null</code>.
     */
    public List getChildElementNodes(Node aNode) {
        ArrayList theResult = new ArrayList();
        
        NodeList theChildNodes = aNode.getChildNodes();
        for (int i = 0; i < theChildNodes.getLength(); i++) {
            Node theNode = theChildNodes.item(i);
            
            if (theNode.getNodeType() != Node.ELEMENT_NODE) { continue; }
            
            theResult.add(theNode);
        }
        
        return theResult;
    }
    
    // Extract node data
    
    /** 
     * Returns a map which contains all entry values of the given node.
     * 
     * @param aEntryNode A entry node. 
     * E.g.
     *       <entryNode>
     *           <string  key="class" value="java.util.Date" />
     *           <boolean key="neg"   value="true" />
     *           <date    key="val1"  value="01.01.2005" />
     *       </entryNode>
     *            
     *                   key    value           value type
     *       map entry  class   java.util.Date   String
     *       map entry  neg     true             Boolean
     *       map entry  val1    01.01.2005       Date
     */
    protected Map getEntryDataAsMap(Node aEntryNode) {
        HashMap theDataMap = new HashMap();
        
        List theEntryNodes = getChildElementNodes(aEntryNode);
        for (Iterator theIter = theEntryNodes.iterator(); theIter.hasNext();) {
            Node   theEntryNode = (Node) theIter.next();
            String theTagName   = theEntryNode.getNodeName();
            
            EntryParser theParser = EntryParserManager.getInstance().getEntryParser(theTagName);
            Entry       theEntry  = theParser.parse(theEntryNode);
            theDataMap.put(theEntry.getKey(), theEntry.getValue());
        }
        
        return theDataMap;
    }
    
    /**
     * This method returns the node data (trimmed) of the first text node which
     * is found or an empty string.
     * 
     * @param aNode
     *        A node. Must not be <code>null</code>.
     */
    protected String getDataAsString(Node aNode) {
        NodeList theList = aNode.getChildNodes();
        for (int i = 0; i < theList.getLength(); i++) {
            Node theNode = theList.item(i);
            if (theNode.getNodeType() == Node.TEXT_NODE) {
                return theNode.getNodeValue().trim();
            }
        }
        
        return "";
    }

    /**
     * This method returns the node data (trimmed) as Color of the first text
     * node which is found.
     * 
     * @param aNode
     *        A node. Must not be <code>null</code>.
     */
    protected Color getDataAsColor(Node aNode) {
        String theColorString = getDataAsString(aNode);
        
        // Use theme background color
        if (theColorString.equalsIgnoreCase("theme")) {
            return ReportUtilities.getThemeBackgroundColor();
        }
        
        return Color.decode(theColorString);
    }
    
    /**
     * This method returns the node data (trimmed) as boolean of the first text
     * node which is found.
     * 
     * @param aNode
     *        A node. Must not be <code>null</code>.
     */
    protected boolean getDataAsBoolean(Node aNode) {
        return Boolean.valueOf(getDataAsString(aNode)).booleanValue();
    }
    
    /**
     * This method returns the node data (trimmed) as int of the first text
     * node which is found.
     * 
     * @param aNode
     *        A node. Must not be <code>null</code>.
     */
    protected int getDataAsInt(Node aNode) {
        return Integer.parseInt(getDataAsString(aNode));
    }
    
    /**
     * This method returns the node data (trimmed) as double of the first text
     * node which is found.
     * 
     * @param aNode
     *        A node. Must not be <code>null</code>.
     */
    protected double getDataAsDouble(Node aNode) {
        return Double.parseDouble(getDataAsString(aNode));
    }

    /**
     * The xpath (with the context node as prefix) must select one node. For
     * this node the method returns the node data (trimmed) as string of the
     * first text node which is found or an empty string.
     * 
     * @param aXPath
     *        A valid XPath string.
     */
    protected String getNodeDataAsString(Node aNode, String aXPath) {
        return getDataAsString(getSingleNode(aNode, aXPath));
    }
    
    /**
     * The xpath (with the context node as prefix) must select one node. For
     * this node the method returns the node data (trimmed) as boolean of the
     * first text node which is found.
     * 
     * @param aXPath
     *        A valid XPath string.
     */
    protected boolean getNodeDataAsBoolean(Node aNode, String aXPath) {
        return getDataAsBoolean(getSingleNode(aNode, aXPath));
    }
    
    /**
     * The xpath (with the context node as prefix) must select one node. For
     * this node the method returns the node data (trimmed) as int of the first
     * text node which is found.
     * 
     * @param aXPath
     *        A valid XPath string.
     */
    protected int getNodeDataAsInt(Node aNode, String aXPath) {
        return getDataAsInt(getSingleNode(aNode, aXPath));
    }
    
    /**
     * The xpath (with the context node as prefix) must select one node. For
     * this node the method returns the node data (trimmed) as double of the
     * first text node which is found.
     * 
     * @param aXPath
     *        A valid XPath string.
     */
    protected double getNodeDataAsDouble(Node aNode, String aXPath) {
        return getDataAsDouble(getSingleNode(aNode, aXPath));
    }
    
    /**
     * The xpath must select one node in the given document. For this node the
     * method returns the node data (trimmed) as string of the first text node
     * which is found or an empty string.
     * 
     * @param aDocument
     *        The document. Must not be <code>null</code>.
     * @param aXPath
     *        A valid XPath string.
     */
    protected String getNodeDataAsString(Document aDocument, String aXPath) {
        return getDataAsString(getSingleNode(aDocument, aXPath));
    }
    
    /**
     * The xpath must select one node in the given document. For this node the
     * method returns the node data (trimmed) as boolean of the first text node
     * which is found or an empty string.
     * 
     * @param aDocument
     *        The document. Must not be <code>null</code>.
     * @param aXPath
     *        A valid XPath string.
     */
    protected boolean getNodeDataAsBoolean(Document aDocument, String aXPath) {
        return getDataAsBoolean(getSingleNode(aDocument, aXPath));
    }
    
    /**
     * The xpath must select one node in the given document. For this node the
     * method returns the node data (trimmed) as int of the first text node
     * which is found or an empty string.
     * 
     * @param aDocument
     *        The document. Must not be <code>null</code>.
     * @param aXPath
     *        A valid XPath string.
     */
    protected int getNodeDataAsInt(Document aDocument, String aXPath) {
        return getDataAsInt(getSingleNode(aDocument, aXPath));
    }
    
    /**
     * The xpath must select one node in the given document. For this node the
     * method returns the node data (trimmed) as double of the first text node
     * which is found or an empty string.
     * 
     * @param aDocument
     *        The document. Must not be <code>null</code>.
     * @param aXPath
     *        A valid XPath string.
     */
    protected double getNodeDataAsDouble(Document aDocument, String aXPath) {
        return getDataAsDouble(getSingleNode(aDocument, aXPath));
    }
    
    // Extract attribute data
    
    /**
     * This method returns the attribute data as string of the given node and
     * attribute.
     * 
     * @param aNode
     *        A {@link Node}. Must not be <code>null</code>.
     * @param anAttributeName
     *        A attribute name. Must not be <code>null</code> or emtpy.
     */
    protected String getAttributeAsString(Node aNode, String anAttributeName) {
        NamedNodeMap theAttributes = aNode.getAttributes();
        
        return theAttributes.getNamedItem(anAttributeName).getNodeValue(); 
    }
    
    /**
     * This method returns the attribute data as boolean of the given node and
     * attribute.
     * 
     * @param aNode
     *        A {@link Node}. Must not be <code>null</code>.
     * @param anAttributeName
     *        A attribute name. Must not be <code>null</code> or emtpy.
     */
    protected boolean getAttributeAsBoolean(Node aNode, String anAttributeName) {
        return Boolean.valueOf(getAttributeAsString(aNode, anAttributeName)).booleanValue();
    }
    
    /**
     * This method returns the attribute data as int of the given node and
     * attribute.
     * 
     * @param aNode
     *        A {@link Node}. Must not be <code>null</code>.
     * @param anAttributeName
     *        A attribute name. Must not be <code>null</code> or emtpy.
     */
    protected int getAttributeAsInt(Node aNode, String anAttributeName) {
        return Integer.parseInt(getAttributeAsString(aNode, anAttributeName));
    }
    
    /**
     * This method returns the attribute data as double of the given node and
     * attribute.
     * 
     * @param aNode
     *        A {@link Node}. Must not be <code>null</code>.
     * @param anAttributeName
     *        A attribute name. Must not be <code>null</code> or emtpy.
     */
    protected double getAttributeAsDouble(Node aNode, String anAttributeName) {
        return Double.parseDouble(getAttributeAsString(aNode, anAttributeName));
    }

    /**
	 * @see #extractObject(String)
	 */ 
    protected Object extractObject(Node aNode) {
        return extractObject(getDataAsString(aNode));
    }
    
    /**
     * This method tries to create an plain java object out of the given string.
     * Therefore it uses the *parse(String)-methods from different classes.
     * If no object could be created, the original string will be returned.
     */
    public static Object extractObject(String aString) {
        
        if (!StringServices.isEmpty(aString)) {
            if ("NEGINF".equals(aString)) {
				return Double.valueOf(Double.NEGATIVE_INFINITY);
            }
            if ("POSINF".equals(aString)) {
				return Double.valueOf(Double.POSITIVE_INFINITY);
            }
            try {
                return DateTokenHandler.getInstance().handleToken(aString);
            } catch (Exception ex) {
                // try next one
            }
            
            try {
                return CalendarUtil.getDateInstance(DateFormat.SHORT).parseObject(aString);
            } catch (ParseException pex) {
                // try next one
            }

            try {
				return Integer.valueOf(aString);
            } catch (NumberFormatException nex) {
                // try next one
            }
            
            try {
				return Long.valueOf(aString);
            } catch (NumberFormatException nex) {
                // try next one
            }
            
            try {
				return Float.valueOf(aString);
            } catch (NumberFormatException nex) {
            }
            
            try {
				return Double.valueOf(aString);
            } catch (NumberFormatException nex) {
                // try next one
            }
            
            // hmm,  so its a string
            aString = aString.replaceAll("\"", "");
            return aString;
        }
        else {
            return null;
        }
    }
}

