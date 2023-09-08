/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.Log;

/**
 * Resolver that removes references from a given set of named items by inlining references.
 * 
 * @param <I>
 *        The reference identifier type.
 * @param <T>
 *        The item type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ReferenceResolver<I, T> {

	/**
	 * Algorithm to access items in a {@link ReferenceResolver}.
	 */
	public interface Analyzer<I, T> {

		/**
		 * Whether the given item is an aggregation that can be {@link #decompose(Object)
		 * decomposed}.
		 */
		boolean isAggregation(T item);

		/**
		 * The decomposition of the given {@link #isAggregation(Object) aggregation}.
		 */
		List<T> decompose(T aggregation);

		/**
		 * Recombines the given items to an {@link #isAggregation(Object) aggregation}.
		 */
		T compose(List<T> items);

		/**
		 * Whether the given item is a reference that can be {@link #getReferenceId(Object)
		 * resolved}.
		 */
		boolean isReference(T item);

		/**
		 * The identifier of the given {@link #isReference(Object) reference}.
		 */
		I getReferenceId(T reference);

	}

	private final Map<I, T> _definitions;

	private final Analyzer<I, T> _analyzer;

	private final Log _log;

	/**
	 * Creates a {@link ReferenceResolver}.
	 * 
	 * @param log
	 *        Log of resolve errors.
	 * @param definitions
	 *        The definitions to simplify.
	 */
	public ReferenceResolver(Log log, Analyzer<I, T> analyzer, Map<I, T> definitions) {
		_log = log;
		_analyzer = analyzer;
		_definitions = definitions;
	}

	/**
	 * Starts the simplification.
	 */
	public void resolve() {
		simplify(new LinkedHashSet<>());
	}

	private void simplify(Set<I> simplifyingIds) {
		for (Entry<I, T> entry : _definitions.entrySet()) {
			T item = entry.getValue();

			I itemId = entry.getKey();
			T simplifiedItems;
			try {
				simplifiedItems = descend(simplifyingIds, itemId, item);
				entry.setValue(simplifiedItems);
			} catch (CycleDetected ex) {
				// Error has been reported, continue with other items.
			}
		}
	}

	private T simplifyItem(Set<I> simplifyingIds, I currentId, T item) throws CycleDetected {
		if (_analyzer.isReference(item)) {
			I itemId = _analyzer.getReferenceId(item);
			T referencedItem = _definitions.get(itemId);
			if (referencedItem == null) {
				_log.error("Undefined reference '" + itemId + "' in item with ID '" + currentId + "'.");
				return item;
			}
			return descend(simplifyingIds, itemId, referencedItem);
		}
		else if (_analyzer.isAggregation(item)) {
			List<T> aggregation = _analyzer.decompose(item);
			List<T> simplifiedItems = new ArrayList<>(aggregation.size());
			for (T combinedItem : aggregation) {
				T simplifiedItem = simplifyItem(simplifyingIds, currentId, combinedItem);
				simplifiedItems.add(simplifiedItem);
			}
			return _analyzer.compose(simplifiedItems);
		}
		else {
			return item;
		}
	}

	private T descend(Set<I> simplifyingIds, I itemId, T item) throws CycleDetected {
		boolean alreadyWorkingOn = !simplifyingIds.add(itemId);
		if (alreadyWorkingOn) {
			_log.error("Cyclic reference detected in item with IDs: "
				+ simplifyingIds);

			// Do not inline anything that is part of the cycle.
			throw new CycleDetected();
		}
		try {
			return simplifyItem(simplifyingIds, itemId, item);
		} finally {
			simplifyingIds.remove(itemId);
		}
	}

	static class CycleDetected extends Exception {
		public CycleDetected() {
			super();
		}
	}

}
