/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.apply;

import static com.top_logic.basic.xml.DOMUtil.*;
import static com.top_logic.config.xdiff.compare.XDiffSchemaConstants.*;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.config.xdiff.compare.XDiffSchemaConstants;

/**
 * Algorithm to transform a diff specification back into the source or destination document.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class XApply {

	enum Mode {
		DELETE() {
			@Override
			public boolean shouldKeep(boolean reverse) {
				return reverse;
			}
		},

		INSERT() {
			@Override
			public boolean shouldKeep(boolean reverse) {
				return !reverse;
			}
		};

		public abstract boolean shouldKeep(boolean reverse);
	}

	private final boolean _reverse;
	
	/**
	 * Creates a {@link XApply}.
	 * 
	 * @param reverse
	 *        <code>false</code> to reconstruct the target document, <code>true</code> to
	 *        reconstruct the target document of a given difference description.
	 */
	public XApply(boolean reverse) {
		_reverse = reverse;
	}

	/**
	 * Transform the given difference description.
	 * 
	 * @param diff
	 *        The difference description that is transformed into either the source or the
	 *        destination document depending on the {@link #XApply(boolean) algorithm configuration}.
	 */
	public void apply(Document diff) {
		processRoot(diff.getDocumentElement());
	}

	private void processRoot(Element root) {
		if (XDIFF_NS.equals(root.getNamespaceURI())) {
			if (XDiffSchemaConstants.DIFF_ELEMENT.equals(root.getLocalName())) {
				processDiffChildren(root);
				removeRootShallow(root);
			} else {
				throw errorUnexpectedDiffElement(root);
			}
		} else {
			processChildren(root);
		}
	}

	private void removeRootShallow(Element diff) {
		Node parent = diff.getParentNode();
		parent.removeChild(diff);
		
		Node nextChild;
		for (Node child = diff.getFirstChild(); child != null; child = nextChild) {
			nextChild = child.getNextSibling();

			diff.removeChild(child);
			if (child instanceof Element) {
				parent.appendChild(child);
			}
		}
	}

	private void processDiffChildren(Element diff) {
		Node nextChild;
		for (Node diffChild = diff.getFirstChild(); diffChild != null; diffChild = nextChild) {
			nextChild = diffChild.getNextSibling();
			
			if (diffChild instanceof Element) {
				processDiffChild((Element) diffChild);
			}
		}
	}

	private void processDiffChild(Element diffChild) {
		if (XDIFF_NS.equals(diffChild.getNamespaceURI())) {
			processOnlyElementOperation(diffChild);
		} else {
			processChildren(diffChild);
		}
	}

	private void processAnyOperation(Element operation) {
		if (ATTRIBUTES_ELEMENT.equals(operation.getLocalName())) {
			processAttributes(operation);
		} else {
			processOnlyElementOperation(operation);
		}
	}

	private void processOnlyElementOperation(Element operation) {
		processElementOperation(operation);
	}
	
	private void processAttributes(Element attributesOperation) {
		Element target = (Element) attributesOperation.getParentNode();

		processAttributesContents(attributesOperation, target);
		
		removeElementDeep(attributesOperation);
	}

	private void processAttributesContents(Element attributesOperation, Element target) {
		/* Note: attribute update operations are ordered:
		 * 
		 * <attributes> <remove .../> <add .../> </attributes>.
		 * 
		 * To apply in reverse mode, the operations must be traversed in reverse order to first
		 * remove attributes before setting their new values, because the same attribute may appear
		 * in the remove and add operation, if the attribute value should updated. */
		for (Node child : children(attributesOperation, _reverse)) {
			if (!(child instanceof Element)) {
				assertWhiteSpace(child);
				continue;
			}

			updateAttributes(target, (Element) child);
		}
	}

	private void updateAttributes(Element target, Element updateOperation) {
		boolean keep = getAttributeMode(updateOperation).shouldKeep(_reverse);

		NamedNodeMap attributes = updateOperation.getAttributes();
		for (int n = 0, cnt = attributes.getLength(); n < cnt; n++) {
			Node attribute = attributes.item(n);

			if (keep) {
				target.setAttributeNS(attribute.getNamespaceURI(), attribute.getLocalName(), attribute.getNodeValue());
			} else {
				target.removeAttributeNS(attribute.getNamespaceURI(), attribute.getLocalName());
			}
		}
	}

	private void assertWhiteSpace(Node child) {
		switch (child.getNodeType()) {
			case Node.COMMENT_NODE:
				return;
			case Node.TEXT_NODE:
				assertWhiteSpace(child.getTextContent());
				return;
			case Node.CDATA_SECTION_NODE:
				throw errorUnexpectedTextContent(child.getTextContent());
			default:
				throw errorUnexpectedNode(child);
		}
	}

	private void assertWhiteSpace(String textContent) {
		String trimmedText = textContent.trim();
		if (trimmedText.length() != 0) {
			throw errorUnexpectedTextContent(trimmedText);
		}
	}

	private void processElementOperation(Element elementOperation) {
		Mode mode = getElementMode(elementOperation);
		if (mode.shouldKeep(_reverse)) {
			processOperationChildren(elementOperation);
			removeElementShallow(elementOperation);
		} else {
			removeElementDeep(elementOperation);
		}
	}

	private void processOperationChildren(Element element) {
		Node nextChild;
		for (Node child = element.getFirstChild(); child != null; child = nextChild) {
			nextChild = child.getNextSibling();
			
			if (child instanceof Element) {
				processOperationChild((Element) child);
			}
		}
	}

	private void processOperationChild(Element element) {
		if (XDIFF_NS.equals(element.getNamespaceURI())) {
			throw errorUnexpectedDiffElement(element);
		} else {
			processChildren(element);
		}
	}

	private void processChildren(Element verbatimElement) {
		Node nextChild;
		for (Node verbatimChild = verbatimElement.getFirstChild(); verbatimChild != null; verbatimChild = nextChild) {
			nextChild = verbatimChild.getNextSibling();
			
			if (verbatimChild instanceof Element) {
				processVerbatimChild((Element) verbatimChild);
			}
		}
	}
	
	private void processVerbatimChild(Element verbatimChild) {
		if (XDIFF_NS.equals(verbatimChild.getNamespaceURI())) {
			processAnyOperation(verbatimChild);
		} else {
			processChildren(verbatimChild);
		}
	}

	private void removeElementDeep(Element element) {
		element.getParentNode().removeChild(element);
	}

	private void removeElementShallow(Element element) {
		Node parent = element.getParentNode();
		Node nextChild;
		for (Node child = element.getFirstChild(); child != null; child = nextChild) {
			nextChild = child.getNextSibling();
			
			element.removeChild(child);
			parent.insertBefore(child, element);
		}
		
		parent.removeChild(element);
	}
	
	private Mode getElementMode(Element element) {
		String localName = element.getLocalName();
		if (INSERT_ELEMENT.equals(localName)) {
			return Mode.INSERT;
		} else if (DELETE_ELEMENT.equals(localName)) {
			return Mode.DELETE;
		} else {
			throw new AssertionError("Unknown element operation: " + localName);
		}
	}

	private Mode getAttributeMode(Element element) {
		String localName = element.getLocalName();
		if (ADD_ELEMENT.equals(localName)) {
			return Mode.INSERT;
		} else if (REMOVE_ELEMENT.equals(localName)) {
			return Mode.DELETE;
		} else {
			throw new AssertionError("Unknown attribute operation: " + localName);
		}
	}

	private RuntimeException errorUnexpectedDiffElement(Element element) {
		return new XDiffSyntaxError("Unexpected diff element '" + element.getLocalName() + "'.");
	}

	private RuntimeException errorUnexpectedNode(Node node) {
		return new XDiffSyntaxError("Unexpected node '" + node + "'.");
	}

	private RuntimeException errorUnexpectedTextContent(String text) {
		return new XDiffSyntaxError("Unexpected text content '" + text + "'.");
	}

	/**
	 * Stand-alone utility to process an XML difference.
	 */
	public static void main(String[] args) throws Exception {
		DocumentBuilder documentBuilder = DOMUtil.newDocumentBuilderNamespaceAware();
		
		Document diff = documentBuilder.parse(args[0]);
		new XApply(false).apply(diff);
		
		DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
		DOMImplementation domImpl = registry.getDOMImplementation("XML 3.0");
		DOMImplementationLS ls = (DOMImplementationLS) domImpl.getFeature("LS", "3.0");
		LSSerializer lsSerializer = ls.createLSSerializer();
		
		LSOutput out = ls.createLSOutput();
		out.setByteStream(System.out);
		
		lsSerializer.write(diff, out);
	}
}
