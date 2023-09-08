/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob;


/**
 * A {@link MOPart} is a part of the {@link MetaObject} hierarchy, e.g.
 * {@link MetaObject} itself or {@link MOAttribute}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface MOPart {

	/**
	 * The name of this type.
	 */
	public String getName();

	/**
	 * After a call to {@link #freeze()}, it is guaranteed that this part does
	 * not change any longer.
	 * 
	 * <p>
	 * Any setter method called on a {@link #isFrozen() frozen} part throws an
	 * {@link IllegalStateException}.  
	 * </p>
	 */
	public void freeze();

	/**
	 * Whether this type can no longer be modified.
	 * 
	 * <p>
	 * Clients and implementations of a part can optimize access and cache
	 * results, after it was frozen. Setters called on a
	 * {@link #isFrozen() frozen} type throw an {@link IllegalStateException}.
	 * </p>
	 * 
	 * @return Whether the access to setter methods of this part is denied.
	 */
	public boolean isFrozen();

}
