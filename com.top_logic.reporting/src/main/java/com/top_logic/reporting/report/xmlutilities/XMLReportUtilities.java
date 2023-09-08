/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.xmlutilities;


/**
 * The XMLReportUtilities is a static class to load, parse and write xml files.
 * 
 * This XMLReportUtilities provides useful method to get nodes, node lists or extract data from nodes or
 * attributes.
 * 
 * @author <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class XMLReportUtilities implements XMLReportTags {
//	private static boolean		NS_AWARENESS			= true;
//	private static boolean		VALIDATING				= true;
//
//	// the schema language for the xml schema
//	private static final String	JAXP_SCHEMA_LANGUAGE	= "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
//	private static final String	W3C_XML_SCHEMA			= "http://www.w3.org/2001/XMLSchema";
//
//	// the source of the schema, could also be a relative path
//	private static final String	JAXP_SCHEMA_SOURCE		= "http://java.sun.com/xml/jaxp/properties/schemaSource";
//	private static final String	SCHEMA					= "/WEB-INF/reportTemplates/flexReporting/report.xsd";
//
//	// Load document methods
//
//	/**
//	 * This method parses the given xml file and returns it.
//	 * 
//	 * @param aFile
//	 *            A xml file. Must not be <code>null</code>.
//	 */
//	public static Document parseDocument(File aFile) throws ImportException {
//		try {
//			DocumentBuilder theBuilder = getDocumentBuilder();
//			Document theDocument = theBuilder.parse(aFile);
//			return theDocument;
//		}
//		catch (IllegalArgumentException e) {
//			throw new ImportException(XMLReportUtilities.class, "Parser does not support JAXP 1.2", e);
//		}
//		catch (IOException e) {
//			throw new ImportException(XMLReportUtilities.class, "Could not parse the file '"
//					+ aFile.getAbsolutePath() + "'.", e);
//		}
//		catch (SAXException e) {
//			throw new ImportException(XMLReportUtilities.class, "Parser error", e);
//		}
//	}
//
//	public static Document parseDocument(String aString) {
//		try {
//			DocumentBuilder theBuilder = getDocumentBuilder();
//			InputStream theIS = new ByteArrayInputStream(aString.getBytes("UTF-8"));
//			Document theDocument = theBuilder.parse(theIS);
//			return theDocument;
//		}
//		catch (IllegalArgumentException e) {
//			throw new ImportException(XMLReportUtilities.class, "Parser does not support JAXP 1.2", e);
//		}
//		catch (SAXException e) {
//			throw new ImportException(XMLReportUtilities.class, "Parser error", e);
//		}
//		catch (IOException e) {
//			throw new ImportException(XMLReportUtilities.class, "Problems parsing the String", e);
//		}
//	}
//
//	/**
//	 * Creates and returns a new xml document.
//	 * 
//	 * @throws ParserConfigurationException
//	 */
//	public static Document createDocument() throws CreateException {
//		try {
//			DocumentBuilder theBuilder = getDocumentBuilder();
//			Document theDocument = theBuilder.newDocument();
//			return theDocument;
//		}
//		catch (IllegalArgumentException e) {
//			throw new ImportException(XMLReportUtilities.class, "Parser does not support JAXP 1.2", e);
//		}
//		catch (Exception e) {
//			throw new CreateException(XMLReportUtilities.class,
//					"Parser with specified options can't be built." + e.getMessage());
//		}
//	}
//
//	/**
//	 * Creates a {@link DocumentBuilder} with the default settings.
//	 * 
//	 * @return the {@link DocumentBuilder}
//	 */
//	private static DocumentBuilder getDocumentBuilder() {
//		DocumentBuilder theBuilder;
//		try {
//			DocumentBuilderFactory theFactory = DocumentBuilderFactory.newInstance();
//			theFactory.setNamespaceAware(NS_AWARENESS);
//			theFactory.setValidating(VALIDATING);
//			theFactory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
//			FileManager theFM = FileManager.getInstance();
//			File theFile = theFM.getFile(SCHEMA);
//			theFactory.setAttribute(JAXP_SCHEMA_SOURCE, theFile.getAbsolutePath());
//			theBuilder = theFactory.newDocumentBuilder();
//			theBuilder.setErrorHandler(new XMLReportErrorHandler());
//		}
//		catch (ParserConfigurationException e) {
//			throw new CreateException(XMLReportUtilities.class,
//					"Parser with specified options can't be built." + e.getMessage());
//		}
//		catch (IOException e) {
//			throw new ImportException(XMLReportUtilities.class, e.getMessage());
//		}
//		return theBuilder;
//	}
//
//	// Select node or node list methods
//
//	/**
//	 * This method returns a {@link Node} which is selected by the given xpath for the document.
//	 * 
//	 * @param aDocument
//	 *            The document. Must not be <code>null</code>.
//	 * @param aXPath
//	 *            A valid XPath string.
//	 */
//	public static Node getSingleNode(Document aDocument, String aXPath) throws ImportException {
//		return getSingleNode(aDocument.getDocumentElement(), aXPath);
//	}
//
//	/**
//	 * This method returns a {@link Node} which is selected by the given xpath for the context node.
//	 * 
//	 * @param aDocument
//	 *            The document. Must not be <code>null</code>.
//	 * @param aXPath
//	 *            A valid XPath string.
//	 */
//	public static Node getSingleNode(Node aContextNode, String aXPath) {
//		try {
//			return XPathAPI5.selectSingleNode(aContextNode, aXPath);
//		}
//		catch (TransformerException te) {
//			throw new ImportException(XMLReportUtilities.class,
//					"Could not get the single node for the xpath '" + aXPath + "'.", te);
//		}
//	}
//
//	/**
//	 * 
//	 * This method checks the existance of a node identified by the given path
//	 * 
//	 * @param aContextNode
//	 *            A node to query the xpath from.
//	 * @param aXPath
//	 *            A XPath string.
//	 * @return <code>true</code> if the node exists, false otherwise.
//	 */
//	public static boolean hasSingleNode(Node aContextNode, String aXPath) {
//		try {
//			return (getSingleNode(aContextNode, aXPath) != null);
//		}
//		catch (Exception ex) {
//			// its ok, node not present, return false
//		}
//		return false;
//	}
//
//	/**
//	 * This method returns a {@link NodeList} which is selected by the given xpath for the document.
//	 * 
//	 * @param aDocument
//	 *            The document. Must not be <code>null</code>.
//	 * @param aXPath
//	 *            A valid XPath string.
//	 */
//	public static NodeList getNodeList(Document aDocument, String aXPath) throws ImportException {
//		return getNodeList(aDocument.getDocumentElement(), aXPath);
//	}
//
//	/**
//	 * This method returns a {@link NodeList} which is selected by the given xpath for the context node.
//	 * 
//	 * @param aNode
//	 *            The document. Must not be <code>null</code>.
//	 * @param aXPath
//	 *            A valid XPath string.
//	 */
//	public static NodeList getNodeList(Node aNode, String aXPath) throws ImportException {
//		try {
//			return XPathAPI5.selectNodeList(aNode, aXPath);
//		}
//		catch (TransformerException te) {
//			throw new ImportException(XMLReportUtilities.class,
//					"Could not get the node list for the xpath '" + aXPath + "'.", te);
//		}
//	}
//
//	/**
//	 * Returns a list with the child element nodes of the given node or an empty list but never null.
//	 * 
//	 * @param aNode
//	 *            The node. Must not be <code>null</code>.
//	 */
//	public static List getChildElementNodes(Node aNode) {
//		ArrayList theResult = new ArrayList();
//
//		NodeList theChildNodes = aNode.getChildNodes();
//		for (int i = 0; i < theChildNodes.getLength(); i++) {
//			Node theNode = theChildNodes.item(i);
//
//			if (theNode.getNodeType() != Node.ELEMENT_NODE) {
//				continue;
//			}
//
//			theResult.add(theNode);
//		}
//
//		return theResult;
//	}
//
//	// Extract node data
//
//	/**
//	 * Returns a map which contains all entry values of the given node.
//	 * 
//	 * @param aEntryNode
//	 *            A entry node. E.g. <entryNode> <string key="class" value="java.util.Date" /> <boolean
//	 *            key="neg" value="true" /> <date key="val1" value="01.01.2005" /> </entryNode>
//	 * 
//	 * key value value type map entry class java.util.Date String map entry neg true Boolean map entry
//	 * val1 01.01.2005 Date
//	 */
//	public static Map getEntryDataAsMap(Node aEntryNode) {
//		HashMap theDataMap = new HashMap();
//
//		List theEntryNodes = getChildElementNodes(aEntryNode);
//		for (Iterator theIter = theEntryNodes.iterator(); theIter.hasNext();) {
//			Node theEntryNode = (Node) theIter.next();
//			String theTagName = theEntryNode.getNodeName();
//
//			EntryParser theParser = EntryParserManager.getInstance().getEntryParser(theTagName);
//			Entry theEntry = theParser.parse(theEntryNode);
//			theDataMap.put(theEntry.getKey(), theEntry.getValue());
//		}
//
//		return theDataMap;
//	}
//
//	/**
//	 * This method returns the node data (trimmed) of the first text node which is found or an empty
//	 * string.
//	 * 
//	 * @param aNode
//	 *            A node. Must not be <code>null</code>.
//	 */
//	public static String getDataAsString(Node aNode) {
//		NodeList theList = aNode.getChildNodes();
//		for (int i = 0; i < theList.getLength(); i++) {
//			Node theNode = theList.item(i);
//			if (theNode.getNodeType() == Node.TEXT_NODE) {
//				return theNode.getNodeValue().trim();
//			}
//		}
//
//		return "";
//	}
//
//	/**
//	 * This method returns the node data (trimmed) as Color of the first text node which is found.
//	 * 
//	 * @param aNode
//	 *            A node. Must not be <code>null</code>.
//	 */
//	public static Color getDataAsColor(Node aNode) {
//		String theColorString = getDataAsString(aNode);
//
//		// Use theme background color
//		if (theColorString.equalsIgnoreCase("theme")) {
//			return ReportUtilities.getThemeBackgroundColor();
//		}
//
//		return Color.decode(theColorString);
//	}
//
//	/**
//	 * This method returns the node data (trimmed) as boolean of the first text node which is found.
//	 * 
//	 * @param aNode
//	 *            A node. Must not be <code>null</code>.
//	 */
//	public static boolean getDataAsBoolean(Node aNode) {
//		return Boolean.valueOf(getDataAsString(aNode)).booleanValue();
//	}
//
//	/**
//	 * This method returns the node data (trimmed) as int of the first text node which is found.
//	 * 
//	 * @param aNode
//	 *            A node. Must not be <code>null</code>.
//	 */
//	public static int getDataAsInt(Node aNode) {
//		return Integer.parseInt(getDataAsString(aNode));
//	}
//
//	/**
//	 * This method returns the node data (trimmed) as double of the first text node which is found.
//	 * 
//	 * @param aNode
//	 *            A node. Must not be <code>null</code>.
//	 */
//	public static double getDataAsDouble(Node aNode) {
//		return Double.parseDouble(getDataAsString(aNode));
//	}
//
//	/**
//	 * The xpath (with the context node as prefix) must select one node. For this node the method returns
//	 * the node data (trimmed) as string of the first text node which is found or an empty string.
//	 * 
//	 * @param aDocument
//	 *            The document. Must not be <code>null</code>.
//	 * @param aXPath
//	 *            A valid XPath string.
//	 */
//	public static String getNodeDataAsString(Node aNode, String aXPath) {
//		return getDataAsString(getSingleNode(aNode, aXPath));
//	}
//
//	/**
//	 * The xpath (with the context node as prefix) must select one node. For this node the method returns
//	 * the node data (trimmed) as boolean of the first text node which is found.
//	 * 
//	 * @param aDocument
//	 *            The document. Must not be <code>null</code>.
//	 * @param aXPath
//	 *            A valid XPath string.
//	 */
//	public static boolean getNodeDataAsBoolean(Node aNode, String aXPath) {
//		return getDataAsBoolean(getSingleNode(aNode, aXPath));
//	}
//
//	/**
//	 * The xpath (with the context node as prefix) must select one node. For this node the method returns
//	 * the node data (trimmed) as int of the first text node which is found.
//	 * 
//	 * @param aDocument
//	 *            The document. Must not be <code>null</code>.
//	 * @param aXPath
//	 *            A valid XPath string.
//	 */
//	public static int getNodeDataAsInt(Node aNode, String aXPath) {
//		return getDataAsInt(getSingleNode(aNode, aXPath));
//	}
//
//	/**
//	 * The xpath (with the context node as prefix) must select one node. For this node the method returns
//	 * the node data (trimmed) as double of the first text node which is found.
//	 * 
//	 * @param aDocument
//	 *            The document. Must not be <code>null</code>.
//	 * @param aXPath
//	 *            A valid XPath string.
//	 */
//	public static double getNodeDataAsDouble(Node aNode, String aXPath) {
//		return getDataAsDouble(getSingleNode(aNode, aXPath));
//	}
//
//	/**
//	 * The xpath must select one node in the given document. For this node the method returns the node
//	 * data (trimmed) as string of the first text node which is found or an empty string.
//	 * 
//	 * @param aDocument
//	 *            The document. Must not be <code>null</code>.
//	 * @param aXPath
//	 *            A valid XPath string.
//	 */
//	public static String getNodeDataAsString(Document aDocument, String aXPath) {
//		return getDataAsString(getSingleNode(aDocument, aXPath));
//	}
//
//	/**
//	 * The xpath must select one node in the given document. For this node the method returns the node
//	 * data (trimmed) as boolean of the first text node which is found or an empty string.
//	 * 
//	 * @param aDocument
//	 *            The document. Must not be <code>null</code>.
//	 * @param aXPath
//	 *            A valid XPath string.
//	 */
//	public static boolean getNodeDataAsBoolean(Document aDocument, String aXPath) {
//		return getDataAsBoolean(getSingleNode(aDocument, aXPath));
//	}
//
//	/**
//	 * The xpath must select one node in the given document. For this node the method returns the node
//	 * data (trimmed) as int of the first text node which is found or an empty string.
//	 * 
//	 * @param aDocument
//	 *            The document. Must not be <code>null</code>.
//	 * @param aXPath
//	 *            A valid XPath string.
//	 */
//	public static int getNodeDataAsInt(Document aDocument, String aXPath) {
//		return getDataAsInt(getSingleNode(aDocument, aXPath));
//	}
//
//	/**
//	 * The xpath must select one node in the given document. For this node the method returns the node
//	 * data (trimmed) as double of the first text node which is found or an empty string.
//	 * 
//	 * @param aDocument
//	 *            The document. Must not be <code>null</code>.
//	 * @param aXPath
//	 *            A valid XPath string.
//	 */
//	public static double getNodeDataAsDouble(Document aDocument, String aXPath) {
//		return getDataAsDouble(getSingleNode(aDocument, aXPath));
//	}
//
//	// Extract attribute data
//
//	/**
//	 * This method returns the attribute data as string of the given node and attribute.
//	 * 
//	 * @param aNode
//	 *            A {@link Node}. Must not be <code>null</code>.
//	 * @param anAttributeName
//	 *            A attribute name. Must not be <code>null</code> or emtpy.
//	 */
//	public static String getAttributeAsString(Node aNode, String anAttributeName) {
//		NamedNodeMap theAttributes = aNode.getAttributes();
//
//		return theAttributes.getNamedItem(anAttributeName).getNodeValue();
//	}
//
//	/**
//	 * This method returns the attribute data as boolean of the given node and attribute.
//	 * 
//	 * @param aNode
//	 *            A {@link Node}. Must not be <code>null</code>.
//	 * @param anAttributeName
//	 *            A attribute name. Must not be <code>null</code> or emtpy.
//	 */
//	public static boolean getAttributeAsBoolean(Node aNode, String anAttributeName) {
//		return Boolean.valueOf(getAttributeAsString(aNode, anAttributeName)).booleanValue();
//	}
//
//	/**
//	 * This method returns the attribute data as int of the given node and attribute.
//	 * 
//	 * @param aNode
//	 *            A {@link Node}. Must not be <code>null</code>.
//	 * @param anAttributeName
//	 *            A attribute name. Must not be <code>null</code> or emtpy.
//	 */
//	public static int getAttributeAsInt(Node aNode, String anAttributeName) {
//		return Integer.parseInt(getAttributeAsString(aNode, anAttributeName));
//	}
//
//	/**
//	 * This method returns the attribute data as double of the given node and attribute.
//	 * 
//	 * @param aNode
//	 *            A {@link Node}. Must not be <code>null</code>.
//	 * @param anAttributeName
//	 *            A attribute name. Must not be <code>null</code> or emtpy.
//	 */
//	public static double getAttributeAsDouble(Node aNode, String anAttributeName) {
//		return Double.parseDouble(getAttributeAsString(aNode, anAttributeName));
//	}
//
//	public static Map getOptionMap(Node aNode) {
//		NodeList theChildNodes = aNode.getChildNodes();
//		Map theMap = new HashMap(theChildNodes.getLength());
//
//		for (int i = 0; i < theChildNodes.getLength(); i++) {
//			Node theNode = theChildNodes.item(i);
//			boolean hasAttributes = theNode.hasAttributes();
//			int children = theNode.getChildNodes().getLength();
//			if (theNode.getNodeType() == Element.TEXT_NODE) { // empty text node
//				continue;
//			}
//			if (hasAttributes) {
//				NamedNodeMap theAttributes = theNode.getAttributes();
//				for (int j = 0; j < theAttributes.getLength(); j++) {
//					Node theAttributeNode = theAttributes.item(j);
//					theMap.put(theNode.getNodeName() + "_" + theAttributeNode.getNodeName(),
//							theAttributeNode.getNodeValue());
//				}
//			}
//			switch (children) {
//				case 0:
//					theMap.put(theNode.getNodeName(), getDataAsString(theNode));
//					break;
//				case 1:
//					if (theNode.getFirstChild().getNodeType() == Element.TEXT_NODE) {
//						theMap.put(theNode.getNodeName(), getDataAsString(theNode));
//					}
//					if (theNode.getFirstChild().getNodeType() == Element.ELEMENT_NODE) {
//						theMap.put(theNode.getNodeName(), getOptionMap(theNode));
//					}
//					break;
//				default:
//					theMap.put(theNode.getNodeName(), getOptionMap(theNode));
//					break;
//			}
//		}
//		return theMap;
//	}
//	
//	public static List getOptionList(Node aNode) {
//		NodeList theChildNodes = aNode.getChildNodes();
//		List theList = new ArrayList(theChildNodes.getLength());
//		
//		for (int i = 0; i < theChildNodes.getLength(); i++) {
//			Node theNode = theChildNodes.item(i);
//			if (theNode.getNodeType() == Element.TEXT_NODE) { // empty text node
//				continue;
//			}
//				theList.add(getOptionMap(theNode));
//		}
//		return theList;
//	}
//
//	/**
//	 * @see {@link #extractObject(String)}
//	 */
//	public static Object extractObject(Node aNode) {
//		return extractObject(getDataAsString(aNode));
//	}
//
//	/**
//	 * This method tries to create an plain java object out of the given string. Therefore it uses the
//	 * *parse(String)-methods from different classes. If no object could be created, the original string
//	 * will be returned.
//	 * 
//	 * @param aString
//	 * @return
//	 */
//	public static Object extractObject(String aString) {
//
//		if (!StringServices.isEmpty(aString)) {
//			if ("NEGINF".equals(aString)) {
//				return new Double(Double.NEGATIVE_INFINITY);
//			}
//			if ("POSINF".equals(aString)) {
//				return new Double(Double.POSITIVE_INFINITY);
//			}
//			try {
//				return DateTokenHandler.getInstance().handleToken(aString);
//			}
//			catch (Exception ex) {
//				// try next one
//			}
//
//			try {
//				return CalendarUtil.getDateInstance(DateFormat.SHORT).parseObject(aString);
//			}
//			catch (ParseException pex) {
//				// try next one
//			}
//
//			try {
//				return Integer.valueOf(Integer.parseInt(aString));
//			}
//			catch (NumberFormatException nex) {
//				// try next one
//			}
//
//			try {
//				return Long.valueOf(Long.parseLong(aString));
//			}
//			catch (NumberFormatException nex) {
//				// try next one
//			}
//
//			try {
//				return new Float(Float.parseFloat(aString));
//			}
//			catch (NumberFormatException nex) {
//			}
//
//			try {
//				return Double.valueOf(Double.parseDouble(aString));
//			}
//			catch (NumberFormatException nex) {
//				// try next one
//			}
//
//			// hmm, so its a string
//			aString = aString.replaceAll("\"", "");
//			return aString;
//		}
//		else {
//			return null;
//		}
//	}
//
//	/**
//	 * Creates a new {@link Element} with the given content as a child text node.
//	 * 
//	 * @param aDocument
//	 *            the {@link Document} the new {@link Element} belongs to.
//	 * @param anElementName
//	 *            the name of the new {@link Element}
//	 * @param aContent
//	 *            the text content of the new {@link Element}
//	 * @return a new {@link Element}
//	 */
//	public static Element createLeafElement(Document aDocument, String anElementName, String aContent) {
//		Element theElement = aDocument.createElement(anElementName);
//		theElement.appendChild(aDocument.createTextNode(aContent));
//		return theElement;
//	}
//
//	/**
//	 * Creates a function element with the given information.
//	 * 
//	 * @param aDocument
//	 *            the {@link Document} the new {@link Element} belongs to.
//	 * @param aType
//	 *            the type of the function (i.e. the name)
//	 * @param ignoreNull
//	 *            ignore null values
//	 * @param anAttribute
//	 *            the name of the attribute this function works on
//	 * @return a new {@link Element}
//	 */
//	public static Element createFunctionElement(Document aDocument, String aType, String anAccessor, String ignoreNull, String anAttribute) {
//		Element theElement = aDocument.createElement(FUNCTION);
//		Element theType = createLeafElement(aDocument, TYPE, aType);
//		Element theAccessorClass = createLeafElement(aDocument, CLASS, anAccessor);
//		Element theAccessor = aDocument.createElement(ACCESSOR);
//		theAccessor.appendChild(theAccessorClass);
//		Element theIgnore = createLeafElement(aDocument, IGNORE_NULL, ignoreNull);
//		Element theAttribute = createLeafElement(aDocument, ATTRIBUTE, anAttribute);
//		theElement.appendChild(theType);
//		theElement.appendChild(theAccessor);
//		theElement.appendChild(theIgnore);
//		theElement.appendChild(theAttribute);
//		return theElement;
//	}
//
//	/**
//	 * Creates a partition element with the given information.
//	 * 
//	 * @param aDocument
//	 *            the {@link Document} the new {@link Element} belongs to.
//	 * @param aType
//	 *            the type of the function (i.e. the name)
//	 * @param ignoreNull
//	 *            ignore null values
//	 * @param anAttribute
//	 *            the name of the attribute this function works on
//	 * @return a new {@link Element}
//	 */
//	public static Element createPartitionElement(Document aDocument, PartitionFunction aFunction) {
//		String theType = aFunction.getType();
//		XMLElementFactory theFactory = XMLElementFactory.getInstance();
//		XMLElement theElement = theFactory.getPartitionElement(aDocument, theType, aFunction);
//		return theElement.createElement(aDocument);
//
//	}
//	
//	public static Element createTargetElement(Document aDocument, String from, String to, String label) {
//		Element theElement = aDocument.createElement(TARGET);
//		Element theFrom = createLeafElement(aDocument, FROM, from);
//		Element theTo = createLeafElement(aDocument, TO, to);
//		Element theLabel = createLeafElement(aDocument, LABEL, label);
//		theElement.appendChild(theFrom);
//		theElement.appendChild(theTo);
//		theElement.appendChild(theLabel);
//		return theElement;
//	}
//	
//	public static Element createColorElement(Document aDocument, String aFuncName, String aColor) {
//		Element theElement = aDocument.createElement(DEFINITION);
//		Element theFunc = createLeafElement(aDocument, FUNCTION_NAME, aFuncName);
//		Element theColor = createLeafElement(aDocument, COLOR, aColor);
//		theElement.appendChild(theFunc);
//		theElement.appendChild(theColor);
//		return theElement;
//	}
}
