/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.module;

import java.io.IOException;

/**
 * The stored configuration of a service in the state the running instance of the service was
 * started with.
 *
 * <p>
 * A baseline is recorded before the stored configuration of a service is changed for the first time
 * since the service was started, see
 * {@link BasicRuntimeModule#recordConfigurationBaseline(java.util.function.Supplier)}. It allows
 * telling whether the stored configuration still differs from the configuration in use and
 * restoring the configuration in use, discarding the changes.
 * </p>
 *
 * @see BasicRuntimeModule#isConfigurationChanged()
 */
public interface ConfigurationBaseline {

	/**
	 * Whether the stored configuration differs from the state captured by this baseline.
	 */
	boolean isChanged();

	/**
	 * Restores the stored configuration to the state captured by this baseline.
	 *
	 * @throws IOException
	 *         When the stored configuration cannot be written.
	 */
	void restore() throws IOException;

}
