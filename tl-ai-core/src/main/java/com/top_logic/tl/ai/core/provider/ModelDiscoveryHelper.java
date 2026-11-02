/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.top_logic.basic.Logger;
import com.top_logic.basic.credential.CredentialProvider;

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
	 * Fetches model IDs from the given provider's {@code /v1/models} endpoint.
	 *
	 * @param credentials
	 *        Authentication of the request.
	 * @param baseUrl
	 *        The provider base URL (without trailing slash or {@code /v1}).
	 * @return List of discovered model IDs, or empty list on failure.
	 */
	public static List<String> fetchModels(CredentialProvider credentials, String baseUrl) {
		return fetchModels(credentials, baseUrl, Map.of());
	}

	/**
	 * Fetches model IDs from the given provider's {@code /v1/models} endpoint, sending additional
	 * headers.
	 *
	 * @param credentials
	 *        Authentication of the request.
	 * @param baseUrl
	 *        The provider base URL (without trailing slash or {@code /v1}).
	 * @param headers
	 *        Additional HTTP headers the provider requires (e.g. its API version).
	 * @return List of discovered model IDs, or empty list on failure.
	 */
	public static List<String> fetchModels(CredentialProvider credentials, String baseUrl,
			Map<String, String> headers) {
		try {
			Map<String, String> requestHeaders = requestHeaders(credentials, headers);
			String cacheKey = cacheKey(baseUrl, requestHeaders);
			long now = System.currentTimeMillis();

			CachedModelList cached = CACHE.get(cacheKey);
			if (cached != null && (now - cached._loadedAtMillis) < CACHE_TTL_MILLIS) {
				return cached._models;
			}

			HttpResponse<String> response = send(baseUrl, requestHeaders);
			if (response.statusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
				// Cached credentials (an access token) may have expired or been revoked: one more
				// attempt with fresh ones.
				credentials.invalidate();
				requestHeaders = requestHeaders(credentials, headers);
				cacheKey = cacheKey(baseUrl, requestHeaders);
				response = send(baseUrl, requestHeaders);
			}

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
	 * The headers of a discovery request: the given ones and the current credentials.
	 */
	private static Map<String, String> requestHeaders(CredentialProvider credentials, Map<String, String> headers) {
		Map<String, String> result = new LinkedHashMap<>(headers);
		credentials.authenticate(result::put);
		return result;
	}

	/**
	 * The key of the discovery result for the given endpoint, distinguishing the credentials used.
	 */
	private static String cacheKey(String baseUrl, Map<String, String> requestHeaders) {
		return baseUrl + "|" + requestHeaders.hashCode();
	}

	private static HttpResponse<String> send(String baseUrl, Map<String, String> requestHeaders)
			throws IOException, InterruptedException {
		HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
			.uri(URI.create(baseUrl + "/v1/models"))
			.GET()
			.timeout(Duration.ofSeconds(30));
		for (Map.Entry<String, String> header : requestHeaders.entrySet()) {
			requestBuilder.header(header.getKey(), header.getValue());
		}

		HttpClient client = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(10))
			.build();
		return client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
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
