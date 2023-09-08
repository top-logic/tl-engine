/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.top_logic.basic.Main;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.XMLPrettyPrinter;

/**
 * Transforms XML files using XPath expressions.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public abstract class XPathFileTransformer extends Main implements FileHandler {

	private XPathExpression _expression;

	/**
	 * Creating a {@link XPathFileTransformer} instance.
	 */
	public XPathFileTransformer() throws XPathExpressionException {
		_expression = XPathFactory.newInstance().newXPath().compile(getXPathExpression());
	}

	@Override
	protected boolean argumentsRequired() {
		return true;
	}

	@Override
	protected int parameter(String[] args, int i) throws Exception {
		FileUtil.handleFile(args[i++], this);
		return i;
	}

	@Override
	protected void doActualPerformance() throws Exception {
		// Nothing more.
	}

	@Override
	public void handleFile(String fileName) throws Exception {
		File root = new File(fileName);

		try (Stream<Path> paths = Files.walk(root.toPath())) {
			paths.filter(Files::isRegularFile).forEach(path -> {
				if (path.toString().endsWith(FileUtilities.XML_FILE_ENDING)) {
					transform(path.toFile());
				}
			});
		}
	}

	private void transform(File file) {
		try {
			Document document = parse(file);
			NodeList nodes = findNodes(document);
			transform(nodes);
			dump(document, file);

			if (nodes.getLength() > 0) {
				System.out.println("File '" + file.getAbsolutePath() + "' transformed.");
			}
		} catch (IOException | SAXException | XPathExpressionException exception) {
			System.err.println("XML transformation of file '" + file.getAbsolutePath() + "' failed.");

			exception.printStackTrace(System.err);
		}
	}

	private NodeList findNodes(Document document) throws XPathExpressionException {
		return (NodeList) _expression.evaluate(document, XPathConstants.NODESET);
	}

	private static Document parse(File file) throws SAXException, IOException {
		return DOMUtil.getDocumentBuilder().parse(file);
	}

	private static void dump(Document document, File file) throws FileNotFoundException, IOException {
		try (OutputStream out = new FileOutputStream(file)) {
			XMLPrettyPrinter.dump(out, document);
		}
	}

	/**
	 * XPath expression to determine all {@link Node}s that should be transformed.
	 */
	protected abstract String getXPathExpression();

	/**
	 * Transformation executed on all nodes found by the given XPath expression.
	 */
	protected abstract void transform(NodeList nodes);

}
