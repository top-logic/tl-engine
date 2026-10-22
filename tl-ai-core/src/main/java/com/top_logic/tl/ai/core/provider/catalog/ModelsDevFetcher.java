/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.catalog;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo.InterleavedConfig;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo.ModelCapabilities;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo.ModelCost;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo.ModelDates;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo.ModelLimits;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo.ModelModalities;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo.TieredCost;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.json.JsonToken;

/**
 * Fetches and parses model metadata from the models.dev catalog API.
 *
 * <p>
 * Given a map of catalog provider IDs to requested model IDs, this class fetches
 * the JSON catalog - or reads the bundled snapshot of it - and extracts only the requested entries
 * into {@link ModelInfo} records.
 * </p>
 */
public final class ModelsDevFetcher {

	private static final String SNAPSHOT_RESOURCE = "models-dev-snapshot.json";

	private ModelsDevFetcher() {
		// Utility class.
	}

	/**
	 * Fetches model metadata from the given catalog URL.
	 *
	 * <p>
	 * The whole exchange - name resolution, connect, headers and body - must complete within the
	 * given deadline. The JDK's request timeout ends when the headers arrive, so a server that
	 * answers at once and then sends its body slowly would otherwise hold the caller for as long as
	 * it likes. When the deadline passes, the request is cancelled.
	 * </p>
	 *
	 * @param catalogUrl
	 *        The URL to fetch (e.g. "https://models.dev/api.json").
	 * @param requestedModels
	 *        Map of catalog provider ID to the set of model IDs to extract.
	 * @param deadline
	 *        The time the whole fetch may take.
	 * @return Map of model ID to parsed {@link ModelInfo}, for all successfully resolved models.
	 * @throws IOException
	 *         If the catalog could not be fetched within the deadline; the message says why.
	 * @throws InterruptedException
	 *         If the calling thread was interrupted while waiting; the request is cancelled.
	 */
	public static Map<String, ModelInfo> fetch(String catalogUrl, Map<String, Set<String>> requestedModels,
			Duration deadline) throws IOException, InterruptedException {
		HttpRequest request;
		try {
			request = HttpRequest.newBuilder()
				.uri(URI.create(catalogUrl))
				.GET()
				.timeout(deadline)
				.build();
		} catch (IllegalArgumentException ex) {
			throw new IOException("invalid catalog URL '" + catalogUrl + "'", ex);
		}
		HttpClient client = HttpClient.newBuilder()
			.connectTimeout(deadline)
			.build();
		CompletableFuture<HttpResponse<String>> exchange =
			client.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		HttpResponse<String> response;
		try {
			response = exchange.get(deadline.toMillis(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException ex) {
			throw new IOException("no complete answer within " + deadline.toSeconds() + " s", ex);
		} catch (ExecutionException ex) {
			Throwable cause = ex.getCause();
			throw new IOException(cause.getMessage() != null ? cause.getMessage() : cause.toString(), cause);
		} finally {
			// Aborts the exchange when it did not complete - also its body read.
			exchange.cancel(true);
		}
		if (response.statusCode() != 200) {
			throw new IOException("HTTP " + response.statusCode());
		}
		Map<String, ModelInfo> result = new HashMap<>();
		try {
			parse(response.body(), requestedModels, result);
			return result;
		} catch (Exception ex) {
			// Not swallowed as in parseJson(): an unreadable answer - a proxy's login page served
			// with 200 - must not replace a catalog that is known to be good.
			throw new IOException("unreadable catalog: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Loads model metadata from the bundled snapshot resource.
	 *
	 * @param requestedModels
	 *        Map of catalog provider ID to the set of model IDs to extract.
	 * @return Map of model ID to parsed {@link ModelInfo}, for all models the snapshot knows.
	 */
	public static Map<String, ModelInfo> loadSnapshot(Map<String, Set<String>> requestedModels) {
		try (InputStream in = ModelsDevFetcher.class.getResourceAsStream(SNAPSHOT_RESOURCE)) {
			if (in == null) {
				Logger.error("Bundled models.dev snapshot not found on classpath.", ModelsDevFetcher.class);
				return Map.of();
			}
			String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
			return parseJson(json, requestedModels);
		} catch (IOException ex) {
			Logger.error("Failed to load bundled models.dev snapshot: " + ex.getMessage(), ModelsDevFetcher.class);
			return Map.of();
		}
	}

	/**
	 * Parses the models.dev JSON catalog, extracting only the requested models.
	 *
	 * @param json
	 *        The raw JSON string from models.dev.
	 * @param requestedModels
	 *        Map of catalog provider ID to the set of model IDs to extract.
	 * @return Map of model ID to parsed {@link ModelInfo}.
	 */
	public static Map<String, ModelInfo> parseJson(String json, Map<String, Set<String>> requestedModels) {
		Map<String, ModelInfo> result = new HashMap<>();
		try {
			parse(json, requestedModels, result);
		} catch (Exception ex) {
			Logger.warn("Failed to parse models.dev JSON: " + ex.getMessage(), ModelsDevFetcher.class);
		}
		return result;
	}

	private static void parse(String json, Map<String, Set<String>> requestedModels, Map<String, ModelInfo> result)
			throws Exception {
		try (JsonReader reader = new JsonReader(new StringR(json))) {
			reader.beginObject();
			while (reader.hasNext()) {
				String providerId = reader.nextName();
				Set<String> wantedModels = requestedModels.get(providerId);
				if (wantedModels == null) {
					reader.skipValue();
					continue;
				}
				parseProvider(reader, wantedModels, result);
			}
			reader.endObject();
		}
	}

	private static void parseProvider(JsonReader reader, Set<String> wantedModels, Map<String, ModelInfo> result)
			throws Exception {
		reader.beginObject();
		while (reader.hasNext()) {
			String field = reader.nextName();
			if ("models".equals(field)) {
				parseModelsObject(reader, wantedModels, result);
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();
	}

	private static void parseModelsObject(JsonReader reader, Set<String> wantedModels, Map<String, ModelInfo> result)
			throws Exception {
		reader.beginObject();
		while (reader.hasNext()) {
			String modelKey = reader.nextName();
			if (wantedModels.contains(modelKey)) {
				ModelInfo info = parseModelEntry(reader);
				if (info != null) {
					result.put(info.id(), info);
				}
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();
	}

	private static ModelInfo parseModelEntry(JsonReader reader) throws Exception {
		String id = null;
		String family = null;
		String status = null;
		boolean reasoning = false;
		boolean toolCall = false;
		boolean temperature = false;
		boolean attachment = false;
		boolean structuredOutput = false;
		boolean openWeights = false;
		ModelModalities modalities = null;
		InterleavedConfig interleaved = null;

		double costInput = 0;
		double costOutput = 0;
		Double cacheRead = null;
		Double cacheWrite = null;
		Double costReasoning = null;
		Double inputAudio = null;
		Double outputAudio = null;
		TieredCost tieredCost = null;

		ModelLimits limits = new ModelLimits(0, 0, 0);

		String knowledgeCutoff = null;
		String releaseDate = null;
		String lastUpdated = null;

		reader.beginObject();
		while (reader.hasNext()) {
			String field = reader.nextName();
			switch (field) {
				case "id":
					id = reader.nextString();
					break;
				case "family":
					family = reader.nextString();
					break;
				case "status":
					status = reader.nextString();
					break;
				case "reasoning":
					reasoning = reader.nextBoolean();
					break;
				case "tool_call":
					toolCall = reader.nextBoolean();
					break;
				case "temperature":
					temperature = reader.nextBoolean();
					break;
				case "attachment":
					attachment = reader.nextBoolean();
					break;
				case "structured_output":
					structuredOutput = reader.nextBoolean();
					break;
				case "open_weights":
					openWeights = reader.nextBoolean();
					break;
				case "interleaved":
					interleaved = parseInterleaved(reader);
					break;
				case "knowledge":
					knowledgeCutoff = reader.nextString();
					break;
				case "release_date":
					releaseDate = reader.nextString();
					break;
				case "last_updated":
					lastUpdated = reader.nextString();
					break;
				case "modalities":
					modalities = parseModalities(reader);
					break;
				case "cost": {
					CostResult c = parseCost(reader);
					costInput = c.input;
					costOutput = c.output;
					cacheRead = c.cacheRead;
					cacheWrite = c.cacheWrite;
					costReasoning = c.reasoning;
					inputAudio = c.inputAudio;
					outputAudio = c.outputAudio;
					tieredCost = c.tiered;
					break;
				}
				case "limit":
					limits = parseLimits(reader);
					break;
				default:
					reader.skipValue();
					break;
			}
		}
		reader.endObject();

		if (id == null || limits.contextWindow() == 0) {
			return null;
		}

		ModelDates dates = (knowledgeCutoff != null || releaseDate != null || lastUpdated != null)
			? new ModelDates(knowledgeCutoff, releaseDate, lastUpdated)
			: null;

		return new ModelInfo(
			id,
			family,
			limits,
			new ModelCapabilities(reasoning, toolCall, temperature, attachment, structuredOutput, modalities,
				interleaved),
			new ModelCost(costInput, costOutput, cacheRead, cacheWrite, costReasoning, inputAudio, outputAudio,
				tieredCost),
			dates,
			openWeights,
			status);
	}

	/**
	 * Intermediate holder for parsed cost fields.
	 */
	private record CostResult(
			double input,
			double output,
			Double cacheRead,
			Double cacheWrite,
			Double reasoning,
			Double inputAudio,
			Double outputAudio,
			TieredCost tiered) {}

	private static CostResult parseCost(JsonReader reader) throws Exception {
		double input = 0;
		double output = 0;
		Double cacheRead = null;
		Double cacheWrite = null;
		Double reasoning = null;
		Double inputAudio = null;
		Double outputAudio = null;
		TieredCost tiered = null;

		reader.beginObject();
		while (reader.hasNext()) {
			String field = reader.nextName();
			switch (field) {
				case "input":
					input = reader.nextDouble();
					break;
				case "output":
					output = reader.nextDouble();
					break;
				case "cache_read":
					cacheRead = reader.nextDouble();
					break;
				case "cache_write":
					cacheWrite = reader.nextDouble();
					break;
				case "reasoning":
					reasoning = reader.nextDouble();
					break;
				case "input_audio":
					inputAudio = reader.nextDouble();
					break;
				case "output_audio":
					outputAudio = reader.nextDouble();
					break;
				case "context_over_200k":
					tiered = parseTieredCost(reader);
					break;
				default:
					reader.skipValue();
					break;
			}
		}
		reader.endObject();
		return new CostResult(input, output, cacheRead, cacheWrite, reasoning, inputAudio, outputAudio, tiered);
	}

	private static TieredCost parseTieredCost(JsonReader reader) throws Exception {
		double input = 0;
		double output = 0;
		Double cacheRead = null;
		Double cacheWrite = null;

		reader.beginObject();
		while (reader.hasNext()) {
			String field = reader.nextName();
			switch (field) {
				case "input":
					input = reader.nextDouble();
					break;
				case "output":
					output = reader.nextDouble();
					break;
				case "cache_read":
					cacheRead = reader.nextDouble();
					break;
				case "cache_write":
					cacheWrite = reader.nextDouble();
					break;
				default:
					reader.skipValue();
					break;
			}
		}
		reader.endObject();
		return new TieredCost(input, output, cacheRead, cacheWrite);
	}

	private static ModelLimits parseLimits(JsonReader reader) throws Exception {
		int context = 0;
		int output = 0;
		int input = 0;

		reader.beginObject();
		while (reader.hasNext()) {
			String field = reader.nextName();
			switch (field) {
				case "context":
					context = reader.nextInt();
					break;
				case "output":
					output = reader.nextInt();
					break;
				case "input":
					input = reader.nextInt();
					break;
				default:
					reader.skipValue();
					break;
			}
		}
		reader.endObject();
		return new ModelLimits(context, output, input);
	}

	/**
	 * Parses the {@code "interleaved"} field which can be either a boolean {@code true} or an
	 * object like {@code {"field": "reasoning_content"}}.
	 */
	private static InterleavedConfig parseInterleaved(JsonReader reader) throws Exception {
		if (reader.peek() == JsonToken.BOOLEAN) {
			return reader.nextBoolean() ? new InterleavedConfig(null) : null;
		}
		// Object form: { "field": "reasoning_content" }
		String field = null;
		reader.beginObject();
		while (reader.hasNext()) {
			String key = reader.nextName();
			if ("field".equals(key)) {
				field = reader.nextString();
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();
		return new InterleavedConfig(field);
	}

	private static ModelModalities parseModalities(JsonReader reader) throws Exception {
		List<String> input = List.of();
		List<String> output = List.of();

		reader.beginObject();
		while (reader.hasNext()) {
			String field = reader.nextName();
			switch (field) {
				case "input":
					input = parseStringArray(reader);
					break;
				case "output":
					output = parseStringArray(reader);
					break;
				default:
					reader.skipValue();
					break;
			}
		}
		reader.endObject();
		return new ModelModalities(input, output);
	}

	private static List<String> parseStringArray(JsonReader reader) throws Exception {
		List<String> items = new ArrayList<>();
		reader.beginArray();
		while (reader.hasNext()) {
			items.add(reader.nextString());
		}
		reader.endArray();
		return items;
	}
}
