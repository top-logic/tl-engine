/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import java.util.List;

import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.Wrapper;

/**
 * Query that searches items within a single revision.
 * 
 * @see HistoryQuery
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RevisionQuery<E> extends AbstractQuery<RevisionQuery<E>> {

	/**
	 * Possible value of {@link RevisionQuery#getLoadStrategy()}.
	 */
	public enum LoadStrategy {
		/**
		 * Use a load strategy based on context information.
		 */
		DEFAULT(false),

		/**
		 * Only request identifiers as query result, load data separately for objects not in cache.
		 */
		ID_LOAD(false),

		/**
		 * Request identifier and data as result of a query.
		 */
		FULL_LOAD(true);

		private final boolean _fullLoad;

		private LoadStrategy(boolean fullLoad) {
			_fullLoad = fullLoad;
		}

		/**
		 * Whether to request identifier and data in a single query.
		 */
		public boolean getFullLoad() {
			return _fullLoad;
		}
	}

	private Order order;

	private final Class<E> _expectedType;

	private final boolean _resolve;

	private LoadStrategy _loadStrategy = LoadStrategy.DEFAULT;

	/**
	 * Creates a {@link RevisionQuery}.
	 * 
	 * @param branchParam
	 *        See {@link #getBranchParam()}
	 * @param rangeParam
	 *        See {@link #getRangeParam()}
	 * @param searchParams
	 *        See {@link #getSearchParams()}
	 * @param search
	 *        See {@link #getSearch()}
	 * @param order
	 *        See {@link #getOrder()}
	 * @param expectedType
	 *        See {@link #getExpectedType()}
	 * @param resolve
	 *        See {@link #getResolve()}
	 */
	RevisionQuery(BranchParam branchParam, RangeParam rangeParam, List<ParameterDeclaration> searchParams,
			SetExpression search, Order order, Class<E> expectedType, boolean resolve) {
		super(branchParam, rangeParam, searchParams, search);

		this.order = order;
		this._expectedType = expectedType;
		_resolve = resolve;
	}

	/**
	 * The order specification of this query.
	 */
	public Order getOrder() {
		return order;
	}

	/**
	 * @see #getOrder()
	 * 
	 * @return This instance for call chaining.
	 */
	public RevisionQuery<E> setOrder(Order order) {
		this.order = order;
		return chain();
	}

	/**
	 * Whether to resolve business objects from the query result.
	 * 
	 * <p>
	 * An unresolved query evaluates to {@link KnowledgeBase} types, a resolved query to
	 * {@link Wrapper} types.
	 * </p>
	 */
	public boolean getResolve() {
		return _resolve;
	}

	/**
	 * Hint, whether data and identifier information of the result should be retrieved together.
	 * 
	 * <p>
	 * Setting this option is useful, if it is likely that the resulting objects are not yet cached.
	 * This option is a hint and is not required to be followed by the {@link KnowledgeBase} load
	 * strategy.
	 * </p>
	 */
	public LoadStrategy getLoadStrategy() {
		return _loadStrategy;
	}

	/**
	 * Enables {@link LoadStrategy#FULL_LOAD}.
	 */
	public RevisionQuery<E> setFullLoad() {
		return setLoadStrategy(LoadStrategy.FULL_LOAD);
	}

	/**
	 * Enables {@link LoadStrategy#ID_LOAD}.
	 */
	public RevisionQuery<E> setIdPreload() {
		return setLoadStrategy(LoadStrategy.ID_LOAD);
	}

	/**
	 * Setter for {@link #getLoadStrategy()}.
	 * 
	 * @see #setFullLoad()
	 * @see #setIdPreload()
	 */
	public RevisionQuery<E> setLoadStrategy(LoadStrategy value) {
		assert value != null;
		_loadStrategy = value;
		return chain();
	}

	@Override
	public <R, A> R visit(AbstractQueryVisitor<R, A> v, A arg) {
		return v.visitRevisionQuery(this, arg);
	}

	@Override
	protected RevisionQuery<E> chain() {
		return this;
	}

	/**
	 * the type of the results found by this {@link RevisionQuery}.
	 */
	public Class<E> getExpectedType() {
		return _expectedType;
	}

}
