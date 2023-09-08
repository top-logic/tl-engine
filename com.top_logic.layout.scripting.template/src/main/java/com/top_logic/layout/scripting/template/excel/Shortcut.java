/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.excel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Mark up interface for a short cut cell value (which is no "key:value" pair but only a "value").
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Shortcut {
	// Markup interface
}

