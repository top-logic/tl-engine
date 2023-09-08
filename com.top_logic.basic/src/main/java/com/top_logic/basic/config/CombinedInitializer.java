/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.List;

/**
 * {@link Initializer} that consists of multiple joined {@link Initializer}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
final class CombinedInitializer extends Initializer {

	private final List<? extends Initializer> _initializers;

	/**
	 * Creates a {@link CombinedInitializer}.
	 * 
	 * @param initializers
	 *        Is not allowed to be <code>null</code>. Is not allowed to contain <code>null</code>.
	 *        (Is not checked, causes {@link NullPointerException} when
	 *        {@link #init(AbstractConfigItem)} is called.
	 */
	public CombinedInitializer(List<? extends Initializer> initializers) {
		_initializers = initializers;
	}

	@Override
	public void init(AbstractConfigItem item) {
		for (Initializer initializer : _initializers) {
			initializer.init(item);
		}
	}

}
