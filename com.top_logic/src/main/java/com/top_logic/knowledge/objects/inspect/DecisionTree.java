/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects.inspect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.inspect.build.DecisionTreeBuilder;

/**
 * Algorithm providing matching values for a {@link KnowledgeItem} based on its properties.
 * 
 * @param <T>
 *        The value type.
 * 
 * @see DecisionTreeBuilder
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class DecisionTree<T> {

	/**
	 * Retrieve values matching the given item as list.
	 * 
	 * @param item
	 *        The item for which values should be retrieved.
	 * @return List of matching values.
	 */
	public List<T> getMatchList(KnowledgeItem item) {
		ArrayList<T> result = new ArrayList<>();
		addMatches(item, result);
		return result;
	}

	/**
	 * Retrieve values matching the given item as set.
	 * 
	 * @param item
	 *        The item for which values should be retrieved.
	 * @return Set of matching values.
	 */
	public Set<T> getMatchSet(KnowledgeItem item) {
		Set<T> result = new HashSet<>();
		addMatches(item, result);
		return result;
	}

	/**
	 * Adds values matching the given item to the given result collection.
	 * 
	 * @param item
	 *        The item for which values should be retrieved.
	 * @param result
	 *        The {@link Collection} to add matches to.
	 */
	public abstract void addMatches(KnowledgeItem item, Collection<T> result);

	/**
	 * Retrieves the first matching value for the given item.
	 * 
	 * <p>
	 * This method is especially useful, if it is clear from the context that at most one value may
	 * match any item.
	 * </p>
	 * 
	 * @param item
	 *        The item to analyze.
	 * @return The first value matching the given item, or <code>null</code> if no value matches the
	 *         given item.
	 */
	public abstract T getFirstMatch(KnowledgeItem item);

}