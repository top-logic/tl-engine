/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.w3c.dom.Document;
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

		XMLPrettyPrinter.updateIfChanged(prettyPrinterConfig(), file, document);
	}

	/**
	 * The {@link XMLPrettyPrinter} configuration to use.
	 */
	protected Config prettyPrinterConfig() {
		return XMLPrettyPrinter.newConfiguration();
	}

}
