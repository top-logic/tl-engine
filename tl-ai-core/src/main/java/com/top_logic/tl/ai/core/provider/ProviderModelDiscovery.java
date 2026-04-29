/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.top_logic.basic.Logger;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.json.JsonReader;

/**
 * Discovers model IDs from configured LLM provider endpoints with a short-lived cache.
 */
final class ProviderModelDiscovery {

	private static final long CACHE_TTL_MILLIS = Duration.ofMinutes(5).toMillis();

	private static final ConcurrentMap<String, CachedModelList> CACHE = new ConcurrentHashMap<>();

	private ProviderModelDiscovery() {
		// Utility class.
	}

	static void clearCache() {
		CACHE.clear();
	}

	static DiscoveryResult discoverModels(String providerName, LLMProviderRegistry.ProviderConfig config) {
		return discoverModels(providerName, config.getApiKey(), config.getBaseUrl(), config.getVersion());
	}

	static DiscoveryResult discoverModels(String providerName, String apiKey, String baseUrl, String version) {
		String cacheKey = providerName + "|" + (baseUrl != null ? baseUrl : "") + "|" + (version != null ? version : "")
			+ "|" + (apiKey != null ? Integer.toHexString(apiKey.hashCode()) : "");
		long now = System.currentTimeMillis();
		CachedModelList cached = CACHE.get(cacheKey);
		if (cached != null && (now - cached._loadedAtMillis) < CACHE_TTL_MILLIS) {
			return new DiscoveryResult(cached._models, true);
		}

		String name = providerName.toLowerCase(Locale.ROOT);
		if (!isAuthOptionalProvider(name) && !LLMProviderRegistry.isValidApiKey(apiKey)) {
			return new DiscoveryResult(List.of(), false);
		}

		String endpoint = resolveBaseUrl(providerName, baseUrl) + "/v1/models";
		HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
			.uri(URI.create(endpoint))
			.GET()
			.timeout(Duration.ofSeconds(30));

		if (name.contains("anthropic")) {
			requestBuilder.header("x-api-key", apiKey);
			requestBuilder.header("anthropic-version", version != null ? version : "2023-06-01");
		} else if (LLMProviderRegistry.isValidApiKey(apiKey)) {
			requestBuilder.header("Authorization", "Bearer " + apiKey);
		}

		try {
			HttpClient client = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(10))
				.build();
			HttpResponse<String> response = client.send(
				requestBuilder.build(),
				HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

			if (response.statusCode() != 200) {
				Logger.warn("Model discovery failed for provider '" + providerName + "': HTTP " + response.statusCode(),
					ProviderModelDiscovery.class);
				return new DiscoveryResult(List.of(), false);
			}

			List<String> discoveredModels = parseModelIds(response.body());
			CACHE.put(cacheKey, new CachedModelList(discoveredModels, now));
			return new DiscoveryResult(discoveredModels, false);
		} catch (Exception ex) {
			Logger.warn("Model discovery failed for provider '" + providerName + "': " + ex.getMessage(),
				ProviderModelDiscovery.class);
			return new DiscoveryResult(List.of(), false);
		}
	}

	static final class DiscoveryResult {
		private final List<String> _models;
		private final boolean _fromCache;

		DiscoveryResult(List<String> models, boolean fromCache) {
			_models = List.copyOf(models);
			_fromCache = fromCache;
		}

		List<String> models() {
			return _models;
		}

		boolean fromCache() {
			return _fromCache;
		}
	}

	static String resolveBaseUrl(String providerName, String configuredBaseUrl) {
		String baseUrl = LLMProviderRegistry.normalizeProviderBaseUrl(configuredBaseUrl);
		if (baseUrl != null) {
			return baseUrl;
		}

		String name = providerName.toLowerCase(Locale.ROOT);
		if (name.contains("anthropic")) {
			return "https://api.anthropic.com";
		}
		if (LLMProviderRegistry.isCustomOpenAICompatibleProviderName(name)) {
			return "http://localhost:11434";
		}
		if (name.contains("openai")) {
			return "https://api.openai.com";
		}
		if (name.contains("mistral")) {
			return "https://api.mistral.ai";
		}
		if (name.contains("xai")) {
			return "https://api.x.ai";
		}
		return "https://api." + name + ".com";
	}

	static boolean isAuthOptionalProvider(String providerName) {
		return LLMProviderRegistry.isCustomOpenAICompatibleProviderName(providerName);
	}

	private static List<String> parseModelIds(String json) {
		List<String> models = new ArrayList<>();
		try (JsonReader reader = new JsonReader(new StringR(json))) {
			reader.beginObject();
			while (reader.hasNext()) {
				String field = reader.nextName();
				if ("data".equals(field)) {
					reader.beginArray();
					while (reader.hasNext()) {
						String modelId = parseModelEntry(reader);
						if (modelId != null && !modelId.isBlank()) {
							models.add(modelId);
						}
					}
					reader.endArray();
				} else {
					reader.skipValue();
				}
			}
			reader.endObject();
		} catch (Exception ex) {
			Logger.warn("Failed to parse model list JSON: " + ex.getMessage(), ProviderModelDiscovery.class);
		}
		return models;
	}

	private static String parseModelEntry(JsonReader reader) throws Exception {
		String id = null;
		reader.beginObject();
		while (reader.hasNext()) {
			String field = reader.nextName();
			if ("id".equals(field)) {
				id = reader.nextString();
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();
		return id;
	}

	private static final class CachedModelList {
		final List<String> _models;
		final long _loadedAtMillis;

		CachedModelList(List<String> models, long loadedAtMillis) {
			_models = List.copyOf(models);
			_loadedAtMillis = loadedAtMillis;
		}
	}
}
