/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.linkings;

import com.top_logic.xio.importer.binding.ImportContext;
import com.top_logic.xio.importer.binding.ObjectLinking;

/**
 * {@link ObjectLinking} performing no action at all.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NoLinking implements ObjectLinking {

	/**
	 * Singleton {@link NoLinking} instance.
	 */
	public static final NoLinking INSTANCE = new NoLinking();

	private NoLinking() {
		// Singleton constructor.
	}

	@Override
	public void linkOrElse(ImportContext context, Object scope, Object target, Runnable continuation) {
		// Ignore.
	}

}
