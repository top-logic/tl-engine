/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.handlers;

import java.util.Locale;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey.Builder;
import com.top_logic.basic.util.ResKey.LiteralKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.xio.importer.binding.ImportContext;

/**
 * {@link Handler} adding a translation of a certain language to a model property of type
 * {@link ResKey}.
 * 
 * <p>
 * The language is given either as fixed {@link Config#getLanguage() language}, by an
 * {@link Config#getLanguageAttribute() XML attribute} of the current element, or by an
 * {@link Config#getLanguageVar() import variable}. Exactly one of these options must be set. Only
 * the language part of a given language tag is used, e.g. a translation for <code>de-DE</code> is
 * stored for <code>de</code>. If the language is read from the input and the input contains none,
 * the {@link Config#getDefaultLanguage() default language} is used.
 * </p>
 * 
 * <p>
 * An empty value (an empty or blank XML attribute or element text) is not stored.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ResKeyUpdate<C extends ResKeyUpdate.Config<?>> extends StoreHandler<C> {

	/**
	 * Configuration options for {@link ResKeyUpdate}.
	 */
	@TagName("set-translation")
	public interface Config<I extends ResKeyUpdate<?>> extends StoreHandler.Config<I> {

		/**
		 * @see #getLanguage()
		 */
		String LANGUAGE = "language";

		/**
		 * @see #getLanguageAttribute()
		 */
		String LANGUAGE_ATTRIBUTE = "language-attribute";

		/**
		 * @see #getLanguageVar()
		 */
		String LANGUAGE_VAR = "language-var";

		/**
		 * @see #getDefaultLanguage()
		 */
		String DEFAULT_LANGUAGE = "default-language";

		/**
		 * @see #getFillFallback()
		 */
		String FILL_FALLBACK = "fill-fallback";

		/**
		 * The XML attribute to read.
		 * 
		 * <p>
		 * If not given, the text content of the current element is read instead.
		 * </p>
		 */
		@Nullable
		String getXmlAttribute();

		/**
		 * The language tag of the language to assign.
		 * 
		 * <p>
		 * Exactly one of this option, {@link #getLanguageAttribute()} and {@link #getLanguageVar()}
		 * must be given.
		 * </p>
		 */
		@Name(LANGUAGE)
		@Nullable
		String getLanguage();

		/**
		 * The XML attribute of the current element containing the language tag of the language to
		 * assign.
		 * 
		 * <p>
		 * Exactly one of this option, {@link #getLanguage()} and {@link #getLanguageVar()} must be
		 * given.
		 * </p>
		 */
		@Name(LANGUAGE_ATTRIBUTE)
		@Nullable
		String getLanguageAttribute();

		/**
		 * The {@link ImportContext#getVar(String) variable} containing the language to assign.
		 * 
		 * <p>
		 * The variable contains either a language tag or a {@link Locale}. Exactly one of this
		 * option, {@link #getLanguage()} and {@link #getLanguageAttribute()} must be given.
		 * </p>
		 * 
		 * @see WithVar
		 */
		@Name(LANGUAGE_VAR)
		@Label("Language variable")
		@Nullable
		String getLanguageVar();

		/**
		 * The language tag of the language to assign, if the {@link #getLanguageAttribute()
		 * language attribute} or the {@link #getLanguageVar() language variable} contains no
		 * language.
		 * 
		 * <p>
		 * If not given, a value without language is reported as error and not stored. Must not be
		 * given together with a fixed {@link #getLanguage() language}.
		 * </p>
		 */
		@Name(DEFAULT_LANGUAGE)
		@Nullable
		String getDefaultLanguage();

		/**
		 * Whether the value is also stored for the fallback language of the application, if the
		 * property has no translation for it yet.
		 */
		@Name(FILL_FALLBACK)
		boolean getFillFallback();

	}

	private final String _xmlAttribute;

	private final Locale _language;

	private final Locale _defaultLanguage;

	/**
	 * Creates a {@link ResKeyUpdate} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ResKeyUpdate(InstantiationContext context, C config) {
		super(context, config);

		_xmlAttribute = config.getXmlAttribute();

		int languageOptions = (config.getLanguage() != null ? 1 : 0)
			+ (config.getLanguageAttribute() != null ? 1 : 0)
			+ (config.getLanguageVar() != null ? 1 : 0);
		if (languageOptions != 1) {
			context.error("Exactly one of '" + Config.LANGUAGE + "', '" + Config.LANGUAGE_ATTRIBUTE + "', and '"
				+ Config.LANGUAGE_VAR + "' must be given at " + config.location() + ".");
		}

		_language = configuredLanguage(context, config, config.getLanguage());
		_defaultLanguage = configuredLanguage(context, config, config.getDefaultLanguage());
		if (_language != null && _defaultLanguage != null) {
			context.error("'" + Config.DEFAULT_LANGUAGE + "' must not be given together with '" + Config.LANGUAGE
				+ "' at " + config.location() + ".");
		}
	}

	private static Locale configuredLanguage(InstantiationContext context, Config<?> config, String languageTag) {
		if (languageTag == null) {
			return null;
		}
		Locale result = toLanguage(languageTag);
		if (result == null) {
			context.error("Invalid language '" + languageTag + "' at " + config.location() + ".");
		}
		return result;
	}

	@Override
	public Object importXml(ImportContext context, XMLStreamReader in) throws XMLStreamException {
		// Note: The language attribute must be read before the element text is consumed.
		Object languageValue = languageValue(context, in);
		String rawValue = _xmlAttribute == null ? in.getElementText() : in.getAttributeValue(null, _xmlAttribute);
		if (rawValue == null || rawValue.isBlank()) {
			return null;
		}

		if (isEmpty(languageValue)) {
			languageValue = _defaultLanguage;
		}
		Locale language = toLanguage(languageValue);
		if (language == null) {
			if (isEmpty(languageValue)) {
				context.error(in.getLocation(), I18NConstants.ERROR_NO_LANGUAGE__HANDLER.fill(location()));
			} else {
				context.error(in.getLocation(),
					I18NConstants.ERROR_INVALID_LANGUAGE__HANDLER_VALUE.fill(location(), languageValue));
			}
			return null;
		}

		context.getProperty(this, var(context), name(), oldValue -> {
			ResKey oldResKey = (ResKey) oldValue;
			Builder builder = ResKey.builder(oldResKey);
			if (oldResKey instanceof LiteralKey literal) {
				builder.addAll(literal);
			}
			builder.add(language, rawValue);
			if (getConfig().getFillFallback()) {
				Locale fallback = fallbackLocale();
				if (fallback != null && !builder.has(fallback)) {
					builder.add(fallback, rawValue);
				}
			}
			context.setProperty(this, var(context), name(), builder.build());
		});
		return null;
	}

	private Object languageValue(ImportContext context, XMLStreamReader in) {
		if (_language != null) {
			return _language;
		}
		String languageAttribute = getConfig().getLanguageAttribute();
		if (languageAttribute != null) {
			return in.getAttributeValue(null, languageAttribute);
		}
		String languageVar = getConfig().getLanguageVar();
		if (languageVar != null) {
			return context.getVarOrNull(languageVar);
		}
		return null;
	}

	private static boolean isEmpty(Object languageValue) {
		return languageValue == null || languageValue.toString().isBlank();
	}

	/**
	 * Reduces the given language tag or {@link Locale} to its language.
	 * 
	 * @return The {@link Locale} of the language, or <code>null</code>, if the given value does
	 *         not specify a language.
	 */
	private static Locale toLanguage(Object languageValue) {
		if (isEmpty(languageValue)) {
			return null;
		}
		Locale locale = languageValue instanceof Locale given ? given
			: Locale.forLanguageTag(languageValue.toString().trim());
		String language = locale.getLanguage();
		if (language.isEmpty()) {
			return null;
		}
		return new Locale(language);
	}

	private static Locale fallbackLocale() {
		if (!ResourcesModule.Module.INSTANCE.isActive()) {
			return null;
		}
		return ResourcesModule.getInstance().getFallbackLocale();
	}

	private String name() {
		return getConfig().getName();
	}

}
