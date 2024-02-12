/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.diff.op;

/**
 * Pair of items with the same identifiers but potential internal updates.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Update<T> extends DiffOp<T> {
	private final T _left;

	private final T _right;

	private T _rightSuccessor;

	/**
	 * Creates a {@link Update}.
	 */
	public Update(T left, T right) {
		this(left, right, null);
	}

	/**
	 * Creates a {@link Update}.
	 */
	public Update(T left, T right, T rightSuccessor) {
		_left = left;
		_right = right;
		_rightSuccessor = rightSuccessor;
	}

	@Override
	public Kind getKind() {
		return Kind.UPDATE;
	}

	/**
	 * The old item.
	 */
	public T getLeft() {
		return _left;
	}

	/**
	 * The new version of the item.
	 */
	public T getRight() {
		return _right;
	}

	/**
	 * The successor of {@link #getRight()} in the new list.
	 * 
	 * @return May be <code>null</code>, either if {@link #getRight()} is the last element in the
	 *         new item sequence, or if {@link #getRight()} is not contained in an ordered
	 *         collection.
	 */
	public T getRightSuccessor() {
		return _rightSuccessor;
	}

	@Override
	public <R, A> R visit(Visitor<? super T, R, A> v, A arg) {
		return v.visit(this, arg);
	}

}