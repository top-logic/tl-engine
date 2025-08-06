/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.copy;

import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;

/**
 * Operation for performing a (configurable) deep copy of a model object graph.
 * 
 * <p>
 * By default, all composition references are copied alongside the copied object, but the depth and
 * types of the copy is configurable, see {@link #setFilter(CopyFilter)},
 * {@link #setConstructor(CopyConstructor)}, and {@link #setTransient(Boolean)}.
 * </p>
 */
public abstract class CopyOperation {

	/**
	 * Creates a nested copy operation that operates on the same object references as the given
	 * outer operation.
	 */
	public static CopyOperation nested(CopyOperation outerOperation) {
		return new NestedOperation(outerOperation);
	}

	/**
	 * Creates an initial (top-level) copy operation to start a fresh copy.
	 */
	public static CopyOperation initial() {
		return new InitialOperation();
	}

	/**
	 * Sets the context (outer object referencing the copied root object).
	 */
	public abstract TLObject setContext(TLObject object, TLReference contextRef);

	/**
	 * Sets a filter that decides which parts of the input object graph should be copied.
	 */
	public abstract CopyOperation setFilter(CopyFilter filter);

	/**
	 * Sets a constructor function that is responsible for creating new instances in the copied
	 * graph.
	 */
	public abstract CopyOperation setConstructor(CopyConstructor constructor);

	/**
	 * Whether to allocate transient objects by default (if no explicit constructor function is
	 * given).
	 */
	public abstract CopyOperation setTransient(Boolean transientCopy);

	/**
	 * Final step of the copy that ensures that all inner fields of all
	 * {@link #copyReference(TLObject) copied} objects and nested compositions are filled.
	 */
	public abstract void finish();

	/**
	 * Copies the given object (without any inner properties).
	 * 
	 * <p>
	 * To ensure that all inner fields are filled, ensure that {@link #finish()} is called
	 * afterwards.
	 * </p>
	 */
	public abstract Object copyReference(TLObject orig);

	/**
	 * Resolves the copy of an original that has potentially copied before.
	 */
	protected abstract TLObject resolveCopy(TLObject orig);

	/**
	 * Enters a copy to be resolved later on.
	 * 
	 * @see #resolveCopy(TLObject)
	 */
	protected abstract void enterCopy(TLObject orig, TLObject copy);

}