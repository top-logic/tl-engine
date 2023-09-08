/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit;

import java.io.StringReader;
import java.util.Locale;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.template.ExprFactory;
import com.top_logic.basic.config.template.TemplateExpression;
import com.top_logic.basic.config.template.TemplateScope;
import com.top_logic.basic.config.template.TextRange;
import com.top_logic.basic.config.template.parser.ConfigTemplateParser;
import com.top_logic.basic.config.template.parser.ParseException;
import com.top_logic.basic.util.I18NBundle;
import com.top_logic.basic.util.ResKey;
import com.top_logic.util.Resources;

/**
 * {@link TemplateScope} that takes template sources from application resources.
 * 
 * @see Resources
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ResourceScope implements TemplateScope {

	private final Locale _locale;

	/**
	 * Creates a {@link ResourceScope} for the current user locale.
	 * 
	 * <p>
	 * Note: The resulting instance must not be statically cached, nor used from multiple sessions.
	 * </p>
	 */
	public ResourceScope() {
		this(null);
	}

	/**
	 * Creates a {@link ResourceScope} for the given resource bundle.
	 * 
	 * @param locale
	 *        The locale to use for lookup.
	 */
	public ResourceScope(Locale locale) {
		_locale = locale;
	}

	@Override
	public TemplateExpression getTemplate(String name, boolean optional) {
		String template;
		ResKey key = ResKey.legacy(name);
		if (key == null) {
			return ExprFactory.literalTextDirect(TextRange.UNDEFINED, "[null]");
		}
		if (optional) {
			template = resources().getString(key, null);
			if (template == null) {
				return null;
			}
		} else {
			template = resources().getString(key);
		}
		TemplateExpression templateExpr;
		try {
			templateExpr = new ConfigTemplateParser(new StringReader(template)).template();
		} catch (ParseException ex) {
			Logger.error("Failure parsing rendering template '" + name + "': " + template, ex, ResourceScope.class);
			return ExprFactory.literalTextDirect(TextRange.UNDEFINED,
				"[Failure resolving template '" + name + "': " + message(ex) + "]");
		}
		return templateExpr;
	}

	private I18NBundle resources() {
		return _locale == null ? Resources.getInstance() : Resources.getInstance(_locale);
	}

	private String message(ParseException ex) {
		String message = ex.getMessage();
		if (message == null) {
			return ex.getClass().getName();
		}
		return message;
	}
}