/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.dob.MetaObject;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.expr.transform.BranchExpressionEnhancement;

/**
 * Base class for queries in a {@link KnowledgeBase}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractQuery<T extends AbstractQuery<T>> extends AbstractQueryPart {
	
	private BranchParam branchParam;
	private RangeParam rangeParam;
	private List<ParameterDeclaration> searchParams;
	
	private SetExpression search;
	private Map<String, Integer> argumentIndexByName;

	private ArrayList<MetaObject> allOfTypes;

	/**
	 * Creates a {@link AbstractQuery}.
	 * 
	 * @param branchParam
	 *        See {@link #getBranchParam()}.
	 * @param rangeParam
	 *        See {@link #getRangeParam()}.
	 * @param searchParams
	 *        See {@link #getSearchParams()}.
	 * @param search
	 *        See {@link #getSearch()}.
	 */
	AbstractQuery(BranchParam branchParam, RangeParam rangeParam, List<ParameterDeclaration> searchParams, SetExpression search) {
		this.branchParam = branchParam;
		this.rangeParam = rangeParam;
		this.searchParams = searchParams;
		this.search = search;
		this.argumentIndexByName = indexParameters(searchParams);
	}
	
	private static HashMap<String, Integer> indexParameters(List<ParameterDeclaration> searchParams) {
		HashMap<String, Integer> indexByName = new HashMap<>();

		for (int n = 0, cnt = searchParams.size(); n < cnt; n++) {
			ParameterDeclaration decl = searchParams.get(n);
			
			Integer clash = indexByName.put(decl.getName(), n);
			assert clash == null : "Parameter names must be unique: " + decl.getName();
		}
		
		return indexByName;
	}

	/**
	 * Abstract specification of the {@link Branch}es to search.
	 * 
	 * @see BranchParam
	 */
	public BranchParam getBranchParam() {
		return branchParam;
	}
	
	/**
	 * @see #getBranchParam()
	 * 
	 * @return This instance for call chaining.
	 */
	public T setBranchParam(BranchParam branchParam) {
		this.branchParam = branchParam;
		return chain();
	}
	
	/**
	 * Abstract specification of the range of {@link Revision}s to search.
	 * 
	 * @see RangeParam
	 */
	public RangeParam getRangeParam() {
		return rangeParam;
	}
	
	/**
	 * @see #getRangeParam()
	 * 
	 * @return This instance for call chaining.
	 */
	public T setRangeParam(RangeParam rangeParam) {
		this.rangeParam = rangeParam;
		return chain();
	}
	
	/**
	 * The declared parameters of this query.
	 */
	public List<ParameterDeclaration> getSearchParams() {
		return searchParams;
	}

	/**
	 * @see #getSearchParams()
	 * 
	 * @return This instance for call chaining.
	 */
	public T setSearchParams(List<ParameterDeclaration> searchParams) {
		this.searchParams = searchParams;
		this.argumentIndexByName = indexParameters(searchParams);
		return chain();
	}

	/**
	 * The search expression describing the set of requested results.
	 */
	public SetExpression getSearch() {
		return search;
	}
	
	/**
	 * @see #getSearch()
	 * 
	 * @return This instance for call chaining.
	 */
	public T setSearch(SetExpression search) {
		this.search = search;
		return chain();
	}

	/**
	 * Index of {@link #getSearchParams()}.
	 * 
	 * @return A mapping of parameter names to indices in the
	 *         {@link #getSearchParams()} list.
	 */
	public Map<String, Integer> getArgumentIndexByName() {
		return argumentIndexByName;
	}
	
	/**
	 * a reference to this {@link AbstractQuery}
	 */
	protected abstract T chain();

	/**
	 * Sets value of {@link #getAllOfTypes()}
	 */
	public void setAllOfTypes(ArrayList<MetaObject> result) {
		this.allOfTypes = result;
	}

	/**
	 * Sets types of the {@link AllOf} in this query to be able to transform concrete branch into
	 * data branch of its branch.
	 *
	 * @see BranchExpressionEnhancement
	 */
	public ArrayList<MetaObject> getAllOfTypes() {
		return allOfTypes;
	}

}
