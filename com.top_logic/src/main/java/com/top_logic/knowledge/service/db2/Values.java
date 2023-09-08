/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

/**
 * {@link ValidityChain} that holds data for the represented life range.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface Values extends ValidityChain<Values> {

	/**
	 * The actual data of this {@link ValidityChain}. Must not be modified.
	 */
	Object[] getData();

	/**
	 * Whether the data are alive in the represented range. If <code>false</code> the data must not
	 * be accessed.
	 */
	boolean isAlive();

}

