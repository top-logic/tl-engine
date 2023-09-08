/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import com.top_logic.model.export.AccessContext;

/**
 * {@link AccessContext} for a dummy prepare operation that needs no close.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class NoPrepare implements AccessContext {

	/**
	 * Singleton {@link NoPrepare} instance.
	 */
	public static final NoPrepare INSTANCE = new NoPrepare();

	private NoPrepare() {
		// Singleton constructor.
	}

	@Override
	public void close() {
		// Ignore.
	}

}