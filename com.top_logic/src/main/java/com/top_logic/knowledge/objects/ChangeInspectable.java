/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects;


import com.top_logic.knowledge.event.ItemChange;

/**
 * Access to internal state of objects for journal creation.
 * 
 * <p>
 * Note: This interface must only be used internally by the framework.
 * </p>
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public interface ChangeInspectable {

	/**
	 * Revision which the returned {@link ItemChange} has.
	 */
	long NO_REVISION = -1;

	/**
	 * Returns an {@link ItemChange} describing the changes.
	 * 
	 * <p>
	 * The returned {@link ItemChange} has revision {@link #NO_REVISION}.
	 * </p>
	 * 
	 * @return <code>null</code> in case object was not changed.
	 */
	ItemChange getChange();

}
