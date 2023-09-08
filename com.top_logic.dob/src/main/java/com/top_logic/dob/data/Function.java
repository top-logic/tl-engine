/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.data;

import java.util.List;

import com.top_logic.dob.meta.MOFunction;

/**
 * Implementation of a {@link MOFunction} type.
 * 
 * @author    <a href=mailto:till.bentz@top-logic.com>Till Bentz</a>
 */
public interface Function {
	
	public MOFunction getType();

	public Object apply(List<?> arguments);

}
