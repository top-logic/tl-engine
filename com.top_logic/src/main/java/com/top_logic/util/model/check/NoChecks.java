/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.model.check;

import com.top_logic.basic.col.Sink;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLObject;

/**
 * {@link InstanceCheck} that never reports a problem.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class NoChecks implements InstanceCheck {

	/**
	 * Singleton {@link NoChecks} instance.
	 */
	public static final NoChecks INSTANCE = new NoChecks();

	private NoChecks() {
		// Singleton constructor.
	}

	@Override
	public void check(Sink<ResKey> problems, TLObject object) {
		// No checks.
	}
}