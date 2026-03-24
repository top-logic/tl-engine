/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.ai.vector.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.checkerframework.dataflow.qual.SideEffectFree;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import opennlp.tools.cmdline.ArgumentParser.OptionalParameter;

/**
 * Class defining TLScript functions related to Vector Indexing.
 * 
 * <p>
 * This class contains methods which are transformed to TLScript functions. They expose
 * functionalities related to Vector Indexing, such as search
 * </p>
 *
 * @author <a href=mailto:boris.nikolov@top-mapps.com>Boris Nikolov</a>
 * 
 */
@ScriptPrefix("vectorIndex")
public class VectorIndexTLScriptFunctions extends TLScriptFunctions {

	/**
	 * Returns a list of results from the
	 *
	 * <p>
	 * Example usage in TL-Script:
	 * </p>
	 * 
	 * <pre>{@code { franceVacations = vectorIndexSearch("all
	 * vacations in France", 2); $franceVacations.map(x -> { "internalId":
	 * $x["embedded"]["text"].parseJson()["internalId"], "TopLogic-type":
	 * $x["embedded"]["text"].parseJson()["TopLogic-type"], "Instance":
	 * (`test.indexingVector:Vacation`).filter(a ->
	 * $a.get(`test.indexingVector:Vacation#id`).equals($x["embedded"]["text"].parseJson()["internalId"]))
	 * }) } }</pre>
	 * 
	 * @param query
	 *              String containing the string to search the vector database @param limit Integer -
	 *              number of records to return (limit, defaults to 5) @param scoreThreshhold Double -
	 *              minimum score to return. Must be a number between 0 and 1, defaults to 0.8 @return A
	 *              list of records from the vector database, matching the search string and
	 *              params. @throws Exception Exception when parameters are incorrect.
	 */
	@Label("Search for a string")
	@SideEffectFree
	public static List<Map<String, Object>> search(@Mandatory String query,
			@OptionalParameter Integer limit, @OptionalParameter Double scoreThreshhold) throws Exception {
		if (limit == null) {
			limit = 5;
		}

		if (scoreThreshhold == null) {
			scoreThreshhold = 0.8;
		}
		if (scoreThreshhold < 0 || scoreThreshhold > 1) {
			throw new Exception("scoreThreshhold must be a number between 0 and 1.");
		}

		List<EmbeddingMatch<TextSegment>> matches =
			VectorIndex.getInstance().searchVectorIndex(query, limit, scoreThreshhold);
		List<Map<String, Object>> results = new ArrayList<>();

		for (EmbeddingMatch<TextSegment> match : matches) {
			Map<String, Object> matchItem = new HashMap<>();
			matchItem.put("score", match.score());
			matchItem.put("embeddingId", match.embeddingId());

			if (match.embedded() != null) {
				Map<String, Object> segmentItem = new HashMap<>();
				segmentItem.put("text", match.embedded().text());
				if (match.embedded().metadata() != null) {
					segmentItem.put("metadata", match.embedded().metadata().toMap());
				}
				matchItem.put("embedded", segmentItem);
			}

			results.add(matchItem);
		}

		return results;
	}
}