/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.diff.config;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.model.TLClass;

/**
 * {@link Update} of a {@link TLClass}.
 */
@Abstract
public interface ClassUpdate extends Update {

	/**
	 * The {@link TLClass} whose properties are changed.
	 */
	@Mandatory
	String getType();

	/** @see #getType() */
	void setType(String value);

}
