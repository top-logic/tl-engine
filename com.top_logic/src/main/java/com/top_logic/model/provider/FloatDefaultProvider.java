/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
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
 * {@link ConfiguredConstantDefaultProvider} returning {@link Float} value.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp(priority = 100)
@TargetType(value = TLTypeKind.FLOAT, name = "tl.core:Float")
public class FloatDefaultProvider extends ConfiguredConstantDefaultProvider {

	/**
	 * Configuration of a {@link FloatDefaultProvider}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName("float")
	public interface Config extends ConfiguredConstantDefaultProvider.Config<FloatDefaultProvider> {

		@Override
		Float getValue();

	}

	/**
	 * Creates a new {@link FloatDefaultProvider} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link FloatDefaultProvider}.
	 */
	public FloatDefaultProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

}

