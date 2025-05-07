/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;

/**
 * Common interface for all templates to create a ResKey.
 */
public interface ResKeyTemplate {

	/**
	 * The internal key to retrieve the internationalization.
	 */
	String getKey();

}
