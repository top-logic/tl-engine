/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.declarative.values;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;

/**
 * Concrete {@link DemoConfiguredInstance}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DemoDefaultConfiguredInstance extends DemoConfiguredInstance {

	/**
	 * Creates a {@link DemoDefaultConfiguredInstance} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DemoDefaultConfiguredInstance(InstantiationContext context, DemoPolymorphicConfig config) {
		super(context, config);
	}

	/**
	 * The value from the property {@link DemoPolymorphicConfig#getFirst()}.
	 */
	public String getFirst() {
		return config().getFirst();
	}

	/**
	 * The value from the property {@link DemoPolymorphicConfig#getFirst()}.
	 */
	public String getSecond() {
		return config().getSecond();
	}

	private DemoPolymorphicConfig config() {
		return (DemoPolymorphicConfig) getConfig();
	}

}
