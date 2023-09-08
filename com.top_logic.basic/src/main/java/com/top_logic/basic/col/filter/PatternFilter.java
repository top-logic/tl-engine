/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter;

import java.util.regex.Pattern;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;

/**
 * This filter accepts Strings that are matched by a specified pattern.
 * 
 * Non-Strings will be converted using {@link Object#toString() toString()}.
 * <code>null</code> will never be matched.
 * 
 * @author    <a href="mailto:fsc@top-logic.com">Friedemann Schneider</a>
 */
public class PatternFilter implements Filter<Object> {

	/**
	 * Configuration options for {@link PatternFilter}.
	 */
	@TagName("matches")
	public interface Config<I extends PatternFilter> extends PolymorphicConfiguration<I> {
		/**
		 * The accepted {@link Pattern}.
		 */
		String getPattern();
	}
    
    private Pattern pattern;
    
	/**
	 * Creates a {@link PatternFilter} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public PatternFilter(InstantiationContext context, Config<?> config) {
		this(config.getPattern());
	}

	/**
	 * Creates a {@link PatternFilter}.
	 *
	 * @param anExpression
	 *        The {@link Pattern} to match.
	 */
    public PatternFilter(String anExpression) {
        this.pattern = Pattern.compile(anExpression);
    }

    @Override
	public boolean accept(Object anObject) {
		if (anObject == null) {
			return false;
		}
        return this.pattern.matcher(anObject.toString()).matches();
    }
}