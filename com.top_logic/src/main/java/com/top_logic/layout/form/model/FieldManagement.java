/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;

/**
 * Configuration for the field management.
 * 
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface FieldManagement extends ConfigurationItem {
	String COMPLEX_FIELD_DATE_YEAR_MAX = "ComplexField.date.year.max";

	String COMPLEX_FIELD_DATE_YEAR_MIN = "ComplexField.date.year.min";

	@Name(COMPLEX_FIELD_DATE_YEAR_MAX)
	int getMaxYear();

	@Name(COMPLEX_FIELD_DATE_YEAR_MIN)
	int getMinYear();
}
