/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml.document;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;

import com.top_logic.basic.StringServices;


/**
 * Thread safe immutable {@link Element} implementation.
 * 
 * @author     <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class TSElement extends TSNode implements Element {

	private static final String TAG_NAME_WILDCARD = "*";
	private static final String NAMESPACE_URI_WILDCARD = "*";

	private static final Attr NO_ATTRIBUTE = null;

	private final String tagName;
	
	private final NamedNodeMap attributes;

	protected TSElement(Element sourceElement, Node parent) {
    	super(sourceElement, parent);
        this.tagName = sourceElement.getTagName();
        if (sourceElement.hasAttributes()) {
        	this.attributes = ThreadSafeDOMFactory.importNamedNodeMap(sourceElement.getAttributes());
        } else {
        	this.attributes = EmptyNamedNodeMap.INSTANCE;
        }
    }
    
    @Override
	public String getTagName() {
        return tagName;
    }

    @Override
	public String getAttribute(String name) {
    	Attr attribute = getAttributeNode(name);
    	return attributeValue(attribute);
    }

    @Override
	public String getAttributeNS(String aNamespaceURI, String aLocalName)
            throws DOMException {
    	Attr attribute = getAttributeNodeNS(aNamespaceURI, aLocalName);
    	return attributeValue(attribute);
    }

	private String attributeValue(Attr attribute) {
		if (attribute == NO_ATTRIBUTE) {
    		return StringServices.EMPTY_STRING;
    	} else {
    		return attribute.getNodeValue();
    	}
	}

    @Override
	public void setAttribute(String name, String value) throws DOMException {
    	throw noModificationAllowedErr();
    }

    @Override
	public void removeAttribute(String aName) throws DOMException {
    	throw noModificationAllowedErr();
    }

    @Override
	public Attr getAttributeNode(String aName) {
    	return getAttributeNode(aName, false, null);
    }

	private Attr getAttributeNode(String aLocalName, boolean useNS, String nsURI) {
		if (!hasAttributes()) {
    		return NO_ATTRIBUTE;
    	}
		Node namedItem;
		if (useNS) {
			namedItem = this.attributes.getNamedItemNS(nsURI, aLocalName);
		} else {
			namedItem = this.attributes.getNamedItem(aLocalName);
		}
		return (Attr) namedItem;
	}

    @Override
	public Attr setAttributeNode(Attr aNewAttr) throws DOMException {
    	throw noModificationAllowedErr();
    }

    @Override
	public Attr removeAttributeNode(Attr aOldAttr) throws DOMException {
    	throw noModificationAllowedErr();
    }

    @Override
	public NodeList getElementsByTagName(String aName) {
    	List<Node> theList = new ArrayList<>();
    	this.addElementsByTagName(this, aName, theList, false, null);
        return new TSNodeList(theList, this);
    }
    
    private void addElementsByTagName(Node root, String aLocalName, List<Node> matches, boolean useNS, String nsURI) {
    	NodeList theChildNodes = root.getChildNodes();

    	for(int i=0; i < theChildNodes.getLength(); i++) {
    	    Node node = theChildNodes.item(i);
    	    if (!(node instanceof Element)) {
    	    	continue;
    	    }
    	    Element element = (Element) node;
    	    
    		if (matchesTagName(element, aLocalName, useNS, nsURI)) {
    			matches.add(element);
    		}
    		if (element instanceof TSElement) {
    			((TSElement) element).addElementsByTagName(element, aLocalName, matches, useNS, nsURI);
    		} else {
    			NodeList elementsByTagName;
    			if (useNS) {
    				elementsByTagName = element.getElementsByTagName(aLocalName);
    			} else {
    				elementsByTagName = element.getElementsByTagNameNS(nsURI, aLocalName);
    			}
    			
    			for (int j = 0, size = elementsByTagName.getLength(); j < size; j++) {
    				Node item = elementsByTagName.item(i);
    				matches.add(item);
    			}
    			throw new IllegalStateException("Node '" + node + "' has children of foreign implementation hierarchy.");
    		}
    	}
    }

	protected boolean matchesTagName(Node node, String aTagName, boolean useNS, String nsURI) {
		boolean tagNameMatches = StringServices.equals(node.getLocalName(), aTagName) || TAG_NAME_WILDCARD.equals(aTagName);
		if (useNS) {
			if (matchesNS(node, nsURI)) {
				return tagNameMatches;
			} else {
				return false;
			}
		} else {
			return tagNameMatches;
		}
	}

	private boolean matchesNS(Node node, String nsURI) {
		return StringServices.equals(node.getNamespaceURI(), nsURI) || NAMESPACE_URI_WILDCARD.equals(nsURI);
	}

    @Override
	public void setAttributeNS(String aNamespaceURI, String aQualifiedName,
            String aValue) throws DOMException {
    	throw noModificationAllowedErr();
    }

    @Override
	public void removeAttributeNS(String aNamespaceURI, String aLocalName)
            throws DOMException {
    	throw noModificationAllowedErr();
    }

    @Override
	public Attr getAttributeNodeNS(String aNamespaceURI, String aLocalName)
            throws DOMException {
    	return getAttributeNode(aLocalName, true, aNamespaceURI);
    }

    @Override
	public Attr setAttributeNodeNS(Attr aNewAttr) throws DOMException {
    	throw noModificationAllowedErr();
    }

    @Override
	public NodeList getElementsByTagNameNS(String aNamespaceURI, String aLocalName)
            throws DOMException {
    	List<Node> theList = new ArrayList<>();
    	this.addElementsByTagName(this, aLocalName, theList, true, aNamespaceURI);
        return new TSNodeList(theList, this);
    }

    @Override
	public boolean hasAttribute(String aName) {
    	return getAttributeNode(aName) != NO_ATTRIBUTE;
    }

    @Override
	public boolean hasAttributeNS(String aNamespaceURI, String aLocalName)
            throws DOMException {
    	return getAttributeNodeNS(aNamespaceURI, aLocalName) != NO_ATTRIBUTE;
    }

    @Override
	public TypeInfo getSchemaTypeInfo() {
		throw unsupportedOperation();
    }

    @Override
	public void setIdAttribute(String aName, boolean aIsId) throws DOMException {
    	throw noModificationAllowedErr();
    }

    @Override
	public void setIdAttributeNS(String aNamespaceURI, String aLocalName, boolean aIsId)
            throws DOMException {
    	throw noModificationAllowedErr();
    }

    @Override
	public void setIdAttributeNode(Attr aIdAttr, boolean aIsId) throws DOMException {
    	throw noModificationAllowedErr();
    }

	@Override
	public NamedNodeMap getAttributes() {
		return attributes;
	}

	@Override
	public boolean hasAttributes() {
		return attributes.getLength() != 0;
	}

	@Override
	public String getNodeName() {
		return getTagName();
	}

	@Override
	public String getNodeValue() throws DOMException {
		return null;
	}

	@Override
	public short getNodeType() {
		return Node.ELEMENT_NODE;
	}
}