/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.vector.index;

import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;

/**
 * AI tool that performs semantic search against the {@link VectorIndex}.
 *
 * <p>
 * This tool exposes the vector index search functionality to AI agents. It accepts a natural
 * language query, embeds it using the configured embedding model, and returns the most semantically
 * similar documents stored in the vector index.
 * </p>
 *
 * <p>
 * The tool is registered in the {@code ToolRegistryService} but is not assigned to any agent by default.
 * </p>
 */
public class VectorIndexSearchTool {

	/**
	 * Configuration for {@link VectorIndexSearchTool}.
	 */
	public interface Config extends PolymorphicConfiguration<VectorIndexSearchTool> {
		// no additional properties
	}

	/**
	 * Creates a new {@link VectorIndexSearchTool}.
	 */
	public VectorIndexSearchTool() {
		// stateless tool
	}

	/**
	 * Creates a new {@link VectorIndexSearchTool} from configuration.
	 */
	@SuppressWarnings("unused")
	@CalledByReflection
	public VectorIndexSearchTool(InstantiationContext context, Config config) {
		// no-op for stateless tools
	}

	/**
	 * Searches the vector index for documents semantically similar to the given query.
	 *
	 * <p>
	 * The query is embedded using the configured embedding model and matched against the stored
	 * document embeddings. Returns up to {@code maxResults} results (default: 10) ranked by
	 * similarity score.
	 * </p>
	 *
	 * @param query
	 *                   Natural language search query.
	 * @param maxResults
	 *                   Optional maximum number of results to return. If {@code null} or &lt;= 0,
	 *                   defaults to 10.
	 * @return JSON string containing an array of matching documents with their similarity scores and
	 *         text content.
	 */
	@ToolMethod(
		description = "Search the vector index for documents semantically similar to the given query. "
			+ "Returns ranked results; optionally limit the number of results via maxResults (default: 10).",
		mode = ToolMode.READ)
	public String searchVectorIndex(
			@P("Natural language search query") String query,
			@P(value = "Optional: maximum number of results to return (default: 10, must be > 0)",
				required = false) Integer maxResults) {

		if (query == null || query.trim().isEmpty()) {
			return errorJson("Search query must not be empty.");
		}

		List<EmbeddingMatch<TextSegment>> matches;
		try {
			VectorIndex vectorIndex = VectorIndex.getInstance();
			if (vectorIndex == null) {
				throw new IllegalStateException("No VectorIndex Instance was found.");
			}
			if (maxResults == null || maxResults <= 0) {
				matches = vectorIndex.searchVectorIndex(query);
			} else {
				matches = vectorIndex.searchVectorIndex(query, maxResults, 0.8);
			}
		} catch (IllegalStateException ex) {
			Logger.error("VectorIndexSearchTool: " + ex.getMessage(), ex, VectorIndexSearchTool.class);
			return errorJson(ex.getMessage());
		} catch (Exception ex) {
			Logger.error("VectorIndexSearchTool: search failed for query '" + query + "': " + ex.getMessage(), ex,
				VectorIndexSearchTool.class);
			return errorJson("Search failed: " + ex.getMessage());
		}

		return buildResultJson(query, matches);
	}

	/**
	 * Builds a JSON result string from the list of embedding matches.
	 */
	@SuppressWarnings("unchecked")
	private static String buildResultJson(String query, List<EmbeddingMatch<TextSegment>> matches) {
		JSONObject root = new JSONObject();
		root.put("success", true);
		root.put("query", query);
		root.put("resultCount", matches.size());

		JSONArray results = new JSONArray();
		for (EmbeddingMatch<TextSegment> match : matches) {
			JSONObject entry = new JSONObject();
			entry.put("score", match.score());
			String text = match.embedded() != null ? match.embedded().text() : null;
			entry.put("text", text);
			results.add(entry);
		}
		root.put("results", results);

		return root.toJSONString();
	}

	/**
	 * Builds a JSON error string with the given message.
	 */
	@SuppressWarnings("unchecked")
	private static String errorJson(String message) {
		JSONObject root = new JSONObject();
		root.put("success", false);
		root.put("error", message);
		return root.toJSONString();
	}

}
