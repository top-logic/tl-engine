/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.module.category;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.module.ServiceCategory;

/**
 * Services translating texts and formatting and parsing values for the languages and countries of the users.
 */
@Label("I18N & formats")
@ServiceCategory.Order(7)
public interface I18NCategory extends ServiceCategory {

	// Pure marker interface.

}
