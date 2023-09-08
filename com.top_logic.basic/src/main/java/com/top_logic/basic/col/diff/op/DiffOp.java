/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.diff.op;

/**
 * Description of a change to an ordered collection.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class DiffOp<T> {

	/**
	 * Kind of supported operations.
	 */
	public enum Kind {
		/**
		 * Creation of a new item.
		 * 
		 * @see Create
		 */
		CREATE,

		/**
		 * The item is left as is with a potential internal update.
		 */
		UPDATE,

		/**
		 * Deletion of an old item.
		 * 
		 * @see Delete
		 */
		DELETE,

		/**
		 * Moving an old item to a new position.
		 * 
		 * @see Move
		 */
		MOVE;
	}

	/**
	 * Visitor method for the {@link DiffOp} hierarchy.
	 */
	public interface Visitor<T, R, A> {
		/** Vist case for {@link Create}. */
		R visit(Create<? extends T> op, A arg);

		/** Vist case for {@link Update}. */
		R visit(Update<? extends T> op, A arg);

		/** Vist case for {@link Delete}. */
		R visit(Delete<? extends T> op, A arg);

		/** Vist case for {@link Move}. */
		R visit(Move<? extends T> op, A arg);
	}

	/**
	 * The {@link Kind} of the operation.
	 */
	public abstract Kind getKind();

	/**
	 * Visit method for {@link DiffOp}s.
	 */
	public abstract <R, A> R visit(Visitor<? super T, R, A> v, A arg);

}
