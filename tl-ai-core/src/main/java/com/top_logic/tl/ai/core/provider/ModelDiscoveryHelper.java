/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.top_logic.basic.Logger;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.json.JsonReader;

/**
 * Helper for discovering model IDs from <code>OpenAI</code>-compatible {@code /v1/models} endpoints.
 *
 * <p>
 * Used by provider configs that support model discovery. Results are cached for 5 minutes.
 * </p>
 */
public final class ModelDiscoveryHelper {

	private static final long CACHE_TTL_MILLIS = Duration.ofMinutes(5).toMillis();

	private static final ConcurrentMap<String, CachedModelList> CACHE = new ConcurrentHashMap<>();

	private ModelDiscoveryHelper() {
		// Utility class.
	}

	/**
	 * Clears the model discovery cache.
	 */
	public static void clearCache() {
		CACHE.clear();
	}

	/**
	 * Fetches model IDs from the given provider's {@code /v1/models} endpoint using Bearer
	 * authentication.
	 *
	 * @param apiKey
	 *        The API key for authentication, or {@code null} for unauthenticated endpoints.
	 * @param baseUrl
	 *        The provider base URL (without trailing slash or {@code /v1}).
	 * @return List of discovered model IDs, or empty list on failure.
	 */
	public static List<String> fetchModels(String apiKey, String baseUrl) {
		Map<String, String> headers = Map.of();
		if (isValidApiKey(apiKey)) {
			headers = Map.of("Authorization", "Bearer " + apiKey);
		}
		return fetchModels(baseUrl, headers);
	}

	/**
	 * Fetches model IDs from the given provider's {@code /v1/models} endpoint using custom
	 * authentication headers.
	 *
	 * @param baseUrl
	 *        The provider base URL (without trailing slash or {@code /v1}).
	 * @param headers
	 *        Custom HTTP headers for authentication (e.g. {@code x-api-key} for Anthropic).
	 * @return List of discovered model IDs, or empty list on failure.
	 */
	public static List<String> fetchModels(String baseUrl, Map<String, String> headers) {
		String cacheKey = baseUrl + "|" + headers.hashCode();
		long now = System.currentTimeMillis();

		CachedModelList cached = CACHE.get(cacheKey);
		if (cached != null && (now - cached._loadedAtMillis) < CACHE_TTL_MILLIS) {
			return cached._models;
		}

		String endpoint = baseUrl + "/v1/models";
		HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
			.uri(URI.create(endpoint))
			.GET()
			.timeout(Duration.ofSeconds(30));

		for (Map.Entry<String, String> header : headers.entrySet()) {
			requestBuilder.header(header.getKey(), header.getValue());
		}

		try {
			HttpClient client = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(10))
				.build();
			HttpResponse<String> response = client.send(
				requestBuilder.build(),
				HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

			if (response.statusCode() != 200) {
				Logger.warn("Model discovery failed for " + baseUrl + ": HTTP " + response.statusCode(),
					ModelDiscoveryHelper.class);
				return List.of();
			}

			List<String> models = parseModelIds(response.body());
			CACHE.put(cacheKey, new CachedModelList(models, now));
			return models;
		} catch (Exception ex) {
			Logger.warn("Model discovery failed for " + baseUrl + ": " + ex.getMessage(),
				ModelDiscoveryHelper.class);
			return List.of();
		}
	}

	/**
	 * Normalizes a base URL by stripping trailing slashes and {@code /v1} suffix.
	 */
	public static String normalizeBaseUrl(String baseUrl) {
		if (baseUrl == null) {
			return null;
		}
		String trimmed = baseUrl.trim();
		while (trimmed.endsWith("/")) {
			trimmed = trimmed.substring(0, trimmed.length() - 1);
		}
		if (trimmed.endsWith("/v1")) {
			trimmed = trimmed.substring(0, trimmed.length() - 3);
		}
		return trimmed;
	}

	private static boolean isValidApiKey(String apiKey) {
		if (apiKey == null || apiKey.trim().isEmpty()) {
			return false;
		}
		if (apiKey.startsWith("%") && apiKey.endsWith("%")) {
			return false;
		}
		return true;
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
			Logger.warn("Failed to parse model list JSON: " + ex.getMessage(), ModelDiscoveryHelper.class);
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
