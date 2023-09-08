/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.provider;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.TagName;

/**
 * {@link DefaultProvider} that always returns <code>null</code>.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class NullDefaultProvider extends ConstantDefaultProvider {

	/** Singleton {@link NullDefaultProvider} instance. */
	public static final NullDefaultProvider INSTANCE =
		new NullDefaultProvider(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			TypedConfiguration.newConfigItem(NullDefaultProvider.Config.class));

	/**
	 * Configuration of the {@link NullDefaultProvider}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName("null")
	public interface Config extends PolymorphicConfiguration<NullDefaultProvider> {
		// Just for annotation
	}

	/**
	 * Creates a new {@link NullDefaultProvider} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link NullDefaultProvider}.
	 * 
	 */
	public NullDefaultProvider(InstantiationContext context, Config config) {
		super(null);
	}

}

