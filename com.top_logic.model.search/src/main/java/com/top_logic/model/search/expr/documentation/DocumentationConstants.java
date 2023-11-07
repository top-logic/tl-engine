/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.documentation;

import com.top_logic.mig.html.HTMLConstants;

/**
 * Common constants to create documentation for TL-Script functions.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface DocumentationConstants {

	/** CSS class for the documentation of a TL script function. */
	String DOCUMENTATION_CSS_CLASS = "tlScriptDoc";

	/**
	 * CSS class for a {@link HTMLConstants#TABLE table} that describes the parameters of a
	 * function.
	 */
	String PARAMETER_TABLE_CSS_CLASS = "tlDocTable";

}
