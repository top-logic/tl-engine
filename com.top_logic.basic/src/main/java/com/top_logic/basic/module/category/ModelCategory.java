/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.module.category;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.module.ServiceCategory;

/**
 * Services providing the application model, its types and their derived values.
 */
@Label("Model")
@ServiceCategory.Order(2)
public interface ModelCategory extends ServiceCategory {

	// Pure marker interface.

}
