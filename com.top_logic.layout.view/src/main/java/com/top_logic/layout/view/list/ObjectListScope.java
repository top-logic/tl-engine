/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.list;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.view.channel.ChannelInputs;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.util.error.TopLogicException;

/**
 * Runtime scope of an {@link ObjectListElement}, reachable from within its templates via
 * {@link com.top_logic.layout.view.ViewContext#getScope(Class)}.
 *
 * <p>
 * Bundles the list's input channels with the configured link / remove functions, so that
 * {@link LinkElementAction} and {@link RemoveElementAction} placed anywhere in an item or
 * new-element template operate on the enclosing list without further configuration.
 * </p>
 */
public class ObjectListScope {

	private final List<ViewChannel> _inputs;

	private final QueryExecutor _link;

	private final QueryExecutor _remove;

	private Runnable _newElementReset;

	/**
	 * Creates an {@link ObjectListScope}.
	 *
	 * @param inputs
	 *        The channels whose values lead the arguments of the link / remove functions, in
	 *        declaration order.
	 * @param link
	 *        The compiled link function {@code ...inputs -> element -> ...}, or {@code null} when
	 *        the list has no link function configured.
	 * @param remove
	 *        The compiled remove function {@code ...inputs -> element -> ...}, or {@code null} when
	 *        the list has no remove function configured.
	 */
	public ObjectListScope(List<ViewChannel> inputs, QueryExecutor link, QueryExecutor remove) {
		_inputs = inputs;
		_link = link;
		_remove = remove;
	}

	/**
	 * Installs the callback that re-initializes the new-element channel with a fresh transient
	 * element.
	 *
	 * <p>
	 * Called by {@link ObjectListItems} once the new-element channel exists. Runs after a successful
	 * {@link #linkElement(Object)}, so the new-element template is ready for the next entry.
	 * </p>
	 *
	 * @param reset
	 *        The reset callback, or {@code null} when the list has no new-element template.
	 */
	void initNewElementReset(Runnable reset) {
		_newElementReset = reset;
	}

	/**
	 * Links the given element to the list's inputs by running the configured link function in a
	 * transaction.
	 *
	 * @param element
	 *        The element to link, typically the new-element template's transient object.
	 * @return The result of the link function.
	 *
	 * @throws TopLogicException
	 *         if an input of the list holds no value, so that there is no place the element could be
	 *         attached to.
	 *
	 * @see #resetNewElement()
	 */
	public Object linkElement(Object element) {
		if (_link == null) {
			throw new TopLogicException(I18NConstants.ERROR_NO_LINK_FUNCTION);
		}
		Object[] arguments = arguments(element);
		if (!InputValues.complete(Arrays.asList(arguments).subList(0, _inputs.size()))) {
			throw new TopLogicException(I18NConstants.ERROR_MISSING_INPUT_VALUE);
		}
		return inTransaction(() -> _link.execute(arguments));
	}

	/**
	 * Re-initializes the new-element channel with a fresh transient element, so the new-element
	 * template is ready for the next entry.
	 */
	public void resetNewElement() {
		if (_newElementReset != null) {
			_newElementReset.run();
		}
	}

	/**
	 * Unlinks the given element from the list's inputs by running the configured remove function in
	 * a transaction.
	 *
	 * @param element
	 *        The element to remove.
	 * @return The result of the remove function.
	 */
	public Object removeElement(Object element) {
		if (_remove == null) {
			throw new TopLogicException(I18NConstants.ERROR_NO_REMOVE_FUNCTION);
		}
		Object[] arguments = arguments(element);
		return inTransaction(() -> _remove.execute(arguments));
	}

	/**
	 * The arguments a function of the list is applied to: the current values of the inputs, in
	 * declaration order, and the element behind them.
	 */
	private Object[] arguments(Object element) {
		return InputValues.alive(ChannelInputs.arguments(_inputs, element));
	}

	private static Object inTransaction(Supplier<Object> operation) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		Transaction tx = kb.beginTransaction();
		try {
			Object result = operation.get();
			tx.commit();
			return result;
		} finally {
			tx.rollback();
		}
	}

}
