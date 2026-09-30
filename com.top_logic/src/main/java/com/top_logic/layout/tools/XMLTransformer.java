/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.xml.DOMUtil;

/**
 * Base class for {@link XMLRewriter}s that apply a XSLT transformation to given files.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class XMLTransformer extends XMLRewriter {

	/**
	 * Namespace for the {@link #CDATA_QUOTE} element.
	 */
	public static final String TRANSFORMER_NS = "http://www.top-logic.com/ns/xslt/1.0";

	/**
	 * Element to wrap CDATA section in.
	 * 
	 * <p>
	 * This allows to keep CDATA sections during transformation (or even explicitly create those).
	 * </p>
	 */
	public static final String CDATA_QUOTE = "cdata-text";

	private final Transformer _tx;

	/**
	 * Creates a {@link XMLTransformer}.
	 */
	public XMLTransformer() throws TransformerConfigurationException {
		_tx = TransformerFactory.newInstance().newTransformer(txSource());
	}

	/**
	 * Builds the {@link Source} for loading the {@link Transformer}.
	 */
	protected abstract Source txSource();

	@Override
	public void handleFile(String fileName) throws Exception {
		handleFile(new File(fileName));
	}

	private void handleFile(File file) throws IOException, TransformerException {
		if (!file.exists()) {
			System.err.println("File does not exist: " + file.getPath());
			return;
		}
		if (file.isDirectory()) {
			descend(file);
		} else {
			process(file);
		}
	}

	private void descend(File dir) throws IOException, TransformerException {
		for (File file : FileUtilities.listFiles(dir)) {
			handleFile(file);
		}
	}

	private void process(File file) {
		if (!file.getName().endsWith(".xml")) {
			return;
		}

		try {
			Document input = loadDocument(file);
			quoteCData(input.getDocumentElement());
			Document output = DOMUtil.newDocument();
			_tx.transform(new DOMSource(input), new DOMResult(output));

			// Join adjacent text nodes to be able to format them during pretty printing below.
			output.normalize();
			makeCData(output.getDocumentElement());

			dump(output, file);
		} catch (Exception ex) {
			System.err.println("ERROR processing file: " + file);
			ex.printStackTrace(System.err);
		}
	}

	/**
	 * Wraps CDATA nodes into <code>cdata-text</code> elements to allow keeping CDATA sections
	 * during transformation.
	 */
	protected void quoteCData(Element element) {
		for (Node child = element.getFirstChild(),
				next = child == null ? null : child.getNextSibling(); child != null; child = next, next =
					next == null ? null : next.getNextSibling()) {
			if (child instanceof CDATASection text) {
				Element quote = element.getOwnerDocument().createElementNS(TRANSFORMER_NS, CDATA_QUOTE);
				element.insertBefore(quote, text);
				element.removeChild(text);
				quote.appendChild(text);
			} else if (child instanceof Element sub) {
				quoteCData(sub);
			}
		}
	}

	/**
	 * Unquotes CDATA sections in the given result element.
	 */
	protected void makeCData(Element element) {
		for (Node child = element.getFirstChild(),
				next = child == null ? null : child.getNextSibling(); child != null; child = next, next =
					next == null ? null : next.getNextSibling()) {
			if (child instanceof Text text) {
				String str = text.getTextContent();
				// Note: When quoting newlines with CDATA, even multiple variable expansions
				// separated by newline would get quoted into a CDATA section. The following
				// behavior is "almost" compatible with XMLPrettyPrinter.
				if (str.contains("<")) {
					CDATASection cdata = element.getOwnerDocument().createCDATASection(str);
					element.insertBefore(cdata, text);
					element.removeChild(text);
				}
			} else if (child instanceof Element sub) {
				if (CDATA_QUOTE.equals(sub.getLocalName()) && TRANSFORMER_NS.equals(sub.getNamespaceURI())) {
					for (Node text = sub.getFirstChild(),
							nextText = text == null ? null : text.getNextSibling(); text != null; text =
								nextText, nextText = text == null ? null : text.getNextSibling()) {
						sub.removeChild(text);

						Node cdata;
						if (text instanceof Text quoted) {
							cdata = element.getOwnerDocument().createCDATASection(quoted.getTextContent());
						} else {
							cdata = text;
						}
						element.insertBefore(cdata, sub);
					}
					element.removeChild(sub);
				}
				makeCData(sub);
			}
		}
	}

	/**
	 * Loads the given XML file.
	 */
	protected Document loadDocument(File file) throws SAXException, IOException, ParserConfigurationException {
		return DocumentBuilderFactory.newDefaultNSInstance().newDocumentBuilder().parse(file);
	}

}
