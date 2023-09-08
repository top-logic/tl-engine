/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;

/**
 * A {@link Filter} for accepting object of a {@link Config#getType() given Java type}.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class InstanceFilter implements Filter<Object> {

	/**
	 * Configuration options for {@link InstanceFilter}.
	 */
	@TagName("instance-of")
	public interface Config extends PolymorphicConfiguration<InstanceFilter> {
		/**
		 * The Java type of accepted objects.
		 */
		@Mandatory
		Class<?> getType();
	}

	private final Class<?> _type;

	/**
	 * Creates a {@link InstanceFilter} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public InstanceFilter(InstantiationContext context, Config config) {
		this(config.getType());
	}

	/** Creates a {@link InstanceFilter} for the given {@link Class}. */
	public InstanceFilter(Class<?> type) {
		_type = type;
	}

	@Override
	public boolean accept(Object anObject) {
		return _type.isInstance(anObject);
	}

}
