/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.provider;

import java.awt.Color;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.form.control.ColorControlProvider;
import com.top_logic.layout.form.format.ColorConfigFormat;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;

/**
 * {@link ConfiguredConstantDefaultProvider} configuring a default {@link Color}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp(priority = 100)
@TargetType(value = TLTypeKind.CUSTOM, name = "tl.util:Color")
public class ColorDefaultProvider extends ConfiguredConstantDefaultProvider {

	/**
	 * Configuration of a {@link ColorDefaultProvider}.
	 */
	@TagName("color")
	public interface Config extends ConfiguredConstantDefaultProvider.Config<ColorDefaultProvider> {

		@Override
		@Format(ColorConfigFormat.class)
		@ControlProvider(ColorControlProvider.class)
		Color getValue();

	}

	/**
	 * Creates a new {@link ColorDefaultProvider} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link ColorDefaultProvider}.
	 */
	public ColorDefaultProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

}
