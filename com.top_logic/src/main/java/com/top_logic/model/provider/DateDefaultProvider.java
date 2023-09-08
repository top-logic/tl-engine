/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.provider;

import java.util.Date;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;

/**
 * {@link ConfiguredConstantDefaultProvider} returning {@link Date} value.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp(priority = 100)
@TargetType(TLTypeKind.DATE)
public class DateDefaultProvider extends ConfiguredConstantDefaultProvider {

	/**
	 * Configuration of a {@link DateDefaultProvider}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName("date")
	public interface Config extends ConfiguredConstantDefaultProvider.Config<DateDefaultProvider> {

		@Override
		Date getValue();

	}

	/**
	 * Creates a new {@link DateDefaultProvider} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link DateDefaultProvider}.
	 */
	public DateDefaultProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

}

