/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.module.category;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.module.ServiceCategory;

/**
 * Services running the application unattended and watching it, such as scheduled tasks, logging and maintenance.
 */
@Label("Operations & monitoring")
@ServiceCategory.Order(10)
public interface OperationsCategory extends ServiceCategory {

	// Pure marker interface.

}
