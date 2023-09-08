/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Iterator;

import com.top_logic.basic.CollectionUtil;

/**
 * {@link Iterator} that must be {@link #close() closed} after usage.
 * 
 * <p>
 * Note: You may use {@link CollectionUtil#toIterable(Iterator)} for using in a
 * foreach loop.
 * </p>
 * 
 * <p>
 * Note: This interface is preferably implemented through {@link AbstractCloseableIterator}.
 * </p>
 * 
 * @see AbstractCloseableIterator
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface CloseableIterator<T> extends Iterator<T>, AutoCloseable {

	@Override
	void close();

}
