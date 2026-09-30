/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.operation;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * The operational environment an application installation runs in.
 *
 * <p>
 * This is the <em>environment</em> axis of an application's operation mode: it distinguishes a
 * developer workstation from an automated test install from a live production install. It is
 * intentionally boot-time configuration and does not change while the application is running.
 * </p>
 *
 * <p>
 * Runtime maintenance is a <em>separate</em>, orthogonal axis (a production install can be in a
 * maintenance window) and is therefore not modelled as a member here. Consumers fold the two axes
 * together via
 * {@link com.top_logic.base.administration.MaintenanceWindowManager#isMaintenanceActive() a separate
 * maintenance flag}.
 * </p>
 *
 * @see OperationModeService
 *
 * @author <a href="mailto:jonathan.huesing@top-logic.com">Jonathan Hüsing</a>
 */
public enum OperationMode implements ExternallyNamed {

	/**
	 * A developer workstation or IDE run.
	 */
	DEVELOPMENT("development"),

	/**
	 * An automated or integration test install.
	 */
	TEST("test"),

	/**
	 * A live production install.
	 */
	PRODUCTION("production");

	private final String _externalName;

	private OperationMode(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}
}
