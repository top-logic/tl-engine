/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.Iterator;
import java.util.List;

/**
 * {@link Iterator} that allows fetching all entries at once.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
interface BufferProvider<T> extends Iterator<T> {

	/**
	 * Optimized implementation of iterating throgh {@link #next()} and adding the result to a
	 * buffer.
	 */
	List<T> getAll();

}
