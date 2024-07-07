/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.model.block;

/**
 * Visual concatenation of several linked {@link Block}s.
 * 
 * @see Block#getNext()
 * @see Block#getPrevious()
 * @see #getFirst()
 * @see #getLast()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface BlockCollection extends BlockGroup {

	/**
	 * Whether this concatenation contains no {@link Block}s.
	 */
	default boolean isEmpty() {
		return getFirst() == null;
	}

	/**
	 * The first {@link Block} in this concatenation.
	 */
	Block getFirst();

	/**
	 * The last {@link Block} in this concatenation.
	 */
	Block getLast();

	/**
	 * This concatenation viewed as {@link Iterable} of {@link Block}s.
	 */
	Iterable<Block> contents();

	/**
	 * Appends the given {@link Block} to the end of this concatenation.
	 */
	default void append(Block newBlock) {
		insertBefore(null, newBlock);
	}

	/**
	 * Insets the given {@link Block} to this concatenation before the given reference
	 * {@link Block}.
	 */
	void insertBefore(Block before, Block newBlock);

	/**
	 * Removes all {@link Block}s from this concatenation and returns a reference to the first of
	 * the linked {@link Block}s.
	 */
	default Block removeAll() {
		return removeFrom(getFirst());
	}

	/**
	 * Removes all {@link Block}s starting from the given one from this concatenation and returns a
	 * reference to the first of the removed {@link Block}s.
	 */
	Block removeFrom(Block start);

}
