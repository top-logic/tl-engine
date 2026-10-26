/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.module.category;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.module.ServiceCategory;

/**
 * Services exchanging data with other systems, such as mail, web services and file imports and exports.
 */
@Label("Interfaces")
@ServiceCategory.Order(9)
public interface InterfacesCategory extends ServiceCategory {

	// Pure marker interface.

}
