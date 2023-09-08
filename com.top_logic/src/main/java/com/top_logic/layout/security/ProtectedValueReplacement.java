/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.security;

import com.top_logic.basic.col.ConstantMapping;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.filter.EqualsFilter;


/**
 * Singleton class. The value is used as replacement for all {@link ProtectedValue} for which the
 * user has not enough rights.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class ProtectedValueReplacement {

	/** Singleton {@link ProtectedValueReplacement} instance. */
	public static final ProtectedValueReplacement INSTANCE = new ProtectedValueReplacement();
	
	private static final Mapping<Object, ProtectedValueReplacement> MAPPING =
		new ConstantMapping<>(INSTANCE);

	private static final Filter<Object> FILTER = new EqualsFilter(INSTANCE);

	private ProtectedValueReplacement() {
		// singleton instance
	}

	/** 
	 * A {@link Mapping} that maps anything to {@link #INSTANCE}.
	 */
	public static Mapping<Object, ProtectedValueReplacement> getMapping() {
		return MAPPING;
	}

	/** 
	 * A {@link Filter} that only accepts {@link #INSTANCE}.
	 */
	public static Filter<Object> getFilter() {
		return FILTER;
	}

}

