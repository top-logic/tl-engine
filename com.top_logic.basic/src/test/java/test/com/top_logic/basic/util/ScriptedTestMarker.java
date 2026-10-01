/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.util;

import junit.framework.Test;

/**
 * Marker for a {@link Test} that runs a script against a started application.
 *
 * <p>
 * The test harness groups such tests into {@link ScriptedTestUnit}s and distributes these units
 * among the shards of a {@link ShardSelection}.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ScriptedTestMarker extends Test {

	// Pure marker interface.

}
