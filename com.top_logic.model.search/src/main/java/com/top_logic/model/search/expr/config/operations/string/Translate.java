/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.string;

import java.util.List;
import java.util.Locale;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.translation.TranslationService;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.tools.resources.translate.Translator;
import com.top_logic.util.TLContext;

/**
 * {@link GenericMethod} translating a string using the {@link TranslationService}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Translate extends GenericMethod {

	/**
	 * Special value for the source language to request auto-detection by the
	 * {@link TranslationService}.
	 */
	protected static final String AUTO_DETECT = "auto";

	/**
	 * Creates a {@link Translate} expression.
	 */
	protected Translate(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new Translate(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Object inputArg = arguments[0];
		if (inputArg == null) {
			return null;
		}

		String text = asString(inputArg);
		if (text.isBlank()) {
			return text;
		}

		Object sourceArg = arguments[2];

		if (!TranslationService.isActive()) {
			return text;
		}

		Locale sourceLang =
			sourceArg == null ? TLContext.getLocale() : (AUTO_DETECT.equals(sourceArg) ? null : asLocale(sourceArg));
		Locale targetLang = asLocale(arguments[1]);

		if (targetLang.equals(sourceLang)) {
			return text;
		}

		Translator translator = TranslationService.getInstance();

		return translator.translate(text, sourceLang, targetLang);
	}

	@Override
	public boolean isSideEffectFree() {
		return false;
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating an {@link Translate} function.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<Translate> {

		/** Description of parameters for a {@link Translate}. */
		public static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("input")
			.mandatory("targetLang")
			.optional("sourceLang")
			.build();

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

		@Override
		public Translate build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new Translate(getConfig().getName(), args);
		}

	}

}
