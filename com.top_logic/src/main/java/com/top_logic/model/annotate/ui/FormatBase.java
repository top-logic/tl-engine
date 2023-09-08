/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.ui;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.format.configured.Formatter;

/**
 * Definition of display formats.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public interface FormatBase extends ConfigurationItem {

	/** see {@link #getFormat()} */
	String FORMAT_PROPERTY = "format";

	/** see {@link #getFormatReference()} */
	String FORMAT_REFERENCE_PROPERTY = "format-ref";

	/**
	 * Display values of this attribute according to this literal format.
	 */
	@Name(FORMAT_PROPERTY)
	String getFormat();

	/**
	 * Display values of this attribute according to this referenced format.
	 * 
	 * @see Formatter#getFormat(String)
	 */
	@Name(FORMAT_REFERENCE_PROPERTY)
	String getFormatReference();

}