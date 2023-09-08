/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Iterator;

/**
 * Java 5 forward compatible interface that summarizes everything that can be
 * iterated over in <code>foreach</code> loops.
 * 
 * @deprecated use the original now
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
public interface Iterable5<T> extends Iterable<T> {

	/**
	 * The {@link Iterator} over this collection.
	 */
	@Override
	Iterator<T> iterator();
	
}
