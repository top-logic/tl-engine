/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration;

/**
 * Migration operation that is executed in a fully started application after the first boot of the
 * new software version.
 */
public interface StartupAction {

	/**
	 * Operation that is invoked, after the system has bootet the new software version the first
	 * time.
	 */
	void perform();

}
