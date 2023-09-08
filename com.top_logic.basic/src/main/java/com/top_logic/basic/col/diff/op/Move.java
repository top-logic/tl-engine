/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.diff.op;

/**
 * {@link DiffOp} describing the move of an item.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Move<T> extends DiffOp<T> {

	private final T _before;

	private T _movedLeft;

	private T _movedRight;


	/**
	 * Creates a {@link Move}.
	 */
	public Move(T before, T movedLeft, T movedRight) {
		_before = before;
		_movedLeft = movedLeft;
		_movedRight = movedRight;
	}

	@Override
	public Kind getKind() {
		return Kind.MOVE;
	}

	/**
	 * The moved item as found in the original list.
	 */
	public T getMovedLeft() {
		return _movedLeft;
	}

	/**
	 * The moved item as inserted into the target list.
	 */
	public T getMovedRight() {
		return _movedRight;
	}

	/**
	 * The key of the insertion point before which to insert the {@link #getMovedRight()}.
	 */
	public T getBefore() {
		return _before;
	}

	@Override
	public <R, A> R visit(Visitor<? super T, R, A> v, A arg) {
		return v.visit(this, arg);
	}
}
