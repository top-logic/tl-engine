/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.diff.op;

/**
 * {@link DiffOp} describing the deletion of an item.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Delete<T> extends DiffOp<T> {

	private final T _delted;

	/**
	 * Creates a {@link Delete}.
	 *
	 */
	public Delete(T delted) {
		_delted = delted;
	}

	@Override
	public Kind getKind() {
		return Kind.DELETE;
	}

	/**
	 * The identifier of the deleted item.
	 */
	public T getDelted() {
		return _delted;
	}

	@Override
	public <R, A> R visit(Visitor<? super T, R, A> v, A arg) {
		return v.visit(this, arg);
	}
}
