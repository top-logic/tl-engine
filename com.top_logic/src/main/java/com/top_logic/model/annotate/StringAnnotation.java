/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;

/**
 * {@link TLAnnotation} representing a {@link String} value.
 * 
 * @see BooleanAnnotation
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Abstract
public interface StringAnnotation extends TLAnnotation {
	
	/** @see #getValue() */
	String VALUE = "value";

	/**
	 * Configured value.
	 */
	@Name(VALUE)
	@Mandatory
	String getValue();

	/**
	 * @see #getValue()
	 */
	void setValue(String value);

}

