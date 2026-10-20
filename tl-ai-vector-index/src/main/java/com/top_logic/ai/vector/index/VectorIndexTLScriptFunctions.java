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
import com.top_logic.element.meta.PersistentClass;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.tl.ai.core.security.ModelAccessRightsSecurity;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.util.TLContext;

import dev.langchain4j.data.segment.TextSegment;
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
	 * Returns a list of results from the Vector database
	 *
	 * <p>
	 * Only hits the executing account may read are returned. The index is filled by a background
	 * daemon under system rights, so it holds every object of a configured type whoever may read it;
	 * the account's own rights are applied here, at query time, and a hit that cannot be attributed to
	 * a readable object is dropped rather than reported (issue #295).
	 * </p>
	 *
	 * <p>
	 * Example usage in TL-Script to resolve objects, contained in the Vector Index result:
	 * </p>
	 * 
	 * <pre>
	 *<code>
	 * vectorIndexSearch("all vacations in France",<br/>
	 * &#160;&#160;&#160;&#160;2,<br/>
	 * &#160;&#160;&#160;&#160;0.8,<br/>
	 * &#160;&#160;&#160;&#160;"test.indexingVector:Vacation").map(x -> {<br/>
	 * &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;"score": $x["score"],<br/>
	 * &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;"instance": $x["TL-instance"]<br/>
	 * })<br/>
	 *</code>
	 * </pre>
	 * 
	 * @param query
	 *        The string query to search inside the vector database.
	 * @param limit
	 *        Optional parameter for number of expected records to return (default to number,
	 *        configured in the Service Setting).
	 * @param scoreThreshold
	 *        Minimum score threshold. Must be between 0 and 1 (defaults to 0.8).
	 * @param type
	 *        TL Script type to filter by in the Vector Index search
	 * @return Returns a list of record dictionaries matching the query and parameters. Each element
	 *         of the list has the following properties:
	 * 
	 *         <pre>
	 *         <i>$result:</i>
	 *         <table class="tlDocTable" border="0">
	 *         <thead>
	 *         		<tr>
	 * 		 			<th>name</th>
	 * 					<th>type</th>
	 * 					<th>description</th>
	 * 				</tr>
	 * 			</thead>
	 * 			<tbody>
	 * 				<tr>
	 * 					<td>score</td>
	 * 					<td>Number between 0 and 1</td>
	 * 					<td>Matching score of the element</td>
	 * 				</tr>
	 * 					<td>embeddingId</td>
	 * 					<td>UUID</td>
	 * 					<td>ID of the point in the vector database</td>
	 * 				<tr>
	 * 					<td>objectId</td>
	 * 					<td>String</td>
	 * 					<td>The object id in the TL application (e.g. GenericObject:12002)</td>
	 * 				</tr>
	 * 				<tr>
	 * 					<td>TL-instance</td>
	 * 					<td>AttributedWrapper</td>
	 * 					<td>The TL Object instance</td>
	 * 				</tr>
	 * 				<tr>
	 * 					<td>embedded</td>
	 * 					<td>Map</td>
	 * 					<td>Content of the point in the vector database</td>
	 * 				</tr>
	 * 			</tbody>
	 *        </table>
	 *        
	 *        <i>$result["embedded"]:</i>
	 *        <table class="tlDocTable" border="0">
	 *         <thead>
	 *         		<tr>
	 * 		 			<th>name</th>
	 * 					<th>type</th>
	 * 					<th>description</th>
	 * 				</tr>
	 * 			</thead>
	 * 			<tbody>
	 * 				<tr>
	 * 					<td>metadata</td>
	 * 					<td>Map</td>
	 * 					<td>Metadata stored in the vector database</td>
	 * 				</tr>
	 *        </tbody>
	 *        </table>
	 *        
	 *        <i>$result["embedded"]["metadata"]:</i>
	 *        <table class="tlDocTable" border="0">
	 *         <thead>
	 *         		<tr>
	 * 		 			<th>name</th>
	 * 					<th>type</th>
	 * 					<th>description</th>
	 * 				</tr>
	 * 			</thead>
	 * 			<tbody>
	 * 				<tr>
	 * 					<td>TopLogic-type</td>
	 * 					<td>String</td>
	 * 					<td>TopLogic type of the stored instance</td>
	 * 				</tr>
	 *        </tbody>
	 *        </table>
	 *        
	 *        $result["embedded"]["text"]:
	 *        <table class="tlDocTable" border="0">
	 *         <thead>
	 *         		<tr>
	 * 		 			<th>name</th>
	 * 					<th>type</th>
	 * 					<th>description</th>
	 * 				</tr>
	 * 			</thead>
	 * 			<tbody>
	 * 				<tr>
	 * 					<td>TLObject-creation-date</td>
	 * 					<td>String</td>
	 * 					<td>Date of TL instance creation</td>
	 * 				</tr>
	 * 				<tr>
	 * 					<td>TLObject-modification-date</td>
	 *					<td>String</td>
	 *					<td>Date of TL instance last modification</td>
	 * 				</tr>
	 * 				<tr>
	 * 					<td>All other instance attributes</td>
	 * 					<td>String</td>
	 * 					<td>All instance attribute key/value pairs</td>
	 * 				</tr>
	 *        </tbody>
	 *        </table>
	 *         </pre>
	 * 
	 * @throws Exception
	 *         When parameters are incorrect (e.g., negative threshold).
	 */
	@Label("Search for a string")
	@SideEffectFree
	public static List<Map<String, Object>> search(@Mandatory String query,
			@OptionalParameter Integer limit, @OptionalParameter Double scoreThreshold,
			@OptionalParameter PersistentClass type)
			throws Exception {


		if (scoreThreshold != null && (scoreThreshold < 0 || scoreThreshold > 1)) {
			throw new Exception("scoreThreshold must be a number between 0 and 1.");
		}

		String searchType = null;

		if (type != null) {
			searchType = type.toString();
		}

		List<Map<String, Object>> results = new ArrayList<>();

		if (query == null || searchType == null) {
			return results;
		}

		// The rights of whoever is executing this script. The index holds every object of a
		// configured type, indexed under system rights, so without this the function would report
		// objects - and their indexed text - to an account that may not read them (issue #295).
		ReadFilter readFilter =
			ReadFilter.forSecurity(new ModelAccessRightsSecurity(TLContext.currentUser()));

		VectorReadFilter.Hits hits =
			VectorIndex.getInstance().searchVectorIndex(query, searchType, limit, scoreThreshold, readFilter);

		for (VectorReadFilter.Hit hit : hits.hits()) {
			Map<String, Object> matchItem = new HashMap<>();
			matchItem.put("score", hit.match().score());
			matchItem.put("embeddingId", hit.match().embeddingId());
			matchItem.put("objectId", hit.source().tId().asString());
			matchItem.put("TL-instance", hit.source());

			TextSegment embedded = hit.match().embedded();
			if (embedded != null) {
				Map<String, Object> segmentItem = new HashMap<>();
				segmentItem.put("text", embedded.text());
				if (embedded.metadata() != null) {
					segmentItem.put("metadata", embedded.metadata().toMap());
				}
				matchItem.put("embedded", segmentItem);
			}

			results.add(matchItem);
		}

		return results;
	}
}
