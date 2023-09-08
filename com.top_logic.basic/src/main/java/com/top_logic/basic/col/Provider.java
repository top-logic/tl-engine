/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

/**
 * Generic interface for providers that produce a reference to some other instance.
 * 
 * @param <T> The type of the instance this {@link Provider} provides.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Provider<T> {

	/**
	 * Lazily provides a reference to another instance.
	 */
	T get();
	
}
