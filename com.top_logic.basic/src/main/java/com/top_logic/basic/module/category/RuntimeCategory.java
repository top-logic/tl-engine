/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.module.category;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.module.ServiceCategory;

/**
 * Services the application runtime and its configuration are built on, such as the application configuration, threads and caches.
 */
@Label("Runtime & configuration")
@ServiceCategory.Order(1)
public interface RuntimeCategory extends ServiceCategory {

	// Pure marker interface.

}
