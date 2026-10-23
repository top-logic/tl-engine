/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.module.category;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.module.ServiceCategory;

/**
 * Services storing and loading the application data, such as the database connection and the knowledge base.
 */
@Label("Persistence")
@ServiceCategory.Order(3)
public interface PersistenceCategory extends ServiceCategory {

	// Pure marker interface.

}
