/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;

/**
 * {@link TLAnnotation} that represents a switch (on/off).
 * 
 * @see StringAnnotation
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface BooleanAnnotation extends TLAnnotation {

	/** @see #getValue() */
	String VALUE = "value";

	/**
	 * Whether this annotation is enabled.
	 */
	@Name(VALUE)
	@BooleanDefault(true)
	boolean getValue();

	/**
	 * @see #getValue()
	 */
	void setValue(boolean value);

}
