/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.server.script;

/**
 * Algorithm converting a script value to a value usable in a Java implementation.
 */
public interface ValueConverter {
	/**
	 * Converts a value computed by TL-Script to a reasonable value to use in a Java implementation.
	 */
	Object fromScript(Object scriptValue);
}
