/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tools.resources.translate.deepl;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.top_logic.tools.resources.translate.Translator;
import com.top_logic.tools.resources.translate.deepl.protocol.Glossaries;
import com.top_logic.tools.resources.translate.deepl.protocol.Glossary;
import com.top_logic.tools.resources.translate.deepl.protocol.LanguageResult;
import com.top_logic.tools.resources.translate.deepl.protocol.TranslationResult;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.json.JsonReader;

/**
 * Implementation of the DeepL API.
 * 
 * @see Builder#build()
 */
public class DeepLTranslator implements Translator {

	private static Logger LOG = LoggerFactory.getLogger(DeepLTranslator.class);

	/** API authentication key. */
	public static final String AUTH_KEY = "auth_key";

	/** The usage function allows to monitor how much you translate, as well as the limits set. */
	public static final String USAGE = "usage";

	/** The languages function allows to list all supported languages of the API. */
	public static final String LANGUAGES = "/languages";

	/** Native name of the language. */
	public static final String LANGUAGE_NATIVE_NAME = "name";

	/**
	 * The translate function allows to translate texts. Synchronous call supports the following
	 * basic parameters:
	 */
	public static final String TRANSLATE = "/translate";

	/**
	 * Language of the text to be translated (DE, EN etc.). If this parameter is omitted,
	 * {@link #TRANSLATE} will attempt to detect the language of the text and translate it.
	 */
	public static final String SOURCE_LANG = "source_lang";

	/** The language into which the text should be translated. */
	public static final String TARGET_LANG = "target_lang";

	/**
	 * Request parameter to specify the glossary to use for the translation. Important: This
	 * requires the source_lang parameter to be set and the language pair of the glossary has to
	 * match the language pair of the request.
	 */
	public static final String GLOSSARY_ID = "glossary_id";

	/**
	 * URL parameter to control splitting the input into sentences. This is enabled by default.
	 * Possible values are:
	 * 
	 * <ul>
	 * <li>"0" - no splitting at all, whole input is treated as one sentence</li>
	 * <li>"1" (default) - splits on interpunction and on newlines</li>
	 * <li>"nonewlines" - splits on interpunction only, ignoring newlines</li>
	 * </ul>
	 * 
	 * @see Builder#getSentenceSplitting()
	 */
	public static final String SPLIT_SENTENCES = "split_sentences";

	/**
	 * URL parameter to control XML processing. Omit to disable. The only currently possible value
	 * is "xml".
	 * 
	 * @see Builder#getTagHandling()
	 */
	public static final String TAG_HANDLING = "tag_handling";

	/** URL parameter transmitting {@link Builder#getNonSplittingTags()} */
	public static final String NON_SPLITTING_TAGS = "non_splitting_tags";

	/**
	 * URL parameter to disable automatic outline detection. Omit to enable. The only currently
	 * possible value is "0".
	 */
	public static final String OUTLINE_DETECTION = "outline_detection";

	/** URL parameter to transfer {@link Builder#getSplittingTags()}. */
	public static final String SPLITTING_TAGS = "splitting_tags";

	/** URL parameter transmitting {@link Builder#getIgnoreTags()}. */
	public static final String IGNORE_TAGS = "ignore_tags";

	private static final int MAX_REQUEST_SIZE = 30 * 1024;

	/**
	 * Builder for {@link DeepLTranslator}s
	 */
	public static class Builder {

		private static final String GLOSSARIES_PATH = "/glossaries";

		private String _apiHost;

		private String _apiKey;

		private List<String> _ignoreTags = Collections.emptyList();

		private List<String> _nonSplittingTags = Collections.emptyList();

		private boolean _outlineDetection;

		private String _splitSentences = "nonewlines";

		private List<String> _splittingTags = Collections.emptyList();

		private boolean _tagHandling;

		private Map<String, Map<String, String>> _glossaryNames = Collections.emptyMap();

		/**
		 * Gets the host of the translation API. Use the official {@link URL}, otherwise consider to
		 * be put on a blacklist.
		 */
		public String getApiHost() {
			return _apiHost;
		}

		/**
		 * @see #getApiHost()
		 */
		public void setApiHost(String apiHost) {
			_apiHost = apiHost;
		}

		/** Gets the API authentication key. Keep it secret, because translations are not free. */
		public String getApiKey() {
			return _apiKey;
		}

		/**
		 * @see #getApiKey()
		 */
		public void setApiKey(String apiKey) {
			_apiKey = apiKey;
		}

		/** Comma-separated list of XML tags that indicate text not to be translated. */
		public List<String> getIgnoreTags() {
			return _ignoreTags;
		}

		/** @see #getIgnoreTags() */
		public void setIgnoreTags(List<String> ignoreTags) {
			_ignoreTags = ignoreTags;
		}

		/** Comma-separated list of XML tags which never split sentences. */
		public List<String> getNonSplittingTags() {
			return _nonSplittingTags;
		}

		/** @see #getNonSplittingTags() */
		public void setNonSplittingTags(List<String> nonSplittingTags) {
			_nonSplittingTags = nonSplittingTags;
		}

		/** Whether automatic outline detection is enabled. */
		public boolean getOutlineDetection() {
			return _outlineDetection;
		}

		/** @see #getOutlineDetection() */
		public void setOutlineDetection(boolean outlineDetection) {
			_outlineDetection = outlineDetection;
		}

		/** Controls splitting of input into sentences. */
		public String getSentenceSplitting() {
			return _splitSentences;
		}

		/** @see #getSentenceSplitting() */
		public void setSplitSentences(String splitSentences) {
			_splitSentences = splitSentences;
		}

		/** Comma-separated list of XML tags which always cause splits. */
		public List<String> getSplittingTags() {
			return _splittingTags;
		}

		/** @see #getSplittingTags() */
		public void setSplittingTags(List<String> splittingTags) {
			_splittingTags = splittingTags;
		}

		/** Whether XML processing is enabled. */
		public boolean getTagHandling() {
			return _tagHandling;
		}

		/** @see #getTagHandling() */
		public void setTagHandling(boolean tagHandling) {
			_tagHandling = tagHandling;
		}

		private String createTranslateUrl() {
			StringBuilder url = new StringBuilder();
			url.append(getApiHost());
			url.append(TRANSLATE);
			boolean first = true;
			first = appendOptional(url, first, SPLIT_SENTENCES, _splitSentences);
			if (getTagHandling()) {
				first = appendOptional(url, first, TAG_HANDLING, "xml");
			}
			if (!getNonSplittingTags().isEmpty()) {
				first = appendOptional(url, first, NON_SPLITTING_TAGS, commaSeparated(getNonSplittingTags()));
			}
			if (!getOutlineDetection()) {
				first = appendOptional(url, first, OUTLINE_DETECTION, "0");
			}
			if (!getSplittingTags().isEmpty()) {
				first = appendOptional(url, first, SPLITTING_TAGS, commaSeparated(getSplittingTags()));
			}
			if (!getIgnoreTags().isEmpty()) {
				first = appendOptional(url, first, IGNORE_TAGS, commaSeparated(getIgnoreTags()));
			}
			return url.toString();
		}

		private static String commaSeparated(List<String> list) {
			return list.stream().collect(Collectors.joining(","));
		}

		/**
		 * If the given parameter is not empty, append it to the {@link URL} string.
		 * 
		 * @param first
		 *        Whether this is the first parameter.
		 * @return Whether the next parameter is still the first parameter.
		 */
		static boolean appendOptional(StringBuilder url, boolean first, String parameterName, String parameterValue) {
			if (!isEmpty(parameterValue)) {
				appendParamter(url, first, parameterName, parameterValue);
				return false;
			}
			return first;
		}

		static void appendParamter(StringBuilder url, boolean first, String parameterName, String parameterValue) {
			url.append(first ? '?' : "&");
			url.append(parameterName).append("=").append(parameterValue);
		}

		/**
		 * Creates a new {@link Translator} with the current configuration options.
		 */
		public DeepLTranslator build() {
			return new DeepLTranslator(
				_apiKey,
				url(LANGUAGES), createTranslateUrl(),
				url(GLOSSARIES_PATH), id -> url(GLOSSARIES_PATH + '/' + id), _glossaryNames);
		}

		private URL url(String path) {
			StringBuilder url = new StringBuilder();
			url.append(getApiHost());
			url.append(path);
			try {
				return new URL(url.toString());
			} catch (MalformedURLException ex) {
				throw new IllegalArgumentException("Invalid URL: " + url, ex);
			}
		}

		/**
		 * Source language mapped to target language mapped to the glossary name to use for that
		 * translation direction.
		 */
		public Map<String, Map<String, String>> getGlossaryNames() {
			return _glossaryNames;
		}

		/**
		 * @see #getGlossaryNames()
		 */
		public void setGlossaries(Map<String, Map<String, String>> glossaries) {
			_glossaryNames = glossaries;
		}
	}

	private final URL _languagesLookupUrl;

	private final String _translateUrl;

	/**
	 * Map from source language to target language to the glossary name to use for this combination.
	 */
	private final Map<String, Map<String, String>> _glossaryNameIndex;

	private Map<String, String> _glossaryIdIndex;

	private URL _glossariesLookupUrl;

	private Function<String, URL> _glossaryUrl;

	private final String _apiKey;

	/**
	 * Creates a {@link DeepLTranslator}.
	 * 
	 * @param glossaryNameIndex
	 *        Mapping of source language to target language to name of the glossary to use for this
	 *        translation direction.
	 */
	public DeepLTranslator(String apiKey, URL languagesLookupURL, String translateUrl, URL glossariesLookupUrl,
			Function<String, URL> glossaryDeleteUrl,
			Map<String, Map<String, String>> glossaryNameIndex) {
		_apiKey = apiKey;
		_languagesLookupUrl = languagesLookupURL;
		_translateUrl = translateUrl;
		_glossariesLookupUrl = glossariesLookupUrl;
		_glossaryUrl = glossaryDeleteUrl;
		_glossaryNameIndex = glossaryNameIndex;
	}

	/**
	 * The URL to use for translation.
	 */
	public String getTranslateUrl() {
		return _translateUrl;
	}

	/**
	 * The URl, where glossaries are hosted.
	 */
	public URL getGlossariesLookupUrl() {
		return _glossariesLookupUrl;
	}

	@Override
	public String translate(String text, String sourceLanguage, String targetLanguage) {
		String json;
		try {
			URL url = buildTranslateURL(sourceLanguage, targetLanguage);
			json = request(url, text);
			return getTranslationFromJSONString(json);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	/**
	 * Creates or updates a glossary.
	 *
	 * @param source
	 *        The source language of the glossary.
	 * @param target
	 *        The destination language of the glossary.
	 * @param name
	 *        The name of the glossary. If a glossary with the same name already exists, it is
	 *        deleted first.
	 * @param contents
	 *        The contents of the glossary in tab-separated values format.
	 * @return The ID of the created glossary.
	 */
	public String createGlossary(String source, String target, String name, String contents)
			throws ProtocolException, UnsupportedEncodingException, IOException {

		Glossaries glossaries = requestGlossaries();
		for (Glossary existing : glossaries.getGlossaries()) {
			if (name.equals(existing.getName())) {
				URL url = _glossaryUrl.apply(existing.getGlossaryId());
				LOG.info("Deleting old glossary entry " + toString(existing) + ".");
				request("DELETE", url, null);
			}
		}

		HttpURLConnection connection = openConnection("POST", _glossariesLookupUrl);
		connection.setDoOutput(true);
		OutputStream output = connection.getOutputStream();
		Writer writer = new StringWriter();
		
		writeField(writer, "name", encode(name));
		writer.append('&');
		writeField(writer, "source_lang", source);
		writer.append('&');
		writeField(writer, "target_lang", target);
		writer.append('&');
		writeField(writer, "entries_format", "tsv");
		writer.append('&');
		writeField(writer, "entries", encode(contents));

		OutputStreamWriter out = new OutputStreamWriter(output, StandardCharsets.UTF_8);
		out.write(writer.toString());
		out.flush();

		String result = retrieveResponse(connection);
		Glossary glossary = Glossary.readGlossary(json(result));

		LOG.info("Created glossary entry: " + toString(glossary));

		return glossary.getGlossaryId();
	}

	private URL buildTranslateURL(String sourceLanguage, String targetLanguage) throws IOException {
		StringBuilder url = new StringBuilder(_translateUrl);
		boolean first = _translateUrl.indexOf('?') < 0;
		Builder.appendOptional(url, first, SOURCE_LANG, sourceLanguage);
		Builder.appendParamter(url, first, TARGET_LANG, targetLanguage);
		String glossaryId = getGlossaryId(sourceLanguage, targetLanguage);
		Builder.appendOptional(url, first, GLOSSARY_ID, glossaryId);

		String urlSpec = url.toString();
		LOG.debug("Requesting translation: " + urlSpec);
		return new URL(urlSpec);
	}

	/**
	 * The ID of the glossary in use for the translation from source to target language.
	 */
	private String getGlossaryId(String sourceLanguage, String targetLanguage) throws IOException {
		String glossaryName =
			_glossaryNameIndex.getOrDefault(sourceLanguage, Collections.emptyMap()).get(targetLanguage);
		if (glossaryName == null) {
			return null;
		}
	
		if (_glossaryIdIndex == null) {
			Glossaries glossaries = requestGlossaries();
	
			Map<String, String> idIndex = new HashMap<>();
			for (var glossary : glossaries.getGlossaries()) {
				idIndex.put(glossary.getName(), glossary.getGlossaryId());
			}
	
			_glossaryIdIndex = idIndex;
		}
	
		return _glossaryIdIndex.get(glossaryName);
	}

	private String toString(Glossary glossary) {
		return glossary.getName() + "(" + glossary.getGlossaryId() + ") for " + glossary.getSourceLang() + " -> "
			+ glossary.getTargetLang();
	}

	private Glossaries requestGlossaries() throws IOException {
		String result = get(_glossariesLookupUrl);
		return Glossaries.readGlossaries(json(result));
	}

	private String getTranslationFromJSONString(String json) throws IOException {
		List<com.top_logic.tools.resources.translate.deepl.protocol.Translation> tuples = parseTranslationResult(json);
		StringBuilder translation = new StringBuilder();
		for (com.top_logic.tools.resources.translate.deepl.protocol.Translation tuple : tuples) {
			String translatedText = tuple.getText();
			translation.append(translatedText);
		}
		return translation.toString();
	}

	/**
	 * Parses a {@link TranslationResult} message.
	 */
	public static List<com.top_logic.tools.resources.translate.deepl.protocol.Translation> parseTranslationResult(
			String json) throws IOException {
		TranslationResult translationResult =
			TranslationResult.readTranslationResult(json(json));
		return translationResult.getTranslations();
	}

	private static JsonReader json(String json) {
		return new JsonReader(new StringR(json));
	}

	/**
	 * Retrieves the response of a GET request to the given URL.
	 */
	private String get(URL url) throws IOException {
		return request("GET", url, null);
	}

	/**
	 * Makes a request via given {@link URL} string and returns the response as a JSON string.
	 * 
	 * @param text
	 *        The text that has to be translated. Can contain HTML tags. If <code>null</code> no
	 *        text will be translated.
	 */
	private String request(URL url, String text) throws IOException {
		return request("POST", url, text);
	}

	private String request(String method, URL url, String text)
			throws IOException, ProtocolException, UnsupportedEncodingException {
		HttpURLConnection connection = openConnection(method, url);
		if (text != null && !text.isEmpty()) {
			checkRequestSize(text);

			connection.setDoOutput(true);
			String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
			OutputStream output = connection.getOutputStream();
			output.write("text=".getBytes(StandardCharsets.UTF_8));
			output.write(encodedText.getBytes(StandardCharsets.UTF_8));
		}
		return retrieveResponse(connection);
	}

	private String encode(String name) {
		return URLEncoder.encode(name, StandardCharsets.UTF_8);
	}

	private void writeField(Writer writer, String property, String value) throws IOException {
		writer.write(property);
		writer.write('=');
		writer.write(value);
	}

	private HttpURLConnection openConnection(String method, URL url) throws IOException, ProtocolException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(method);
		if (_apiKey != null) {
			connection.setRequestProperty("Authorization", "DeepL-Auth-Key " + _apiKey);
		}
		connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
		return connection;
	}

	private String retrieveResponse(HttpURLConnection connection) throws IOException {
		int responseCode = connection.getResponseCode();
		if (responseCode >= 200 && responseCode < 300) {
			return readResponse(connection);
		} else {
			throw handleError(responseCode);
		}
	}

	private RuntimeException handleError(int responseCode) {
		String message = getErrorMessage(responseCode);
		throw new RuntimeException("HTTP/" + responseCode + ": " + message);
	}

	private String readResponse(HttpURLConnection connection) throws IOException {
		StringBuilder response = new StringBuilder();
		try (BufferedReader reader =
			new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
			reader.close();
		}
		connection.disconnect();
		return response.toString();
	}

	/**
	 * Checks the given request size.
	 */
	private static void checkRequestSize(String urlString) {
		int requestSize = urlString.length();
		if (requestSize == 0) {
			throw new IllegalArgumentException("Empty request.");
		}
		if (requestSize > MAX_REQUEST_SIZE) {
			throw new IllegalArgumentException("Request exceeds maximum size: " + requestSize);
		}
	}

	private static String getErrorMessage(int code) {
		switch (code) {
			case 400:
				return "Bad request";
			case 403:
				return "Authorization failed";
			case 404:
				return "The requested resource could not be found";
			case 413:
				return "The request size exceeds the limit";
			case 429:
				return "Too many requests";
			case 456:
				return "Quota exceeded";
			case 503:
				return "Resource currently unavailable";
		}
		if (code >= 500) {
			return "Internal error";
		}
		return "";
	}

	@Override
	public List<String> getSupportedLanguages() {
		List<String> languages = new ArrayList<>();

		try {
			String result = request(_languagesLookupUrl, null);
			JsonReader json = json(result);
			json.beginArray();
			while (json.hasNext()) {
				LanguageResult languageResult = LanguageResult.readLanguageResult(json);
				String language = languageResult.getLanguage();
				languages.add(normalizeLanguage(language));
			}
			json.endArray();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		return languages;
	}

	/**
	 * The supported language should correspond to the language of a locale, since the question of
	 * supporting a language is really a question of supporting a locale language.
	 * 
	 * @see Locale#getLanguage()
	 */
	private static String normalizeLanguage(String language) {
		return new Locale(language).getLanguage();
	}

	/**
	 * Check if a String is empty, i.e. null or <code>""</code>.
	 *
	 * @return true, if the String is null or <code>""</code>.
	 */
	public static final boolean isEmpty(CharSequence aString) {
		return ((aString == null) || aString.length() == 0);
	}
}
