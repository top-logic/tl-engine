/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.col.MapUtil;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.search.QueryArguments;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.search.RevisionQueryArguments;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.db2.expr.visit.SimpleExpressionEvaluator;
import com.top_logic.model.TLObject;

/**
 * Helper class to adapt a list of items that match a given {@link SimpleQuery}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
abstract class SearchResultAdaptation<E> {

	private final SimpleQuery<E> _simpleQuery;

	private final RevisionQuery<E> _query;

	private final Map<String, Object> _searchArguments;

	private final Branch _branch;

	public SearchResultAdaptation(KnowledgeBase kb, SimpleQuery<E> simpleQuery, RevisionQuery<E> query,
			RevisionQueryArguments arguments) {
		_simpleQuery = simpleQuery;
		_query = query;
		_searchArguments = SearchResultAdaptation.createArguments(query, arguments);
		_branch =
			QueryArguments.resolveRequestedBranch(arguments, simpleQuery.getBranchParam(), kb.getHistoryManager());
	}

	private static Map<String, Object> createArguments(RevisionQuery<?> query, RevisionQueryArguments arguments) {
		Map<String, Object> searchArguments;
		Map<String, Integer> argumentIndexByName = query.getArgumentIndexByName();
		if (argumentIndexByName.size() == 0) {
			searchArguments = Collections.<String, Object> emptyMap();
		} else {
			Object[] args = arguments.getArguments();
			searchArguments = MapUtil.newMap(argumentIndexByName.size());
			for (Entry<String, Integer> entry : argumentIndexByName.entrySet()) {
				searchArguments.put(entry.getKey(), args[entry.getValue()]);
			}
		}
		return searchArguments;
	}

	protected boolean resolve() {
		return _simpleQuery._resolve;
	}

	protected Class<E> wrapperClass() {
		return _simpleQuery._implClass;
	}

	protected SetExpression search() {
		return _query.getSearch();
	}

	protected Map<String, Object> searchArguments() {
		return _searchArguments;
	}

	protected Branch branch() {
		return _branch;
	}
	
	/**
	 * Adapts the given items according to the specification of the concrete implementation.
	 * 
	 * @param dbResult
	 *        The list of items to adapt.
	 * @param inline
	 *        Whether the given list should be modified inline. If <code>false</code> the given list
	 *        remains untouched and a new list is returned (if necessary).
	 * @return Either <code>null</code> or a {@link List} instance containing the modified list. If
	 *         <code>null</code>, then no change occurred.
	 */
	public List<E> adaptResult(List<E> dbResult, boolean inline) {
		/* Drop modified items. They are added from the changed objects if they still match. This
		 * automatically drops removed objects. */
		List<E> droppedChanged = dropChangedObjects(dbResult, inline);
		if (droppedChanged != null) {
			/* Method changed list. Therefore the changed list can be modified. */
			inline = true;
			dbResult = droppedChanged;
		}

		/* Add newly created objects, that match the search filter. */
		List<E> addedNew = filter(createdObjects(), dbResult, inline);
		if (addedNew != null) {
			/* Method changed list. Therefore the changed list can be modified. */
			inline = true;
			dbResult = addedNew;
		}

		/* Add changed objects, that match the search filter. */
		List<E> addedChanged = filter(changedObjects(), dbResult, inline);
		if (addedChanged != null) {
			/* Method changed list. Therefore the changed list can be modified. */
			inline = true;
			dbResult = addedChanged;
		}

		if (addedChanged != null || addedNew != null || droppedChanged != null) {
			return dbResult;
		}

		return null;
	}

	/**
	 * Items that are changed and must be checked, whether they match the query.
	 */
	protected abstract Collection<? extends KnowledgeItem> changedObjects();

	/**
	 * Items that are created and must be checked, whether they match the query.
	 */
	protected abstract Collection<? extends KnowledgeItem> createdObjects();

	private List<E> dropChangedObjects(List<E> items, boolean inline) {
		boolean modified = false;
		List<E> result = items;
		int writeIdx = 0;
		for (int readIdx = 0, cnt = items.size(); readIdx < cnt; readIdx++) {
			E item = items.get(readIdx);
			KnowledgeItem ki;
			if (resolve()) {
				ki = ((TLObject) item).tHandle();
			} else {
				ki = (KnowledgeItem) item;
			}
			boolean locallyChanged = isChangedOrDropped(ki);
			if (inline) {
				if (!locallyChanged) {
					items.set(writeIdx++, item);
				}
			} else {
				if (locallyChanged) {
					if (writeIdx == readIdx) {
						result = new ArrayList<>(items.subList(0, readIdx));
					}
				} else {
					if (writeIdx != readIdx) {
						result.add(writeIdx, item);
					}
					writeIdx++;
				}
			}
			if (locallyChanged) {
				modified = true;
			}
		}
		// Shrink result.
		for (int n = result.size() - 1; n >= writeIdx; n--) {
			result.remove(n);
		}
		if (modified) {
			return result;
		}
		assert result == items;
		return null;
	}

	/**
	 * Whether the given {@link KnowledgeItem} is changed or dropped
	 */
	protected abstract boolean isChangedOrDropped(KnowledgeItem ki);

	private List<E> filter(Collection<? extends KnowledgeItem> potentialMatches, List<E> matchingItems,
			boolean inline) {
		boolean modified = false;
		List<E> result = matchingItems;
		for (KnowledgeItem item : potentialMatches) {
			if (branch() != null) {
				MOStructure itemType = item.tTable();
				long searchedDataBranch = branch().getBaseBranchId(itemType);
				long itemDataBranch = item.getBranchContext();
				if (itemDataBranch != searchedDataBranch) {
					continue;
				}
			} else {
				// don't care about branch, each object is searched
			}
			if (SimpleExpressionEvaluator.contains(search(), item, searchArguments())) {
				if (!inline && result == matchingItems) {
					result = new ArrayList<>(matchingItems);
				}
				result.add(wrapperClass().cast(resolve() ? item.getWrapper() : item));
				modified = true;
			}
		}
		if (modified) {
			return result;
		}
		assert result == matchingItems;
		return null;
	}

}
