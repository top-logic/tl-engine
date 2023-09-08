/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.provider;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;

/**
 * {@link ConfiguredConstantDefaultProvider} returning {@link Integer} value.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp(priority = 100)
@TargetType(value = TLTypeKind.INT, name = "tl.core:Integer")
public class IntegerDefaultProvider extends ConfiguredConstantDefaultProvider {

	/**
	 * Configuration of a {@link IntegerDefaultProvider}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName("int")
	public interface Config extends ConfiguredConstantDefaultProvider.Config<IntegerDefaultProvider> {

		@Override
		Integer getValue();

	}

	/**
	 * Creates a new {@link IntegerDefaultProvider} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link IntegerDefaultProvider}.
	 */
	public IntegerDefaultProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

}

