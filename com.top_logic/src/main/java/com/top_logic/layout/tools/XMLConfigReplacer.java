/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.collections4.iterators.FilterIterator;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.XMLPrettyPrinter;

/**
 * Structured search and replace tool for XML files.
 * 
 * <p>
 * Usage: <code>java XMLConfigReplacer [instructionsFile] [xmlFileOrDir]</code>
 * </p>
 * 
 * <p>
 * An instructions file may be loaded from the classpath by adding the prefix
 * <code>classpath:</code> to the absolute resource name of the class path resource with the
 * instructions.
 * </p>
 * 
 * <p>
 * An instructions file must have the following schema:
 * </p>
 * 
 * <pre>
 * <xmp>
 * <instructions xmlns:config="http://www.top-logic.com/ns/config/6.0">
 *     <instruction>
 *         <search>
 *             <pattern-xml-element/>
 *         </search>
 *         <replace>
 *             <xml-element-to-insert/>
 *         </replace>
 *     </instruction>
 *     ...
 * </instructions>
 * </xmp>
 * </pre>
 */
public class XMLConfigReplacer {

	/**
	 * Creates a {@link XMLConfigReplacer}.
	 */
	public XMLConfigReplacer(String instructions, File rootDir) {
		this.instructionsFile = instructions;
		this.rootDir = rootDir;
	}

	private File rootDir;

	private String instructionsFile;

	private Document instructions;

	private void run() throws IOException, SAXException, ParserConfigurationException {
		instructions = load(instructionsFile);
		scan(rootDir);
	}

	private void scan(File file) throws IOException, SAXException, ParserConfigurationException {
		if (!file.exists()) {
			System.err.println("ERROR: File does not exist: " + file.getPath());
			return;
		}
		if (file.isDirectory()) {
			scanDir(file);
		} else {
			process(file);
		}
	}

	private void scanDir(File dir) throws IOException, SAXException, ParserConfigurationException {
		for (File file : FileUtilities.listFiles(dir)) {
			scan(file);
		}
	}

	private void process(File file) throws SAXException, IOException, ParserConfigurationException {
		if (!file.getName().endsWith(".xml")) {
			System.err.println("Ignoring: " + file.getPath());
			return;
		}

		System.err.println("Processing: " + file.getPath());

		Document document = load(file);
		process(document);
		XMLPrettyPrinter.dump(new FileOutputStream(file), document);
	}

	private Document load(String fileName) throws SAXException, IOException, ParserConfigurationException {
		if (fileName.startsWith("classpath:")) {
			try (InputStream in =
				XMLConfigReplacer.class.getResourceAsStream(fileName.substring("classpath:".length()))) {
				return builder().parse(in);
			}
		} else {
			return load(new File(fileName));
		}
	}

	private DocumentBuilder builder() throws ParserConfigurationException {
		return DocumentBuilderFactory.newDefaultNSInstance().newDocumentBuilder();
	}

	private Document load(File file) throws SAXException, IOException, ParserConfigurationException {
		return builder().parse(file);
	}

	private void process(Document document) {
		for (Element instruction : DOMUtil.elements(instructions.getDocumentElement())) {
			Element search = firstElement(DOMUtil.elementsNS(instruction, null, "search").iterator().next());
			Element replace = firstElement(DOMUtil.elementsNS(instruction, null, "replace").iterator().next());

			new Replacer(search, replace).findAndReplace(document.getDocumentElement());
		}
	}

	private Element firstElement(Element root) {
		return DOMUtil.elements(root).iterator().next();
	}

	private static class Replacer {
		private Element _search;

		private Element _replace;

		public Replacer(Element search, Element replace) {
			_search = search;
			_replace = replace;
		}

		public void findAndReplace(Element element) {
			for (Node child = element.getFirstChild(),
					next = child == null ? null : child.getNextSibling(); child != null; child = next, next =
					child == null ? null : child.getNextSibling()) {

				if (child instanceof Element sub) {
					if (matchesSearch(sub)) {
						Node replacement = element.getOwnerDocument().importNode(_replace, true);
						element.insertBefore(replacement, child);
						element.removeChild(sub);
					} else {
						findAndReplace(sub);
					}
				}
			}
		}

		private boolean matchesSearch(Element element) {
			return matchesElement(element, _search);
		}

		private boolean matchesElement(Element actual, Element expected) {
			if (!Objects.equals(actual.getNamespaceURI(), expected.getNamespaceURI())) {
				return false;
			}
			if (!Objects.equals(actual.getLocalName(), expected.getLocalName())) {
				return false;
			}

			NamedNodeMap actualAttrs = actual.getAttributes();
			if (actualAttrs.getLength() != expected.getAttributes().getLength()) {
				return false;
			}
			for (int cnt = actualAttrs.getLength(), n = 0; n < cnt; n++) {
				Node actualAttr = actualAttrs.item(n);
				Attr expectedAttr =
					expected.getAttributeNodeNS(actualAttr.getNamespaceURI(), actualAttr.getLocalName());
				if (expectedAttr == null) {
					return false;
				}
				if (!Objects.equals(actualAttr.getNodeValue(), expectedAttr.getValue())) {
					return false;
				}
			}

			Iterator<Node> expectedChildren =
				new FilterIterator<>(DOMUtil.children(expected).iterator(), this::isRelevant);
			for (Node actualChild = actual.getFirstChild(); actualChild != null; actualChild =
				actualChild.getNextSibling()) {
				if (isIgnored(actualChild)) {
					continue;
				}

				if (!expectedChildren.hasNext()) {
					return false;
				}

				Node expectedChild = expectedChildren.next();
				if (!matches(actualChild, expectedChild)) {
					return false;
				}
			}
			if (expectedChildren.hasNext()) {
				return false;
			}

			return true;
		}

		private boolean matches(Node actual, Node expected) {
			if (actual instanceof Element actualElement) {
				if (expected instanceof Element expectedElement) {
					return matchesElement(actualElement, expectedElement);
				} else {
					return false;
				}
			}
			else if (actual instanceof Text actualText) {
				if (expected instanceof Text expectedText) {
					return matchesText(actualText, expectedText);
				} else {
					return false;
				}
			}
			return false;
		}

		private boolean matchesText(Text actualText, Text expectedText) {
			String actualContent = actualText.getTextContent().strip().replaceAll("\\s+", " ");
			String expectedContent = expectedText.getTextContent().strip().replaceAll("\\s+", " ");

			return Objects.equals(actualContent, expectedContent);
		}

		private boolean isRelevant(Node node) {
			return !isIgnored(node);
		}

		private boolean isIgnored(Node node) {
			return node instanceof Comment || (node instanceof Text text && text.getTextContent().isBlank());
		}
	}
	
	public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
		new XMLConfigReplacer(args[0], new File(args[1])).run();
	}
}

