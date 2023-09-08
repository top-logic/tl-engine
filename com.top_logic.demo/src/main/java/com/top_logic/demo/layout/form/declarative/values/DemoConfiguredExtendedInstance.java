/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.declarative.values;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;

/**
 * {@link DemoDefaultConfiguredInstance} with an extended configuration option.
 * 
 * @see Config#getExtended()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DemoConfiguredExtendedInstance extends DemoDefaultConfiguredInstance {
	
	/**
	 * Configuration options of {@link DemoConfiguredExtendedInstance}.
	 */
	public interface Config extends DemoPolymorphicConfig, AbstractDemoExtendedConfig {

		// Pure sum interface.

	}

	/**
	 * Creates a {@link DemoConfiguredExtendedInstance} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DemoConfiguredExtendedInstance(InstantiationContext context, Config config) {
		super(context, config);
	}

}
