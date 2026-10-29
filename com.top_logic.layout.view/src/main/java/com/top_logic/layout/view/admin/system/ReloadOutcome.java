/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.system;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.Reloadable;
import com.top_logic.basic.ReloadableManager.ReloadFailure;

/**
 * What became of reloading some {@link Reloadable}s, as reported by
 * {@link ReloadableFunctions#reload(java.util.Collection)}.
 *
 * @param reloaded
 *        The keys of the {@link Reloadable}s that were reloaded, in the order given.
 * @param failures
 *        For the key of each {@link Reloadable} that could not be reloaded, why, in the order
 *        given.
 */
public record ReloadOutcome(List<String> reloaded, Map<String, ReloadFailure> failures) {

	/**
	 * Whether a {@link Reloadable} could not be reloaded.
	 */
	public boolean failed() {
		return !failures.isEmpty();
	}

}
