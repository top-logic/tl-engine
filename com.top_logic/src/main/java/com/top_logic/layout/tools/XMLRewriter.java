/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.basic.xml.XMLPrettyPrinter.Config;

/**
 * Base class for {@link Rewriter}s processing XML documents.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class XMLRewriter extends Rewriter {

	/**
	 * Parses the given XML file.
	 */
	protected final Document parse(File file) throws SAXException, IOException {
		return DOMUtil.getDocumentBuilder().parse(file);
	}

	/**
	 * Dumps the given {@link Document} to the given file.
	 */
	protected final void dump(Document document, File file) throws FileNotFoundException, IOException {
		// Join adjacent text nodes to be able to format them during pretty printing below.
		document.normalize();

		makeCData(document.getDocumentElement());

		XMLPrettyPrinter.updateIfChanged(prettyPrinterConfig(), file, document);
	}

	private void makeCData(Element element) {
		for (Node child = element.getFirstChild(),
				next = child == null ? null : child.getNextSibling(); child != null; child = next, next =
					next == null ? null : next.getNextSibling()) {
			if (child instanceof Text text) {
				String str = text.getTextContent();
				if (str.contains("&") || str.contains("<") || str.contains(">") || str.contains("\\n")) {
					CDATASection cdata = element.getOwnerDocument().createCDATASection(str);
					element.insertBefore(cdata, text);
					element.removeChild(text);
				}
			}
			else if (child instanceof Element sub) {
				makeCData(sub);
			}
		}
	}

	/**
	 * The {@link XMLPrettyPrinter} configuration to use.
	 */
	protected Config prettyPrinterConfig() {
		return XMLPrettyPrinter.newConfiguration();
	}

}
