/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui.tlscript;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.translation.TranslationService;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredText;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredTextUtil;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.string.Internationalize;
import com.top_logic.tools.resources.translate.Translator;

/**
 * {@link Internationalize} which creates an {@link I18NStructuredText} from given sequence of
 * {@link StructuredText}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class I18NInternationalize extends Internationalize {

	/**
	 * Creates a {@link I18NInternationalize}.
	 */
	protected I18NInternationalize(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new I18NInternationalize(getName(), arguments);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Object inputArg = arguments[0];
		boolean translate = asBoolean(arguments[2]);
		if (inputArg instanceof StructuredText) {
			StructuredText structuredText = (StructuredText) inputArg;
			Object langArg = arguments[1];

			ResourcesModule resources = ResourcesModule.getInstance();
			String sourceCode = structuredText.getSourceCode();
			Locale lang = langArg == null ? resources.getDefaultLocale() : asLocale(langArg);
			I18NStructuredText result = new I18NStructuredText(Collections.singletonMap(lang, structuredText));
			if (translate && TranslationService.isActive()) {
				Translator translator = TranslationService.getInstance();

				for (Locale target : resources.getSupportedLocales()) {
					if (target.equals(lang)) {
						continue;
					}

					String translation = translator.translate(sourceCode, lang, target);
					result = I18NStructuredTextUtil.updateSourceCode(result, target, translation);
				}
			}
			return result;
		} else if (inputArg instanceof Map) {
			Map<?, ?> entries = (Map<?, ?>) inputArg;
			boolean onlyStructuredTextValues =
				entries.values().stream().filter(x -> !(x instanceof StructuredText)).findAny().isEmpty();
			if (onlyStructuredTextValues) {
				Map<Locale, StructuredText> textByLocale = entries.entrySet()
						.stream()
						.collect(Collectors.toMap(e -> asLocale(e.getKey()), e -> (StructuredText) e.getValue()));
				I18NStructuredText result = new I18NStructuredText(textByLocale);
				if (translate && TranslationService.isActive()) {
					// Fill missing translations.
					Translator translator = TranslationService.getInstance();
					List<String> supportedLanguages = translator.getSupportedLanguages();
					Locale sourceLang = null;
					StructuredText sourceText = null;
					for (String lang : supportedLanguages) {
						sourceLang = ResourcesModule.localeFromString(lang);
						sourceText = textByLocale.get(sourceLang);
						if (sourceText != null) {
							break;
						}
					}

					if (sourceText != null) {
						String sourceCode = sourceText.getSourceCode();
						ResourcesModule resources = ResourcesModule.getInstance();
						for (Locale target : resources.getSupportedLocales()) {
							if (textByLocale.containsKey(target)) {
								continue;
							}

							String translation = translator.translate(sourceCode, sourceLang, target);
							result = I18NStructuredTextUtil.updateSourceCode(result, target, translation);
						}
					}
				}
				return result;
			}
		}
		return super.eval(arguments, definitions);
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating an {@link I18NInternationalize} function.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<I18NInternationalize> {

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return Internationalize.Builder.DESCRIPTOR;
		}

		@Override
		public I18NInternationalize build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new I18NInternationalize(getConfig().getName(), args);
		}

	}

}

