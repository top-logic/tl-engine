/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.diff.op;

/**
 * {@link DiffOp} creating a new item in an ordered collection.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Create<T> extends DiffOp<T> {

	private final T _before;

	private final T _item;

	/**
	 * Creates a {@link Create}.
	 *
	 */
	public Create(T before, T item) {
		_before = before;
		_item = item;
	}

	@Override
	public Kind getKind() {
		return Kind.CREATE;
	}

	/**
	 * The new item.
	 * 
	 * @see #getBefore()
	 */
	public T getItem() {
		return _item;
	}

	/**
	 * The identifier of the item before which to insert {@link #getItem()}.
	 */
	public T getBefore() {
		return _before;
	}

	@Override
	public <R, A> R visit(Visitor<? super T, R, A> v, A arg) {
		return v.visit(this, arg);
	}

}
