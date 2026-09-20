/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.vector.index;

import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tl.ai.tool.api.AnswerMarking;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * AI tool that performs semantic search against the {@link VectorIndex}.
 *
 * <p>
 * This tool exposes the vector index search functionality to AI agents. It accepts a natural
 * language query, embeds it using the configured embedding model, and returns the most semantically
 * similar objects stored in the vector index.
 * </p>
 *
 * <p>
 * <b>What it discloses.</b> The index is filled by a background daemon under system rights, so a hit
 * says nothing on its own about whether the run's principal may see it. Two things therefore happen
 * before a hit reaches the model, and neither is optional (issue #295):
 * </p>
 *
 * <ul>
 * <li>{@link VectorReadFilter} drops every hit whose source object the principal may not read, or
 * cannot be attributed to one at all.</li>
 * <li>What is then written is <b>not the indexed chunk</b> but the object as the read filter
 * renders it now, attribute by attribute and under the principal's own rights. The chunk is a
 * snapshot of every flex attribute at indexing time, so handing it over would disclose an attribute
 * the principal may not read, and a value that has since changed. That is two defects for the price
 * of one.</li>
 * </ul>
 *
 * <p>
 * The tool is registered in the {@code ToolRegistryService} but is not assigned to any agent by default.
 * </p>
 *
 * @see ReadFilter#attributesOf(com.top_logic.model.TLObject)
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
	 * Searches the vector index for objects semantically similar to the given query.
	 *
	 * <p>
	 * The query is embedded using the configured embedding model and matched against the stored
	 * embeddings. Returns up to {@code maxResults} hits (default: 10) ranked by similarity score,
	 * each one an object the run's principal may read, with the attributes it may read.
	 * </p>
	 *
	 * @param query
	 *        Natural language search query.
	 * @param maxResults
	 *        Optional maximum number of results to return. If {@code null} or &lt;= 0, defaults to 10.
	 * @param context
	 *        Injected execution context, supplying the read filter of the run.
	 * @return JSON string containing an array of matching objects with their similarity scores,
	 *         identity and readable attributes, marked as business data.
	 */
	@ToolMethod(
		description = "Search the vector index for objects semantically similar to the given query. "
			+ "Returns ranked results, each with the object's identifier and the attributes you may "
			+ "read; objects you may not read are not listed. Optionally limit the number of results "
			+ "via maxResults (default: 10).",
		mode = ToolMode.READ, group = ToolGroups.DATA_READ, marking = AnswerMarking.TOOL)
	public String searchVectorIndex(
			@P("Natural language search query") String query,
			@P(value = "Optional: maximum number of results to return (default: 10, must be > 0)",
				required = false) Integer maxResults,
			ToolExecutionContext context) {

		if (query == null || query.trim().isEmpty()) {
			return errorJson("Search query must not be empty.");
		}

		ReadFilter readFilter = ReadFilter.of(context);
		VectorReadFilter.Hits hits;
		try {
			VectorIndex vectorIndex = VectorIndex.getInstance();
			if (vectorIndex == null) {
				throw new IllegalStateException("No VectorIndex Instance was found.");
			}
			hits = vectorIndex.searchVectorIndex(query, null, maxResults, null, readFilter);
		} catch (IllegalStateException ex) {
			Logger.error("VectorIndexSearchTool: " + ex.getMessage(), ex, VectorIndexSearchTool.class);
			return errorJson(ex.getMessage());
		} catch (Exception ex) {
			Logger.error("VectorIndexSearchTool: search failed for query '" + query + "': " + ex.getMessage(), ex,
				VectorIndexSearchTool.class);
			return errorJson("Search failed: " + ex.getMessage());
		}

		// Marked as business data: what a hit renders is text other end users of the application
		// wrote, exactly like the result of any other data tool.
		return ReadFilter.marked(query, buildResultJson(query, hits, readFilter));
	}

	/**
	 * Builds a JSON result string from the readable hits.
	 *
	 * <p>
	 * Each hit is rendered from the live object through the read filter, so an attribute the principal
	 * may not read is absent and a reference to an object it may not read is dropped rather than
	 * disclosed as an identity. The indexed chunk itself is not reported.
	 * </p>
	 */
	@SuppressWarnings("unchecked")
	private static String buildResultJson(String query, VectorReadFilter.Hits hits, ReadFilter readFilter) {
		JSONObject root = new JSONObject();
		root.put("success", true);
		root.put("query", query);
		root.put("resultCount", Integer.valueOf(hits.hits().size()));
		// Says "ask for more if you need more", never "something was withheld": a short answer is
		// also what a small index gives, and the two must not be told apart from outside.
		root.put("moreAvailable", Boolean.valueOf(!hits.candidatesExhausted()));

		JSONArray results = new JSONArray();
		for (VectorReadFilter.Hit hit : hits.hits()) {
			JSONObject entry = new JSONObject();
			entry.put("score", hit.match().score());
			entry.put("object", ReadFilter.labelled(hit.source()));
			entry.put("attributes", jsonData(readFilter.attributesOf(hit.source())));
			results.add(entry);
		}
		root.put("results", results);

		return root.toJSONString();
	}

	/**
	 * The JSON-compatible data {@link ReadFilter#attributesOf(com.top_logic.model.TLObject)} produced,
	 * in the collection types this writer serializes.
	 *
	 * <p>
	 * The filter has already decided what may be disclosed and in what shape, so this makes no
	 * decision of its own and only converts.
	 * </p>
	 */
	@SuppressWarnings("unchecked")
	private static Object jsonData(Object data) {
		if (data instanceof Map<?, ?> map) {
			JSONObject result = new JSONObject();
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				result.put(String.valueOf(entry.getKey()), jsonData(entry.getValue()));
			}
			return result;
		}
		if (data instanceof Iterable<?> items) {
			JSONArray result = new JSONArray();
			for (Object item : items) {
				result.add(jsonData(item));
			}
			return result;
		}
		return data;
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
