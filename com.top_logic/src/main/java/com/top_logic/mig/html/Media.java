/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import com.top_logic.layout.DisplayContext;

/**
 * Medium to which data are delivered.
 * 
 * @see DisplayContext#getOutputMedia()
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public enum Media {

	/** Output is delivered to a browser. */
	BROWSER,

	/** Output is delivered to a PDF document. */
	PDF,

	;

}

