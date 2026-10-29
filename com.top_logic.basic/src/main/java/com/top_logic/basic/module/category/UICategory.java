/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.module.category;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.module.ServiceCategory;

/**
 * Services building and serving the user interface, such as layouts, views, themes and icons.
 */
@Label("User interface")
@ServiceCategory.Order(8)
public interface UICategory extends ServiceCategory {

	// Pure marker interface.

}
