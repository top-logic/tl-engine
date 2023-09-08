/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.xml.DOMUtil;

/**
 * Base class for {@link XMLRewriter}s that apply a XSLT transformation to given files.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class XMLTransformer extends XMLRewriter {

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
			Document document = DOMUtil.newDocument();
			Result result = new DOMResult(document);
			_tx.transform(new StreamSource(file), result);
			dump(document, file);
		} catch (Exception ex) {
			System.err.println("ERROR processing file: " + file);
			ex.printStackTrace(System.err);
		}
	}

}
