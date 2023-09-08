/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.basic.col.CloseableIterator;


/**
 * {@link CloseableIterator} additionally implementing {@link BufferProvider}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
interface BufferingCloseableIterator<T> extends CloseableIterator<T>, BufferProvider<T> {

	// Pure sum interface.

}
