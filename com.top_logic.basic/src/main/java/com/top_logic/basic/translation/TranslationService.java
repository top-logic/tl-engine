/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.translation;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.format.MemorySizeFormat;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.tools.resources.translate.Translator;

/**
 * A service for translation.
 * 
 * <p>
 * <b>Cautions:</b>
 * <ul>
 * <li>Every translation causes costs proportional to the number of translated characters and the
 * total number is limited too.</li>
 * <li>This service does no caching for translations, so for each call of {@link #translate
 * translate}, it establishes an {@link HttpsURLConnection} to access the translation (REST)
 * API.</li>
 * <li>Be advised to read the documentation for {@link Config} and be careful with usage. For
 * example, if API failed to translate, postpone the failed translation request instead of resending
 * it again and again.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Functionalities:</b>
 * <ul>
 * <li>In order to translate a (XML-structured) text, use:
 * <ul>
 * <li>{@link #translate(String, String) translate(String, Locale)}</li>
 * <li>{@link #translate(String, String, String) translate(String, Locale, Locale)}</li>
 * </ul>
 * </li>
 * 
 * <li>In order to determine, whether a language is supported, use {@link #isSupported(Locale)
 * isSupported(Locale)}.</li>
 * </ul>
 * </p>
 * 
 * @author <a href="mailto:Dmitry.Ivanizki@top-logic.com">Dmitry Ivanizki</a>
 */
public abstract class TranslationService<C extends TranslationService.Config> extends ConfiguredManagedClass<C>
		implements Translator {

	/**
	 * Configuration for {@link TranslationService}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<TranslationService<?>> {

		/** @see #getApiHost() */
		String API_HOST = "api-host";

		/** The API key to authenticate the request. */
		String API_KEY = "api-key";

		/** @see #getMaxAccumulatedTranslationSize() */
		String MAX_ACCUMULATED_TRANSLATION_SIZE = "max-accumulated-translation-size";

		/** A value for {@link #getMaxAccumulatedTranslationSize()}. */
		long UNLIMITED_ACCUMULATED_TRANSLATION_SIZE = 0;

		/**
		 * URL prefix of the translation API.
		 */
		@Name(API_HOST)
		@Mandatory
		String getApiHost();

		/** @see #getApiHost() */
		void setApiHost(String apiHost);

		/** The API key to authenticate the requests. */
		@Name(API_KEY)
		@Mandatory
		@Encrypted
		String getApiKey();

		/** @see #getApiKey() */
		void setApiKey(String apiKey);

		/**
		 * The maximum accumulated size of translations.
		 * 
		 * <p>
		 * Means of limiting the costs of translations. The size is accumulated while the
		 * application is running, and reset to zero when it is stopped or restarted.
		 * </p>
		 * 
		 * <p>
		 * Don't use this property to disable the service.
		 * </p>
		 * 
		 * <p>
		 * Use the value {@value #UNLIMITED_ACCUMULATED_TRANSLATION_SIZE} to make the maximal size
		 * infinite.
		 * </p>
		 */
		@Name(MAX_ACCUMULATED_TRANSLATION_SIZE)
		@LongDefault(UNLIMITED_ACCUMULATED_TRANSLATION_SIZE)
		@Format(MemorySizeFormat.class)
		long getMaxAccumulatedTranslationSize();

		/** @see #getMaxAccumulatedTranslationSize */
		void setMaxAccumulatedTranslationSize(long maxAccumulatedTranslationSize);
	}

	/**
	 * Summary size of translations. Must be constrained by
	 * {@link Config#MAX_ACCUMULATED_TRANSLATION_SIZE}.
	 */
	private int _summarySize = 0;

	/** List of supported languages as provided by the translation API. */
	private List<String> _supportedLanguages;

	private Translator _translator;

	/**
	 * Creates a new {@link TranslationService}.
	 */
	public TranslationService(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	protected void startUp() {
		super.startUp();
		_translator = createTranslator();
		try {
			_supportedLanguages = Collections.unmodifiableList(_translator.getSupportedLanguages());
		} catch (Exception ex) {
			Logger.error("Failed to retrieve supported languages.", ex, TranslationService.class);
			_supportedLanguages = Collections.emptyList();
			_translator = Translator.NONE;
		}
	}

	/**
	 * Creates the {@link Translator} implementation.
	 */
	protected abstract Translator createTranslator();

	/**
	 * Translates the given text from the source {@link Locale} to the target {@link Locale}.
	 * 
	 * @param sourceLanguage
	 *        The language of the given text, <code>null</code> to use auto-detect.
	 */
	@Override
	public String translate(String text, String sourceLanguage, String targetLanguage) {
		if (StringServices.isEmpty(text)) {
			return StringServices.EMPTY_STRING;
		}
		handleTranslationSize(text);
		try {
			return _translator.translate(text, sourceLanguage, targetLanguage);
		} catch (Exception ex) {
			throw new I18NRuntimeException(I18NConstants.HTTP_REQUEST_ERROR__REASON.fill(ex.getMessage()), ex);
		}
	}

	/**
	 * Checks and accumulates the size of the text to translate.
	 */
	protected void handleTranslationSize(String text) {
		int textSize = text.length();
		long maxAccumulatedSize = getConfig().getMaxAccumulatedTranslationSize();
		if (maxAccumulatedSize != Config.UNLIMITED_ACCUMULATED_TRANSLATION_SIZE
			&& _summarySize + textSize > maxAccumulatedSize) {
			throw new I18NRuntimeException(I18NConstants.REQUEST_EXCEEDS_MAX_ACCUMULATED_SIZE);
		}
		_summarySize += textSize;
	}

	/**
	 * UTF-8 decoding.
	 */
	protected static String decode(String string) {
		try {
			return URLDecoder.decode(string, StringServices.UTF8);
		} catch (UnsupportedEncodingException ex) {
			throw new UnsupportedOperationException("Encoding not supported: " + StringServices.UTF8, ex);
		}
	}

	/**
	 * Retrieves the list of supported languages as provided by the translation API.
	 */
	@Override
	public List<String> getSupportedLanguages() {
		return _supportedLanguages;
	}

	/**
	 * Resets the {@link #_summarySize} to zero.
	 */
	public void resetSummarySize() {
		_summarySize = 0;
	}

	/**
	 * Whether the {@link TranslationService} is activated.
	 * 
	 * @see #getInstance()
	 */
	public static boolean isActive() {
		return Module.INSTANCE.isActive();
	}

	/**
	 * Provides the singleton instance of {@link TranslationService}.
	 * 
	 * <p>
	 * In case the {@link TranslationService} is {@link #isActive() not active}, a dummy instance is
	 * returned that does not support any language.
	 * </p>
	 */
	public static Translator getInstance() {
		if (!isActive()) {
			return Translator.NONE;
		}
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Module for instantiation of the {@link TranslationService}.
	 */
	@SuppressWarnings("rawtypes")
	public static class Module extends TypedRuntimeModule<TranslationService> {

		/** Singleton for this module. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<TranslationService> getImplementation() {
			return TranslationService.class;
		}

	}
}
