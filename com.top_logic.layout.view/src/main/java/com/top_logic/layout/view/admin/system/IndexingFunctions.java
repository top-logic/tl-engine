/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.system;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.knowledge.indexing.DefaultIndexingService;
import com.top_logic.knowledge.indexing.IndexingService;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.operations.AdminOnly;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.util.TLCollator;
import com.top_logic.util.error.TopLogicException;

/**
 * TL-Script functions maintaining the full-text index of the running {@link IndexingService}: the
 * commands of the detail page of that service in the administration.
 *
 * <p>
 * Rebuilding and re-indexing read every object of the indexed types and may take a long time, so
 * a view runs them as the body of a background job:
 * </p>
 *
 * <pre>
 * &lt;start-job job="job" function="{ $job.jobIndeterminate(); indexingRebuild(); }"/&gt;
 * </pre>
 *
 * @implNote Every public static method here is a TL-Script function named by the
 *           {@link ScriptPrefix} of this class followed by the capitalized method name, so
 *           {@link #rebuild()} is called <code>indexingRebuild</code>. The functions change the
 *           index of the application, which only an administrator may do from a script entered
 *           interactively.
 */
@ScriptPrefix(IndexingFunctions.PREFIX)
@AdminOnly
public class IndexingFunctions extends TLScriptFunctions {

	/** Prefix all function names of this class start with. */
	public static final String PREFIX = "indexing";

	/**
	 * The types of objects the running indexing service indexes.
	 *
	 * @return The names of the indexed object types in alphabetical order; empty if no indexing
	 *         service is running.
	 *
	 * @see IndexingService#getTypesToIndex()
	 */
	@Label("Types of the indexed objects")
	public static List<String> types() {
		if (!DefaultIndexingService.isAvailable()) {
			return List.of();
		}
		Collection<String> types = service().getTypesToIndex();
		if (types == null) {
			return List.of();
		}
		return types.stream().sorted(new TLCollator()).toList();
	}

	/**
	 * Feeds the objects of the given types to the running indexing service.
	 *
	 * @param types
	 *        The names of the object types to index, see {@link #types()}; nothing or an empty
	 *        collection for all indexed types.
	 * @param onlyNew
	 *        Whether only objects not yet in the index are indexed.
	 * @return The number of objects indexed.
	 *
	 * @see IndexingService#reindex(Collection, Collection, boolean)
	 */
	@Label("Re-index objects")
	public static Double reindex(Collection<?> types, boolean onlyNew) {
		List<String> typeNames = types == null ? List.of()
			: types.stream().filter(Objects::nonNull).map(Object::toString).toList();
		return SearchExpression.toNumber(service().reindex(typeNames.isEmpty() ? null : typeNames, null, onlyNew));
	}

	/**
	 * Drops the index of the running indexing service and indexes all objects of the indexed
	 * types again.
	 *
	 * @see IndexingService#rebuildIndex()
	 */
	@Label("Rebuild the full-text index")
	public static void rebuild() {
		service().rebuildIndex();
	}

	private static IndexingService service() {
		if (!DefaultIndexingService.isAvailable()) {
			throw new TopLogicException(I18NConstants.ERROR_NO_INDEXING_SERVICE);
		}
		return DefaultIndexingService.getIndexingService();
	}

}
