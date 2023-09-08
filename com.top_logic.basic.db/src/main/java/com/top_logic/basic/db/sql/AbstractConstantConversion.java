/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import java.util.Map;

import com.top_logic.basic.col.Mapping;


/**
 * Hybrid of {@link Conversion} that ignores the requested revision and {@link Mapping} that
 * actually does the conversion.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractConstantConversion<D> implements Conversion, Mapping<Object, D> {

	@Override
	public Object convert(Object argument, Map<String, Integer> argumentIndexByName, Object[] arguments) {
		return map(argument);
	}

}

