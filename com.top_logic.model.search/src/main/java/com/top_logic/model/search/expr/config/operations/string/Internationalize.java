/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.string;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.translation.TranslationService;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.Literal;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.tools.resources.translate.Translator;

/**
 * {@link GenericMethod} creating an internationalized text from an input text in a source language.
 * 
 * <p>
 * To create internationalizations in other languages, the {@link TranslationService} must be
 * active.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Internationalize extends GenericMethod {

	/**
	 * Creates a {@link Internationalize}.
	 */
	protected Internationalize(String name, SearchExpression self, SearchExpression[] arguments) {
		super(name, self, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new Internationalize(getName(), self, arguments);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Object inputArg = arguments[0];
		if (inputArg == null) {
			return null;
		}

		boolean translate = asBoolean(arguments[2]);
		if (inputArg instanceof Map) {
			ResKey.Builder builder = ResKey.builder();
			for (Entry<?, ?> entry : ((Map<?, ?>) inputArg).entrySet()) {
				Locale lang = asLocale(entry.getKey());
				String text = asString(entry.getValue());
				builder.add(lang, text);
			}

			if (translate && TranslationService.isActive()) {
				// Fill missing translations.
				Translator translator = TranslationService.getInstance();

				List<String> supportedLanguages = translator.getSupportedLanguages();
				Locale sourceLang = null;
				String sourceText = null;
				for (String lang : supportedLanguages) {
					sourceLang = ResourcesModule.localeFromString(lang);
					sourceText = builder.get(sourceLang);
					if (sourceText != null) {
						break;
					}
				}

				if (sourceText != null) {
					ResourcesModule resources = ResourcesModule.getInstance();
					for (Locale target : resources.getSupportedLocales()) {
						if (builder.has(target)) {
							continue;
						}

						String translation = translator.translate(sourceText, sourceLang, target);
						builder.add(target, translation);
					}
				}
			}
			return builder.build();
		} else {
			String text = asString(inputArg);
			Object langArg = arguments[1];

			ResourcesModule resources = ResourcesModule.getInstance();
			Locale lang = langArg == null ? resources.getDefaultLocale() : asLocale(langArg);

			ResKey.Builder builder = ResKey.builder();
			builder.add(lang, text);
			if (translate && TranslationService.isActive()) {
				Translator translator = TranslationService.getInstance();

				for (Locale target : resources.getSupportedLocales()) {
					if (target.equals(lang)) {
						continue;
					}

					String translation = translator.translate(text, lang, target);
					builder.add(target, translation);
				}
			}
			return builder.build();
		}
	}

	@Override
	public boolean isSideEffectFree() {
		SearchExpression translateArg = getArguments()[1];
		return translateArg instanceof Literal && !isTrue(((Literal) translateArg).getValue());
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating an {@link Internationalize} function.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<Internationalize> {

		/** Description of parameters for an {@link Internationalize}. */
		public static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("input")
			.optional("sourceLang")
			.optional("translate", false)
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
		public Internationalize build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			return new Internationalize(getConfig().getName(), self, args);
		}

	}

}
