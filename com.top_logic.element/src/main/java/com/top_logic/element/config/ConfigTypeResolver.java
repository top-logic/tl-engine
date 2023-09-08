/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config;

import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.element.meta.AttributeSettings;
import com.top_logic.element.meta.expr.internal.OperationFactory;
import com.top_logic.element.meta.expr.parser.ExpressionParser;
import com.top_logic.element.meta.expr.parser.ParseException;
import com.top_logic.element.meta.kbbased.filtergen.AbstractAttributeValueLocator;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocator;
import com.top_logic.element.meta.kbbased.filtergen.PathAttributeValueLocatorInvertable;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.config.EnumConfig;
import com.top_logic.util.list.ListInitializationUtil;

/**
 * XSLT plugin to select a {@link AttributeConfig} interface name for a given {@link TLStructuredTypePart}
 * implementation type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConfigTypeResolver {

	/**
	 * Normalized (human-readable) version of the implementation type.
	 */
	@CalledByReflection
	public static String normalizeImplementationType(String implementationTypeName) {
		try {
			return tryNormalize(implementationTypeName);
		} catch (Throwable ex) {
			// Log problem, since this method is called directly from the XSLT transformer, which
			// hides the problem.
			Logger.error("Cannot normalize implementation type code: " + implementationTypeName, ex,
				ConfigTypeResolver.class);
			return implementationTypeName;
		}
	}

	private static String tryNormalize(String implementationTypeName) {
		if (implementationTypeName.startsWith("TYPE_")) {
			return implementationTypeName.substring("TYPE_".length());
		} else {
			// Type was given as type code number, reconstruct the name.
			int implementationId = AttributeSettings.getInstance().getImplementationId(implementationTypeName);
			return AttributeSettings.getInstance().getTypeAsString(implementationId);
		}
	}

	/**
	 * Whether the given classification is marked as multi-select by default.
	 */
	@CalledByReflection
	public static boolean isMultiSelect(String classificationName) {
		try {
			return ListInitializationUtil.getMultiSelectAnnotation(getEnumConfig(classificationName));
		} catch (IOException ex) {
			throw errorCannotDetermineMultiselect(classificationName, ex);
		} catch (ConfigurationException ex) {
			throw errorCannotDetermineMultiselect(classificationName, ex);
		}
	}

	private static EnumConfig getEnumConfig(String classificationName) throws IOException, ConfigurationException {
		checkClassificationName(classificationName);
		// Note: The model is not yet set up during parsing definition files. Therefore, the
		// transformation must solely rely on configuration files, even for enumerations.
		EnumConfig enumConfig = ListInitializationUtil.loadEnumConfig(classificationName);
		return enumConfig;
	}

	private static void checkClassificationName(String classificationName) {
		if (StringServices.isEmpty(classificationName)) {
			throw new IllegalArgumentException(
				"FastList name is not specified or the 'classification:' before it is missing."
					+ " (on a metaattribute of type 'TYPE_CLASSIFICATION')");
		}
	}

	private static ConfigurationError errorCannotDetermineMultiselect(String classificationName, Exception ex) {
		return new ConfigurationError("Cannot determine multi-select annotation of '" + classificationName + "'.", ex);
	}

	/**
	 * Whether the given classification is marked as unordered.
	 */
	@CalledByReflection
	public static boolean isUnordered(String classificationName) {
		try {
			return ListInitializationUtil.getUnorderedAnnotation(getEnumConfig(classificationName)) != null;
		} catch (IOException ex) {
			throw errorCannotDetermineUnordered(classificationName, ex);
		} catch (ConfigurationException ex) {
			throw errorCannotDetermineUnordered(classificationName, ex);
		}
	}

	private static ConfigurationError errorCannotDetermineUnordered(String classificationName, Exception ex) {
		return new ConfigurationError("Cannot determine unordered annotation of '" + classificationName + "'.", ex);
	}

	/**
	 * Transforms the configuration of a {@link PathAttributeValueLocatorInvertable} to new syntax:
	 * "-&lt;element&gt;:&lt;attribute&gt;" must not be "-me:&lt;element&gt;#&lt;attribute&gt;"
	 */
	@CalledByReflection
	public static String transformPathAttributeValueLocaterInvertable(String path) {
		Matcher matcher = Pattern.compile("(?<=PathInverted\\(['\"]).*(?=['\"]\\))").matcher(path);
		StringBuffer out = new StringBuffer();
		while (matcher.find()) {
			String newPath = PathAttributeValueLocatorInvertable.getNewPath(matcher.group());
			matcher.appendReplacement(out, newPath);
		}
		matcher.appendTail(out);
		return out.toString();
	}

	/**
	 * @see AbstractAttributeValueLocator#isBackReferenceLocator(AttributeValueLocator)
	 */
	@CalledByReflection
	public static boolean locatorIsReference(String locatorExpression) {
		return AbstractAttributeValueLocator.isBackReferenceLocator(parseLocator(locatorExpression));
	}

	/**
	 * @see AbstractAttributeValueLocator#getLocatorValueTypeSpec(AttributeValueLocator)
	 */
	@CalledByReflection
	public static String locatorType(String locatorExpression) {
		return AbstractAttributeValueLocator.getLocatorValueTypeSpec(parseLocator(locatorExpression));
	}

	/**
	 * @see AbstractAttributeValueLocator#isCollectionLocator(AttributeValueLocator)
	 */
	@CalledByReflection
	public static boolean locatorIsMultiple(String locatorExpression) {
		return AbstractAttributeValueLocator.isCollectionLocator(parseLocator(locatorExpression));
	}

	/**
	 * @see AbstractAttributeValueLocator#getLocatorReverseEndSpec(AttributeValueLocator)
	 */
	@CalledByReflection
	public static String locatorReverseEnd(String locatorExpression) {
		return AbstractAttributeValueLocator.getLocatorReverseEndSpec(parseLocator(locatorExpression));
	}

	/**
	 * Parses the given {@link AttributeValueLocator} expression.
	 * 
	 * @param expressionSource
	 *        See {@link ExpressionParser}.
	 * @return The instantiated {@link AttributeValueLocator}.
	 */
	public static AttributeValueLocator parseLocator(String expressionSource) {
		ExpressionParser parser = new ExpressionParser(new StringReader(expressionSource));
		OperationFactory operations = new OperationFactory();
		parser.setOperationFactory(operations);
		try {
			return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(parser.expr());
		} catch (ParseException ex) {
			throw new IllegalArgumentException("Invalid locator expression '" + expressionSource + "'.", ex);
		}
	}

	@CalledByReflection
	public static String interfaceType(String moName) {
		return ApplicationObjectUtil.TL_TABLES_MODULE + ":"
			+ ApplicationObjectUtil.iTableTypeName(moName);
	}

}
