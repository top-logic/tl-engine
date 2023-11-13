/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui.tlscript;

import java.util.Collections;
import java.util.Locale;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.translation.TranslationService;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.string.Translate;
import com.top_logic.tools.resources.translate.Translator;
import com.top_logic.util.TLContext;

/**
 * {@link Translate} that translates a {@link StructuredText} using the {@link TranslationService}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class I18NTranslate extends Translate {

	/**
	 * Creates a {@link I18NTranslate} expression.
	 */
	protected I18NTranslate(String name, SearchExpression self, SearchExpression[] arguments) {
		super(name, self, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new I18NTranslate(getName(), self, arguments);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		if (arguments[0] instanceof StructuredText) {
			StructuredText text = (StructuredText) arguments[0];

			Object sourceArg = arguments[2];
			if (!TranslationService.isActive()) {
				return text;
			}

			Locale sourceLang = sourceArg == null ? TLContext.getLocale()
				: (AUTO_DETECT.equals(sourceArg) ? null : asLocale(sourceArg));
			Locale targetLang = asLocale(arguments[1]);

			if (targetLang.equals(sourceLang)) {
				return text;
			}

			Translator translator = TranslationService.getInstance();

			String translation = translator.translate(text.getSourceCode(), sourceLang, targetLang);
			return new StructuredText(translation, Collections.unmodifiableMap(text.getImages()));
		}
		return super.eval(arguments, definitions);
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating an {@link Translate} function.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<I18NTranslate> {

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return Translate.Builder.DESCRIPTOR;
		}

		@Override
		public I18NTranslate build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			return new I18NTranslate(getConfig().getName(), self, args);
		}

	}
}

