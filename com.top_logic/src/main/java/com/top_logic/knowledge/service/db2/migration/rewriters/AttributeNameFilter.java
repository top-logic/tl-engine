/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.rewriters;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.knowledge.event.ItemChange;

/**
 * {@link AttributeRewriter} that drops attributes matching a certain regular expression.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AttributeNameFilter extends ConfiguredItemChangeRewriter<AttributeNameFilter.Config> {

	/**
	 * Configuration of an {@link AttributeNameFilter}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<AttributeNameFilter> {

		/** Name of {@link #getExcludePattern()}. */
		String EXCLUDE_PATTERN = "exclude-pattern";

		/**
		 * Pattern matching the attributes to remove.
		 */
		@Name(EXCLUDE_PATTERN)
		@Mandatory
		String getExcludePattern();

		/**
		 * Setter for {@link #getExcludePattern()}.
		 */
		void setExcludePattern(String value);

	}

	private final Pattern _pattern;

	/**
	 * Creates a {@link AttributeNameFilter} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AttributeNameFilter(InstantiationContext context, Config config) {
		super(context, config);

		_pattern = Pattern.compile(config.getExcludePattern());
	}

	@Override
	public void rewriteItemChange(ItemChange event) {
		for (Iterator<? extends Entry<String, ?>> it = event.getValues().entrySet().iterator(); it.hasNext();) {
			Entry<String, ?> entry = it.next();
			if (_pattern.matcher(entry.getKey()).matches()) {
				it.remove();
			}
		}
	}

}
