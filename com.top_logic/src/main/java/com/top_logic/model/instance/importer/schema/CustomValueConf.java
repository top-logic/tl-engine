/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.instance.importer.schema;

import com.top_logic.basic.config.annotation.Abstract;

/**
 * Base configuration for model primitive types that require a complex representation in an XML
 * export.
 */
@Abstract
public interface CustomValueConf extends ValueConf {
	// Marker interface.
}
