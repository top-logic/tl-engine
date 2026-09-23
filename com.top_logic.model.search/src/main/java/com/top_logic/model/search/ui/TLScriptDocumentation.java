/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.Optional;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLObject;
import com.top_logic.model.resources.TLPartResourceProvider;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.documentation.DocumentationConstants;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * HTML documentation of the names a TL-Script refers to: script functions and model elements.
 *
 * <p>
 * Script editors show this documentation next to a code completion and as a tooltip for a token
 * under the mouse. Every result is an HTML fragment enclosed in an element with the CSS class
 * {@link DocumentationConstants#DOCUMENTATION_CSS_CLASS}.
 * </p>
 */
public final class TLScriptDocumentation implements TLScriptConstants {

	private static final String VARIABLE_PREFIX = "$";

	private TLScriptDocumentation() {
		// Utility class.
	}

	/**
	 * The documentation of the script function with the given name.
	 *
	 * @param context
	 *        The context providing the locale of the documentation; <code>null</code> when there is
	 *        none, in which case no documentation is available.
	 * @param functionName
	 *        The name of the script function.
	 * @return The HTML documentation, or {@link Optional#empty()} when the context is missing or the
	 *         function has no documentation.
	 */
	public static Optional<String> functionDocumentation(DisplayContext context, String functionName) {
		if (context == null) {
			return Optional.empty();
		}
		return SearchBuilder.getInstance().getDocumentation(context, functionName);
	}

	/**
	 * The documentation of the given model element: its kind, label, qualified name and description.
	 *
	 * @param part
	 *        The model element to describe.
	 * @return The HTML documentation, or {@link Optional#empty()} when there is nothing to show for
	 *         the given element.
	 */
	public static Optional<String> modelPartDocumentation(TLModelPart part) {
		String tooltip = TLPartResourceProvider.INSTANCE.getTooltip(part);
		if (tooltip == null || tooltip.isBlank()) {
			return Optional.empty();
		}

		StringWriter buffer = new StringWriter();
		try (TagWriter out = new TagWriter(buffer)) {
			out.beginTag(DIV, CLASS_ATTR, DocumentationConstants.DOCUMENTATION_CSS_CLASS);
			// The tooltip is an HTML fragment with all dynamic values already encoded.
			out.writeContent(tooltip);
			out.endTag(DIV);
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
		return Optional.of(buffer.toString());
	}

	/**
	 * The documentation of a token of a TL-Script source.
	 *
	 * <p>
	 * A model reference (a qualified name enclosed in {@link #MODEL_SCOPE_SEPARATOR}, or a name
	 * containing {@link #MODEL_TYPE_SEPARATOR} or {@link #MODEL_ATTRIBUTE_SEPARATOR}) is resolved in
	 * the application model and described by {@link #modelPartDocumentation(TLModelPart)}. A plain
	 * identifier is taken as the name of a script function and described by
	 * {@link #functionDocumentation(DisplayContext, String)}. A variable reference has no
	 * documentation.
	 * </p>
	 *
	 * @param context
	 *        The context providing the locale of function documentation, <code>null</code> when
	 *        there is none.
	 * @param token
	 *        The token to describe.
	 * @return The HTML documentation, or {@link Optional#empty()} when the token has none, in
	 *         particular when it does not resolve to a function or model element.
	 */
	public static Optional<String> documentation(DisplayContext context, String token) {
		if (token == null) {
			return Optional.empty();
		}
		String name = token.strip();
		if (name.isEmpty() || name.startsWith(VARIABLE_PREFIX)) {
			return Optional.empty();
		}

		boolean quoted = name.startsWith(MODEL_SCOPE_SEPARATOR);
		if (quoted || name.contains(MODEL_TYPE_SEPARATOR) || name.contains(MODEL_ATTRIBUTE_SEPARATOR)) {
			return resolveModelPart(stripModelScope(name)).flatMap(TLScriptDocumentation::modelPartDocumentation);
		}
		return functionDocumentation(context, name);
	}

	private static String stripModelScope(String name) {
		String result = name;
		if (result.startsWith(MODEL_SCOPE_SEPARATOR)) {
			result = result.substring(MODEL_SCOPE_SEPARATOR.length());
		}
		if (result.endsWith(MODEL_SCOPE_SEPARATOR)) {
			result = result.substring(0, result.length() - MODEL_SCOPE_SEPARATOR.length());
		}
		return result.strip();
	}

	private static Optional<TLModelPart> resolveModelPart(String qualifiedName) {
		if (qualifiedName.isEmpty()) {
			return Optional.empty();
		}
		TLObject resolved;
		try {
			resolved = TLModelUtil.resolveQualifiedName(qualifiedName);
		} catch (TopLogicException ex) {
			// A name that does not (yet) denote a model element, e.g. while it is being typed.
			return Optional.empty();
		}
		if (resolved instanceof TLModelPart part) {
			return Optional.of(part);
		}
		return Optional.empty();
	}

}
