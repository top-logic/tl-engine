/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.top_logic.basic.io.binary.BinaryData;

/**
 * Utilities for evaluating XPath expressions.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class XPathUtil {

	/**
	 * Number of matches of the given expression in the given file.
	 * 
	 * @param file
	 *        the name of the file to search in.
	 * @param expressionSrc
	 *        The expression to count matches for.
	 * @return The number of matches.
	 */
	public static int matchCount(File file, String expressionSrc) throws XPathExpressionException,
			FileNotFoundException {
		return matchCount(streamInput(fileStream(file)), expressionSrc);
	}

	/**
	 * Number of matches of the given expression in the given file.
	 * 
	 * @param file
	 *        the name of the file to search in.
	 * @param expressionSrc
	 *        The expression to count matches for.
	 * @return The number of matches.
	 */
	public static int matchCount(BinaryData file, String expressionSrc) throws XPathExpressionException, IOException {
		return matchCount(streamInput(file.getStream()), expressionSrc);
	}

	/**
	 * Number of matches of the given expression in the given file.
	 * 
	 * @param context
	 *        The {@link InputSource} or {@link Node} to evaluate in, <code>null</code> for the
	 *        empty document.
	 * @param expressionSrc
	 *        The expression to count matches for.
	 * 
	 * @return The number of matches.
	 */
	public static int matchCount(Object context, String expressionSrc) throws XPathExpressionException {
		return evalInt(context, "count(" + expressionSrc + ")");
	}

	/**
	 * Evaluates the given expression to an integer result.
	 * 
	 * @param context
	 *        The {@link InputSource} or {@link Node} to evaluate in, <code>null</code> for the
	 *        empty document.
	 * @param expressionSrc
	 *        The expression to evaluate.
	 * 
	 * @return Evaluation result.
	 */
	public static int evalInt(Object context, String expressionSrc) throws XPathExpressionException {
		return evalNumber(context, expressionSrc).intValue();
	}

	/**
	 * Evaluates the given expression to a double result.
	 * 
	 * @param context
	 *        The {@link InputSource} or {@link Node} to evaluate in, <code>null</code> for the
	 *        empty document.
	 * @param expressionSrc
	 *        The expression to evaluate.
	 * 
	 * @return Evaluation result.
	 */
	public static double evalDouble(Object context, String expressionSrc) throws XPathExpressionException {
		return evalNumber(context, expressionSrc).doubleValue();
	}

	/**
	 * Evaluates the given expression to a numeric result.
	 * 
	 * @param context
	 *        The {@link InputSource} or {@link Node} to evaluate in, <code>null</code> for the
	 *        empty document.
	 * @param expressionSrc
	 *        The expression to evaluate.
	 * 
	 * @return Evaluation result.
	 */
	public static Number evalNumber(Object context, String expressionSrc) throws XPathExpressionException {
		return (Number) eval(context, expressionSrc, XPathConstants.NUMBER);
	}

	/**
	 * Evaluates the given expression to a string result.
	 * 
	 * @param context
	 *        The {@link InputSource} or {@link Node} to evaluate in, <code>null</code> for the
	 *        empty document.
	 * @param expressionSrc
	 *        The expression to evaluate.
	 * 
	 * @return Evaluation result.
	 */
	public static String evalString(Object context, String expressionSrc) throws XPathExpressionException {
		return (String) eval(context, expressionSrc, XPathConstants.STRING);
	}

	/**
	 * Evaluates the given expression to a boolean result.
	 * 
	 * @param context
	 *        The {@link InputSource} or {@link Node} to evaluate in, <code>null</code> for the
	 *        empty document.
	 * @param expressionSrc
	 *        The expression to evaluate.
	 * 
	 * @return Evaluation result.
	 */
	public static boolean evalBoolean(Object context, String expressionSrc) throws XPathExpressionException {
		return (Boolean) eval(context, expressionSrc, XPathConstants.BOOLEAN);
	}

	/**
	 * Evaluates the given expression to a result of type {@link Element}.
	 * 
	 * @param input
	 *        The document source to evaluate in.
	 * @param expressionSrc
	 *        The expression to evaluate.
	 * 
	 * @return Evaluation result.
	 */
	public static Element evalElement(InputSource input, String expressionSrc) throws XPathExpressionException {
		return (Element) evalNode(input, expressionSrc);
	}

	/**
	 * Evaluates the given expression to a result of type {@link Node}.
	 * 
	 * @param context
	 *        The {@link InputSource} or {@link Node} to evaluate in, <code>null</code> for the
	 *        empty document.
	 * @param expressionSrc
	 *        The expression to evaluate.
	 * 
	 * @return Evaluation result.
	 */
	public static Node evalNode(Object context, String expressionSrc) throws XPathExpressionException {
		return (Node) eval(context, expressionSrc, XPathConstants.NODE);
	}

	/**
	 * Evaluates the given expression to a result of type {@link NodeList}.
	 * 
	 * @param input
	 *        The document source to evaluate in.
	 * @param expressionSrc
	 *        The expression to evaluate.
	 * 
	 * @return Evaluation result.
	 */
	public static NodeList evalNodeList(InputSource input, String expressionSrc) throws XPathExpressionException {
		return (NodeList) parseAndEvalIn(input, expressionSrc, XPathConstants.NODESET);
	}

	private static Object eval(Object context, String expressionSrc, QName type) throws XPathExpressionException {
		if (context instanceof InputSource) {
			return parseAndEvalIn((InputSource) context, expressionSrc, type);
		} else {
			return evalIn((Node) context, expressionSrc, type);
		}
	}

	/**
	 * Evaluates the given expression to a result of the given type.
	 * 
	 * @param input
	 *        The document source to evaluate in.
	 * @param expressionSrc
	 *        The expression to evaluate.
	 * @param type
	 *        One of {@link XPathConstants}.
	 * 
	 * @return Evaluation result.
	 */
	public static Object parseAndEvalIn(InputSource input, String expressionSrc, QName type) throws XPathExpressionException {
		return safeEval(compile(expressionSrc), input, type);
	}

	/**
	 * XML eXternal Entity injection (XXE), which is now part of the OWASP Top 10, is a type of
	 * attack against an application that parses XML input. This attack occurs when untrusted XML
	 * input containing a reference to an external entity is processed by a weakly configured XML
	 * parser. This attack may lead to the disclosure of confidential data, denial of service,
	 * Server Side Request Forgery (SSRF), port scanning from the perspective of the machine where
	 * the parser is located, and other system impacts.
	 * 
	 * @see "https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet#Java"
	 */
	public static Object safeEval(XPathExpression expr, InputSource input, QName type)
			throws XPathExpressionException {
		try {
			// The given input source must be parsed with a safe parser to prevent XXE attacks.
			return expr.evaluate(DOMUtil.getDocumentBuilder().parse(input), type);
		} catch (SAXException | IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Evaluates the given expression to a result of the given type.
	 * 
	 * @param context
	 *        The {@link Node} context to evaluate in.
	 * @param expressionSrc
	 *        The expression to evaluate.
	 * @param type
	 *        One of {@link XPathConstants}.
	 * 
	 * @return Evaluation result.
	 */
	public static Object evalIn(Node context, String expressionSrc, QName type) throws XPathExpressionException {
		return compile(expressionSrc).evaluate(context, type);
	}

	/**
	 * Compiles the given XPath expression source.
	 */
	public static XPathExpression compile(String expressionSrc) throws XPathExpressionException {
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression expression = xPath.compile(expressionSrc);
		return expression;
	}

	/**
	 * {@link InputSource} for the given file name.
	 */
	public static InputSource fileInput(File file) throws FileNotFoundException {
		return streamInput(fileStream(file));
	}

	/**
	 * {@link InputStream} to the given file name.
	 */
	private static InputStream fileStream(File file) throws FileNotFoundException {
		return new FileInputStream(file);
	}

	/**
	 * {@link InputSource} for the given stream.
	 */
	public static InputSource streamInput(InputStream stream) {
		return new InputSource(stream);
	}

	/**
	 * {@link InputSource} for the given String.
	 */
	public static InputSource stringInput(String xml) {
		return readerInput(new StringReader(xml));
	}

	/**
	 * {@link InputSource} for the given character stream.
	 */
	public static InputSource readerInput(Reader reader) {
		return new InputSource(reader);
	}

}
