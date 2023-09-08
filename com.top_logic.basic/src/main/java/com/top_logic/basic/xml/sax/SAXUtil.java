/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml.sax;

import static java.util.Objects.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import com.top_logic.basic.Logger;

/**
 * Utilities for working with {@link SAXParser}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class SAXUtil {

	private static final SAXParserFactory PARSER_FACTORY = safeFactory();

	private static final SAXParserFactory PARSER_FACTORY_VALIDATING;

	static {
		PARSER_FACTORY_VALIDATING = safeFactory();
		PARSER_FACTORY_VALIDATING.setValidating(true);
	}

	private static final SAXParserFactory UNSAFE_PARSER_FACTORY_VALIDATING;

	static {
		UNSAFE_PARSER_FACTORY_VALIDATING = unsafeFactory();
		UNSAFE_PARSER_FACTORY_VALIDATING.setValidating(true);
	}

	private static final SAXParserFactory PARSER_FACTORY_NAMESPACE_AWARE;

	static {
		PARSER_FACTORY_NAMESPACE_AWARE = safeFactory();
		PARSER_FACTORY_NAMESPACE_AWARE.setNamespaceAware(true);
	}

	private static final SAXParserFactory UNSAVE_PARSER_FACTORY_NAMESPACE_AWARE;

	static {
		UNSAVE_PARSER_FACTORY_NAMESPACE_AWARE = unsafeFactory();
		UNSAVE_PARSER_FACTORY_NAMESPACE_AWARE.setNamespaceAware(true);
	}

	private static final SAXParserFactory PARSER_FACTORY_VALIDATING_NAMESPACE_AWARE;

	static {
		PARSER_FACTORY_VALIDATING_NAMESPACE_AWARE = safeFactory();
		PARSER_FACTORY_VALIDATING_NAMESPACE_AWARE.setValidating(true);
		PARSER_FACTORY_VALIDATING_NAMESPACE_AWARE.setNamespaceAware(true);
	}

	private static final SAXParserFactory UNSAVE_PARSER_FACTORY_VALIDATING_NAMESPACE_AWARE;

	static {
		UNSAVE_PARSER_FACTORY_VALIDATING_NAMESPACE_AWARE = unsafeFactory();
		UNSAVE_PARSER_FACTORY_VALIDATING_NAMESPACE_AWARE.setValidating(true);
		UNSAVE_PARSER_FACTORY_VALIDATING_NAMESPACE_AWARE.setNamespaceAware(true);
	}

	/**
	 * @see #hardenAgainsXXEAttacks(SAXParserFactory)
	 */
	private static SAXParserFactory safeFactory() {
		SAXParserFactory result = unsafeFactory();
		hardenAgainsXXEAttacks(result);
		return result;
	}

	private static SAXParserFactory unsafeFactory() {
		SAXParserFactory result = SAXParserFactory.newInstance();
		return result;
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
	private static void hardenAgainsXXEAttacks(SAXParserFactory result) {
		// This is the PRIMARY defense. If DTDs (doctypes) are disallowed, almost all XML entity
		// attacks are prevented
		// Xerces 2 only - http://xerces.apache.org/xerces2-j/features.html#disallow-doctype-decl
		setFeature(result, "http://apache.org/xml/features/disallow-doctype-decl", true);

		// If you can't completely disable DTDs, then at least do the following:
		// Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-general-entities
		// Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-general-entities
		// JDK7+ - http://xml.org/sax/features/external-general-entities
		setFeature(result, "http://xml.org/sax/features/external-general-entities", false);

		// Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-parameter-entities
		// Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-parameter-entities
		// JDK7+ - http://xml.org/sax/features/external-parameter-entities
		setFeature(result, "http://xml.org/sax/features/external-parameter-entities", false);

		// Disable external DTDs as well
		setFeature(result, "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

		// and these as well, per Timothy Morgan's 2014 paper: "XML Schema, DTD, and Entity Attacks"
		result.setXIncludeAware(false);
		// result.setExpandEntityReferences(false);

		// And, per Timothy Morgan: "If for some reason support for inline DOCTYPEs are a
		// requirement, then
		// ensure the entity settings are disabled (as shown above) and beware that SSRF attacks
		// (http://cwe.mitre.org/data/definitions/918.html) and denial
		// of service attacks (such as billion laughs or decompression bombs via "jar:") are a
		// risk."
	}

	private static void setFeature(SAXParserFactory factory, String feature, boolean value) {
		try {
			factory.setFeature(feature, value);
		} catch (SAXNotRecognizedException | SAXNotSupportedException | ParserConfigurationException ex) {
			Logger.error("Setting security-relevant feature '" + feature
				+ "' not supported, no protection agains XML external entity injection is provided.", SAXUtil.class);
		}
	}

	/**
	 * Creates a {@link SAXParser} from a {@link SAXParserFactory} with default settings.
	 * <p>
	 * The default settings are:
	 * <ul>
	 * <li>Not {@link SAXParserFactory#setValidating(boolean) validating}.</li>
	 * <li>Not {@link SAXParserFactory#setNamespaceAware(boolean) namespace aware}.</li>
	 * <li>Not {@link SAXParserFactory#setXIncludeAware(boolean) XML Inclusions aware}.</li>
	 * <li>No {@link SAXParserFactory#setSchema(javax.xml.validation.Schema) schema for validation}
	 * is set.</li>
	 * <li>No {@link SAXParserFactory#setFeature(String, boolean) feature} activated.</li>
	 * </ul>
	 * </p>
	 * 
	 * @return Never null.
	 */
	public static SAXParser newSAXParser() throws ParserConfigurationException, SAXException {
		return requireNonNull(PARSER_FACTORY.newSAXParser());
	}

	/**
	 * Variant of {@link #newSAXParser()} that {@link SAXParserFactory#setValidating(boolean) sets
	 * validating} to true.
	 * 
	 * @return Never null.
	 */
	public static SAXParser newSAXParserValidating() throws ParserConfigurationException, SAXException {
		return requireNonNull(PARSER_FACTORY_VALIDATING.newSAXParser());
	}

	/**
	 * Creates an unsafe {@link SAXParser} from with {@link SAXParserFactory#setValidating(boolean)
	 * validating} enabled.
	 * 
	 * <p>
	 * Note: Special care must be taken not to use the resulting parser for user-provided content.
	 * </p>
	 */
	public static SAXParser newUnsafeSAXParserValidating() throws ParserConfigurationException, SAXException {
		return requireNonNull(UNSAFE_PARSER_FACTORY_VALIDATING.newSAXParser());
	}

	/**
	 * Variant of {@link #newSAXParser()} that {@link SAXParserFactory#setNamespaceAware(boolean)
	 * sets namespace aware} to true.
	 * 
	 * @return Never null.
	 */
	public static SAXParser newSAXParserNamespaceAware() throws ParserConfigurationException, SAXException {
		return requireNonNull(PARSER_FACTORY_NAMESPACE_AWARE.newSAXParser());
	}

	/**
	 * Variant of {@link #newSAXParser()} that {@link SAXParserFactory#setNamespaceAware(boolean)
	 * sets namespace aware} to true.
	 * 
	 * <p>
	 * Note: Special care must be taken not to use the resulting parser for user-provided content.
	 * </p>
	 */
	public static SAXParser newUnsaveSAXParserNamespaceAware() throws ParserConfigurationException, SAXException {
		return requireNonNull(UNSAVE_PARSER_FACTORY_NAMESPACE_AWARE.newSAXParser());
	}

	/**
	 * Variant of {@link #newSAXParser()} that sets {@link SAXParserFactory#setValidating(boolean)
	 * validating} and {@link SAXParserFactory#setNamespaceAware(boolean) namespace aware} to true.
	 * 
	 * @return Never null.
	 */
	public static SAXParser newSAXParserValidatingNamespaceAware() throws ParserConfigurationException, SAXException {
		return requireNonNull(PARSER_FACTORY_VALIDATING_NAMESPACE_AWARE.newSAXParser());
	}

	/**
	 * Variant of {@link #newSAXParser()} that sets {@link SAXParserFactory#setValidating(boolean)
	 * validating} and {@link SAXParserFactory#setNamespaceAware(boolean) namespace aware} to true.
	 * 
	 * <p>
	 * Note: Special care must be taken not to use the resulting parser for user-provided content.
	 * </p>
	 */
	public static SAXParser newUnsaveSAXParserValidatingNamespaceAware() throws ParserConfigurationException, SAXException {
		return requireNonNull(UNSAVE_PARSER_FACTORY_VALIDATING_NAMESPACE_AWARE.newSAXParser());
	}

}
