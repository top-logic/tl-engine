/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;

/**
 * {@link Filter} accepting {@link Object}s whose {@link Object#toString() string-representation}
 * start with a {@link Config#getPrefix() given prefix}.
 * 
 * @author <a href="mailto:fsc@top-logic.com">Friedemann Schneider</a>
 */
public class StartsWithFilter implements Filter<Object> {

	/**
	 * Configuration options for {@link StartsWithFilter}.
	 */
	@TagName("starts-with")
	public interface Config<I extends StartsWithFilter> extends PolymorphicConfiguration<I> {
		/**
		 * The accepted prefix.
		 */
		String getPrefix();

		/**
		 * Whether case should be ignored.
		 */
		boolean getIgnoreCase();
	}
    
	private final String _prefix;

	private final boolean _ignoreCase;
    
	/**
	 * Creates a {@link StartsWithFilter} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public StartsWithFilter(InstantiationContext context, Config<?> config) {
		this(config.getPrefix(), config.getIgnoreCase());
	}
    
	/**
	 * Creates a {@link StartsWithFilter}.
	 *
	 * @param prefix
	 *        The prefix to match.
	 */
	public StartsWithFilter(String prefix) {
		this(prefix, false);
    }
    
	/**
	 * Creates a {@link StartsWithFilter}.
	 *
	 * @param prefix
	 *        The prefix to match.
	 * @param ignoreCase
	 *        Whether to ignore case.
	 */
	public StartsWithFilter(String prefix, boolean ignoreCase) {
		_ignoreCase = ignoreCase;
		_prefix = prefix;
    }
    
    @Override
	public boolean accept(Object anObject) {
		if (anObject == null) {
			return false;
		}
		String value = anObject.toString();
		if (_ignoreCase) {
			int prefixLength = _prefix.length();
			if (value.length() < prefixLength) {
				return false;
			} else {
				return value.substring(0, prefixLength).equalsIgnoreCase(_prefix);
			}
		} else {
			return value.startsWith(_prefix);
        }
    }
}