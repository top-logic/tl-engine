/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.processor;

import static com.top_logic.basic.core.xml.DOMUtil.*;
import static com.top_logic.layout.processor.LayoutModelConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.ConfigurationSchemaConstants;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.xml.DOMUtil;

/**
 * Static utilities for processing layout definitions.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LayoutModelUtils {

	private static final String DEFINITION_FILES_SEPARATOR = ConfigurationReader.DEFINITION_FILES_SEPARATOR;

	/**
	 * Checks if the given {@link Node} specifies that an untyped layout template is used, i.e.
	 * included.
	 */
	public static boolean isInclude(Node node) {
		return DOMUtil.hasLocalName(LayoutModelConstants.INCLUDE_ELEMENT, node);
	}

	/**
	 * Checks if the given {@link Node} is a template call, i.e. an instantiation of a typed layout
	 * template.
	 */
	public static boolean isTemplateCall(Node node) {
		return LayoutModelConstants.TEMPLATE_CALL_ELEMENT.equals(node.getLocalName())
			&& ConfigurationSchemaConstants.CONFIG_NS.equals(node.getNamespaceURI());
	}

	/**
	 * Checks if the given {@link Node} is a template argument object.
	 */
	public static boolean isTemplateArgument(Node node) {
		return LayoutModelConstants.TEMPLATE_CALL_ARGUMENTS_ELEMENT.equals(node.getLocalName());
	}

	/**
	 * Checks if the given {@link Node} specifies the used typed layout template.
	 */
	public static boolean isTemplate(Node node) {
		return DOMUtil.hasLocalName(LayoutModelConstants.TEMPLATE_ELEMENT, node) && isRootNode(node);
	}

	private static boolean isRootNode(Node node) {
		Node parent = node.getParentNode();
		return parent == null || parent.getNodeType() == Node.DOCUMENT_NODE;
	}

	public static String getReferenceName(Element reference, Expansion contextExpansion) {
		if (isInclude(reference)) {
			String rawValue = reference.getAttributeNS(null, LayoutModelConstants.INCLUDE_NAME);
			return contextExpansion.expandString(null, LayoutModelConstants.INCLUDE_NAME, rawValue);
		}
		throw new IllegalArgumentException("Not a reference tag " + DOMUtil.toStringRaw(reference));
	}

	public static void stripComments(Node node) {
		Node nextSibling;
		for (Node child = node.getFirstChild(); child != null; child = nextSibling) {
			nextSibling = child.getNextSibling();
			
			switch (child.getNodeType()) {
				case Node.ELEMENT_NODE: {
					stripComments(child);
					break;
				}
				case Node.COMMENT_NODE: {
					node.removeChild(child);
					break;
				}
				case Node.TEXT_NODE:
				case Node.CDATA_SECTION_NODE: {
					String value = child.getNodeValue();
					if (isWhiteSpace(value)) {
						node.removeChild(child);
					}
					break;
				}
			}
		}
	}

	private static boolean isWhiteSpace(String value) {
		for (int n = 0, cnt = value.length(); n < cnt; n++) {
			char ch = value.charAt(n);
			if (!Character.isWhitespace(ch)) {
				return false;
			}
		}
		return true;
	}

	public static void setSourceAnnotation(Document layoutDocument, String definitionPath) {
		Element documentElement = layoutDocument.getDocumentElement();
		if (documentElement.getLocalName().equals(LayoutModelConstants.TEMPLATE_ELEMENT)) {
			for (Element contentElement : DOMUtil.elements(documentElement)) {
				if (!DOMUtil.isElement(null, LayoutModelConstants.PARAMS_ELEMENT, contentElement)) {
					annotateSource(contentElement, definitionPath);
				}
			}
		}
		annotateSource(documentElement, definitionPath);
	}

	private static void annotateSource(Element element, String definitionPath) {
		element.setAttributeNS(ANNOTATION_NS, DEFINITION_FILE_ANNOTATION_ATTR, definitionPath);
	}

	static void enhanceDefinitionFiles(Element parent, Iterable<? extends Node> children) {
		String parentSource;
		parentSource = getDirectSourceAnnotation(parent);
		if (StringServices.isEmpty(parentSource)) {
			return;
		}
		for (Node child : children) {
			if (!(child instanceof Element)) {
				continue;
			}
			String childSource = getDirectSourceAnnotation((Element) child);
			if (StringServices.isEmpty(childSource)) {
				continue;
			}
			annotateSource((Element) child, parentSource + DEFINITION_FILES_SEPARATOR + childSource);
		}
	}

	public static SourceLocation getSourceLocation(Node node) {
		SourceLocation result = null;
		while (node != null) {
			if (node instanceof Element) {
				String[] sources = getDirectSourceAnnotations((Element) node);
				for (int i = sources.length - 1; i >= 0; i--) {
					result = new SourceLocation(sources[i], result);
				}
			}

			node = node.getParentNode();
		}

		if (result == null) {
			return new SourceLocation("<source not available>", null);
		} else {
			return result;
		}
	}

	public static String getSourceAnnotations(Node node) {
		StringBuilder result = new StringBuilder();
		while (node != null) {
			if (node instanceof Element) {
				String[] sources = getDirectSourceAnnotations((Element) node);
				for (int i = sources.length - 1; i >= 0; i--) {
					if (result.length() > 0) {
						result.append(" referenced from ");
					}
					result.append(sources[i]);
				}
			}

			node = node.getParentNode();
		}
		return result.toString();
	}

	private static String[] getDirectSourceAnnotations(Element element) {
		return getDefinitionFiles(getDirectSourceAnnotation(element));
	}

	private static String getDirectSourceAnnotation(Element element) {
		return element.getAttributeNS(ANNOTATION_NS, DEFINITION_FILE_ANNOTATION_ATTR);
	}

	/**
	 * Splits the concatenated definition files read from an
	 * {@link LayoutModelConstants#DEFINITION_FILE_ANNOTATION_ATTR} into the separate definition
	 * files.
	 * 
	 * @param definitionFiles
	 *        A value read from the {@link LayoutModelConstants#DEFINITION_FILE_ANNOTATION_ATTR}
	 *        attribute. May be <code>null</code> or empty which leads to an empty result.
	 */
	public static String[] getDefinitionFiles(String definitionFiles) {
		if (StringServices.isEmpty(definitionFiles)) {
			return ArrayUtil.EMPTY_STRING_ARRAY;
		}
		return definitionFiles.split(DEFINITION_FILES_SEPARATOR);
	}

	public static void setVersionAnnotation(Document layoutDocument, String version) {
		Element documentElement = layoutDocument.getDocumentElement();
		documentElement.setAttributeNS(ANNOTATION_NS, DEFINITION_VERSION_ANNOTATION_ATTR, version.trim());
	}

	public static RuntimeException errorSetup(ModuleException ex) {
		throw error("Setup failed.", ex);
	}

	public static RuntimeException error(String message, Throwable ex) {
		throw (AssertionError) new AssertionError(message).initCause(ex);
	}

	public static String getIncludeId(Element reference) {
		return StringServices.nonEmpty(reference.getAttributeNS(null, LayoutModelConstants.INCLUDE_ID));
	}

	public static Map<String, ParameterValue> getArguments(Node reference, Expansion argumentExpansion) {
		HashMap<String, ParameterValue> arguments = new HashMap<>();
		NamedNodeMap attributes = reference.getAttributes();
		for (int n = 0, cnt = attributes.getLength(); n < cnt; n++) {
			Attr attr = (Attr) attributes.item(n);
			if (!StringServices.isEmpty(attr.getNamespaceURI())) {
				continue;
			}

			String name = attr.getName();
			if (LayoutModelConstants.INCLUDE_NAME.equals(name) || LayoutModelConstants.INCLUDE_ID.equals(name)) {
				continue;
			}

			arguments.put(name, new StringValue(name, attr.getTextContent()));
		}

		for (Element param : elements(reference)) {
			String paramName = param.getLocalName();
			if (LayoutModelConstants.INJECT_ELEMENT.equals(paramName)) {
				continue;
			}

			arguments.put(paramName, RangeValue.createArgumentValue(paramName, param));
		}
		return arguments;
	}

	public static String getComponentName(Element component) {
		return component.getAttributeNS(null, LayoutModelConstants.COMPONENT_NAME);
	}

	public static String getComponentClass(Element component) {
		return component.getAttributeNS(null, LayoutModelConstants.CLASS_ATTRIBUTE);
	}

	public static void setInlinedAnnotation(Document document) {
		Element documentElement = document.getDocumentElement();

		// In case of failure, no document element might be produced.
		if (documentElement != null) {
			documentElement.setAttributeNS(ANNOTATION_NS, INLINED_ANNOTATION_ATTR, INLINED_ANNOTATION_VALUE);
		}
	}

	public static boolean isInlined(Document document) {
		return INLINED_ANNOTATION_VALUE.equals(
			document.getDocumentElement().getAttributeNS(ANNOTATION_NS, INLINED_ANNOTATION_ATTR));
	}

}
