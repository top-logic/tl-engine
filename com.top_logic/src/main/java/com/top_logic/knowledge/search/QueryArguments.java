/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import java.util.Collections;
import java.util.Set;

import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryManager;

/**
 * Base class for arguments of an {@link AbstractQuery}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class QueryArguments<T extends QueryArguments<T>> {
	
	private static final Object[] NO_ARGUMENTS = {};
	
	private Set<Branch> requestedBranches;
	private int stopRow = -1;
	private Object[] arguments = NO_ARGUMENTS;

	/**
	 * The set of {@link Branch}es the query should be executed in.
	 */
	public Set<Branch> getRequestedBranches() {
		return requestedBranches;
	}
	
	/**
	 * Execute the query within the given single {@link Branch}.
	 * 
	 * @return This instance for chaining calls.
	 * 
	 * @see BranchParam
	 */
	public T setRequestedBranch(Branch requestedBranch) {
		this.requestedBranches = Collections.singleton(requestedBranch);
		return chain();
	}
	
	/**
	 * Execute the query within the given number of {@link Branch}es.
	 * 
	 * @return This instance for chaining calls.
	 * 
	 * @see BranchParam
	 */
	public T setRequestedBranches(Set<Branch> requestedBranches) {
		this.requestedBranches = requestedBranches;
		return chain();
	}
	
	/**
	 * The index of the last result (exclusive).
	 * 
	 * @see RangeParam
	 */
	public int getStopRow() {
		return stopRow;
	}
	
	/**
	 * @see #getStopRow()
	 * 
	 * @return This instance for call chaining.
	 */
	public T setStopRow(int stopRow) {
		this.stopRow = stopRow;
		return chain();
	}
	
	/**
	 * The argument values of the query.
	 * 
	 * @see AbstractQuery#getSearchParams()
	 */
	public Object[] getArguments() {
		return arguments;
	}
	
	/**
	 * @see #getArguments()
	 * 
	 * @return This instance for call chaining.
	 */
	public T setArguments(Object... arguments) {
		this.arguments = arguments;
		for (int index = 0, length = arguments.length; index < length; index++) {
			Object argument = arguments[index];
			if (argument == null) {
				// null-Literals are not allowed #9479
				throw new IllegalArgumentException("Arguments can not be 'null'.");
			}
			if (argument instanceof KnowledgeItem) {
				arguments[index] = ((KnowledgeItem) argument).tId();
			}
		}
		return chain();
	}

	/**
	 * This instance for chaining calls.
	 */
	protected abstract T chain();

	/**
	 * Resolves a single branch from the {@link QueryArguments} depending on the given branch
	 * parameter.
	 * 
	 * @see QueryArguments#getRequestedBranches()
	 */
	public static Branch resolveRequestedBranch(QueryArguments<?> queryArgs, BranchParam branchType,
			HistoryManager hm) {
		Set<Branch> requestedBranches = queryArgs.getRequestedBranches();
		final Branch requestedBranch;
		switch (branchType) {
			case all:
				requestedBranch = null;
				break;
			case single:
				if (requestedBranches == null) {
					requestedBranch = hm.getContextBranch();
				} else {
					requestedBranch = requestedBranches.iterator().next();
				}
				break;
			case set:
				// TODO:
				throw new UnsupportedOperationException("Search in set of branches.");
			case without:
				// TODO:
				throw new UnsupportedOperationException("Search in all branches without set of branches.");
			default:
				throw BranchParam.unknownRangeParam(branchType);
		}
		return requestedBranch;
	}

}
