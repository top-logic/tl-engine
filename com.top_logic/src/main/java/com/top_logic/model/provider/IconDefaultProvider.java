/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.provider;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;

/**
 * {@link ConfiguredConstantDefaultProvider} returning an {@link ThemeImage} value.
 */
@InApp(priority = 100)
@TargetType(value = TLTypeKind.CUSTOM, name = "tl.core:Icon")
public class IconDefaultProvider extends ConfiguredConstantDefaultProvider {

	/**
	 * Configuration of a {@link IconDefaultProvider}.
	 */
	@TagName("icon")
	public interface Config extends ConfiguredConstantDefaultProvider.Config<IconDefaultProvider> {

		@Override
		ThemeImage getValue();

	}

	/**
	 * Creates a new {@link IconDefaultProvider} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link IconDefaultProvider}.
	 */
	public IconDefaultProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

}

