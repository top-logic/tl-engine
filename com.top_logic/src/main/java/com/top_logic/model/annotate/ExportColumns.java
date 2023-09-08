/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import java.util.List;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.TagName;

/**
 * Configuration of the export columns for the annotated element.
 * 
 * <p>
 * When an attribute is displayed as table this annotation specifies which columns are exported.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName("export-columns")
public interface ExportColumns extends TLAttributeAnnotation {

	/**
	 * List of table columns to export.
	 */
	@Format(CommaSeparatedStrings.class)
	List<String> getValue();

}

