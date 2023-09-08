/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import com.top_logic.basic.ExceptionUtil;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.MultiError;
import com.top_logic.basic.Settings;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.Content;
import com.top_logic.basic.io.FileBasedBinaryContent;

/**
 * Methods for applying an XSLT-Script to a xml file.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class XsltUtil {

	private static final int INDENT = 2;

	/**
	 * Applies the XSLT in the transformerFilename to the xml in the sourceFilename and writes the
	 * result in the outputFilename.
	 * 
	 * @param indent
	 *        Whether to properly indent the result.
	 * 
	 */
	public static void transformFile(String sourceFilename, String transformerFilename, String outputFilename, boolean indent) {
		try {
			try (FileInputStream sourceIn = new FileInputStream(sourceFilename)) {
				try (FileInputStream transformerIn = new FileInputStream(transformerFilename)) {
					try (FileOutputStream out = new FileOutputStream(outputFilename)) {
						transform(
							new StreamSource(sourceIn),
							new StreamSource(transformerIn),
							new StreamResult(out), indent);
					}
				}
			}
		} catch (Throwable throwable) {
			throw new RuntimeException("Transformation of '" + sourceFilename + "' via '" + transformerFilename + "' to '" + outputFilename + "' failed!", throwable);
		}
	}

	/**
	 * Applies the XSLT in the transformerSource to the xml in the inputSource and writes the result
	 * in the outputResult.
	 * 
	 * @param indent
	 *        Whether to properly indent the result.
	 */
	public static void transform(Source inputSource, Source transformerSource, Result outputResult, boolean indent) {
		try {
			Transformer transformer = createTransformer(transformerSource, indent);
			// Setting the indent succeeded at least at one of the two ways.
			transformer.transform(inputSource, outputResult);
		} catch (Throwable throwable) {
			throw new RuntimeException("Transformation of '" + inputSource.getSystemId() + "' via '"
				+ transformerSource.getSystemId() + "' to '" + outputResult.getSystemId() + "' failed! "
				+ throwable.getMessage(), throwable);
		}
	}

	/**
	 * Applies the XSLT in the transformerSource to the xml in the inputSource and writes the result
	 * in the outputResult.
	 * 
	 * <p>
	 * Note: The given transformer source must be loaded from a secure location and must not contain
	 * user input.
	 * </p>
	 * 
	 * @param indent
	 *        Whether to properly indent the result.
	 */
	public static void transformUnsafe(Source inputSource, Source transformerSource, Result outputResult, boolean indent) {
		try {
			Transformer transformer = createUnsafeTransformer(transformerSource, indent);
			// Setting the indent succeeded at least at one of the two ways.
			transformer.transform(inputSource, outputResult);
		} catch (Throwable throwable) {
			throw new RuntimeException("Transformation of '" + inputSource.getSystemId() + "' via '"
				+ transformerSource.getSystemId() + "' to '" + outputResult.getSystemId() + "' failed! "
				+ throwable.getMessage(), throwable);
		}
	}

	/**
	 * Creates a {@link Transformer} from a resource picked up from the {@link FileManager}.
	 *
	 * @param resource
	 *        The XSLT transformation resource, see {@link FileManager#getStream(String)}
	 * @param indent
	 *        Whether to properly indent the result.
	 * @return The {@link Transformer} built from the given resource.
	 */
	public static Transformer createTransformer(String resource, boolean indent) {
		try {
			return createTransformer(new StreamSource(FileManager.getInstance().getStream(resource)), indent);
		} catch (TransformerConfigurationException ex) {
			throw new IllegalArgumentException("Invalid transformation '" + resource + "'.", ex);
		} catch (TransformerFactoryConfigurationError ex) {
			throw new IllegalStateException("Invalid transformer configuration.", ex);
		} catch (IOException ex) {
			throw new IllegalArgumentException("Cannot read transformation '" + resource + "'.", ex);
		}
	}

	/**
	 * Creates a {@link Transformer} from the given {@link Source}.
	 * 
	 * @param indent
	 *        Whether to properly indent the result.
	 */
	public static Transformer createTransformer(Source transformerSource, boolean indent)
			throws TransformerFactoryConfigurationError, TransformerConfigurationException {
		return createTransformer(safeTransformerFactory(), transformerSource, indent);
	}

	/**
	 * Creates a {@link Transformer} from the given {@link Source} that has secure processing
	 * disabled.
	 * 
	 * <p>
	 * Note: The given transformer source must be loaded from a secure location and must not contain
	 * user input.
	 * </p>
	 * 
	 * @param indent
	 *        Whether to properly indent the result.
	 */
	public static Transformer createUnsafeTransformer(Source transformerSource, boolean indent)
			throws TransformerFactoryConfigurationError, TransformerConfigurationException {
		return createTransformer(unsafeTransformerFactory(), transformerSource, indent);
	}

	private static Transformer createTransformer(TransformerFactory transformerFactory, Source transformerSource, boolean indent)
			throws TransformerConfigurationException {
		if (indent) {
			IllegalArgumentException indentFailureOld = DOMUtil.setIndentOlderXalan(transformerFactory, INDENT);
			Transformer transformer = transformerFactory.newTransformer(transformerSource);
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			IllegalArgumentException indentFailureNew = DOMUtil.setIndentNewerXalan(transformer, INDENT);
			if ((indentFailureNew != null) && (indentFailureOld != null)) {
				String message = "Setting the indent failed on both the old and the new way.";
				MultiError error = ExceptionUtil.createMultiError(message, indentFailureNew, indentFailureOld);
				Logger.debug(message, error, XsltUtil.class);
			}
			return transformer;
		} else {
			return transformerFactory.newTransformer(transformerSource);
		}
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
	public static TransformerFactory safeTransformerFactory() throws TransformerFactoryConfigurationError {
		TransformerFactory result = unsafeTransformerFactory();
		setFeature(result, XMLConstants.FEATURE_SECURE_PROCESSING, true);
		return result;
	}

	/**
	 * Creates a {@link TransformerFactory} that creates transformers that are prone to XXE attacks.
	 * 
	 * <p>
	 * Care must be taken, that the resulting factory is not used in context, where the style sheet
	 * contains user input.
	 * </p>
	 * 
	 * @see #safeTransformerFactory()
	 */
	public static TransformerFactory unsafeTransformerFactory() throws TransformerFactoryConfigurationError {
		return TransformerFactory.newDefaultInstance();
	}

	private static void setFeature(TransformerFactory result, String name, boolean value) {
		try {
			result.setFeature(name, value);
		} catch (TransformerConfigurationException ex) {
			Logger.error("Setting security-relevant transformer feature '" + name
				+ "' not supported, no protection agains XML external entity injection is provided.", XsltUtil.class);
		}
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
	public static Validator safeValidator(Schema schema) {
		Validator validator = schema.newValidator();
		hardenAgainsXXEAttacks(validator);
		return validator;
	}

	private static void hardenAgainsXXEAttacks(Validator validator) {
		setProperty(validator, XMLConstants.ACCESS_EXTERNAL_DTD, "");
		setProperty(validator, XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
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
	public static SchemaFactory safeSchemaFactory() throws SAXNotRecognizedException, SAXNotSupportedException {
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
		return schemaFactory;
	}

	private static void setProperty(Validator validator, String name, String value) {
		try {
			validator.setProperty(name, value);
		} catch (SAXNotRecognizedException | SAXNotSupportedException ex) {
			Logger.error("Setting security-relevant validator feature '" + name
				+ "' not supported, no protection agains XML external entity injection is provided.", XsltUtil.class);
		}
	}

	/**
	 * Utility to transform an abstract {@link Content}.
	 * 
	 * @param source
	 *        The {@link Content} to transform.
	 * @param transformer
	 *        {@link Transformer} to use.
	 * @return The transformed {@link Content}.
	 */
	public static BinaryContent transform(Content source, Transformer transformer)
			throws IOException, TransformerException {
		File outFile = File.createTempFile(plainName(source.toString()), ".xml", Settings.getInstance().getTempDir());
		try (FileOutputStream out = new FileOutputStream(outFile)) {
			transformer.transform(XMLContent.toSource(source), new StreamResult(out));
		}
		return FileBasedBinaryContent.createBinaryContent(outFile);
	}

	private static String plainName(String name) {
		int from = Math.max(Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\')), name.lastIndexOf(':'));
		if (from < 0) {
			from = 0;
		} else {
			// Skip last special char.
			from++;
		}

		int to;
		if (name.endsWith(".xml")) {
			to = name.length() - ".xml".length();
		} else {
			to = name.length();
		}

		return name.substring(from, to);
	}

}
