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
 * {@link ConfiguredConstantDefaultProvider} returning {@link Byte} value.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp(priority = 100)
@TargetType(value = TLTypeKind.INT, name = "tl.core:Byte")
public class ByteDefaultProvider extends ConfiguredConstantDefaultProvider {

	/**
	 * Configuration of a {@link ByteDefaultProvider}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName("byte")
	public interface Config extends ConfiguredConstantDefaultProvider.Config<ByteDefaultProvider> {

		@Override
		Byte getValue();

	}

	/**
	 * Creates a new {@link ByteDefaultProvider} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link ByteDefaultProvider}.
	 */
	public ByteDefaultProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

}

