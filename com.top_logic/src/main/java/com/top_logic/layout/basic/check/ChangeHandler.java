/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.check;

/**
 * Component that may contain transient changes.
 * 
 * @see #isChanged()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ChangeHandler extends CanApply {

	/**
	 * Whether this component has unsaved changes.
	 * 
	 * @see #getApplyClosure()
	 * @see #getDiscardClosure()
	 * @see #getChangeDescription()
	 */
	boolean isChanged();

	/**
	 * Whether this component has an error that would cause the {@link #getApplyClosure()} to fail.
	 * 
	 * @see #getApplyClosure()
	 */
	boolean hasError();

	/**
	 * An optional description of the changes in this component.
	 *
	 * @return A user-visible text describing the change, or <code>null</code> if no description is
	 *         available.
	 */
	String getChangeDescription();

}
