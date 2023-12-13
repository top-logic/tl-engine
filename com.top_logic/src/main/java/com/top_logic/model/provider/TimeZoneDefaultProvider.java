/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.provider;

import java.util.TimeZone;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TimeZoneValueProvider;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.time.ConfiguredTimeZones;
import com.top_logic.basic.time.TimeZones;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;

/**
 * {@link DefaultProvider} delivering a configured time zone from {@link TimeZones}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp(priority = 100)
@TargetType(value = TLTypeKind.CUSTOM, name = "tl.util:TimeZone")
public class TimeZoneDefaultProvider extends ConfiguredConstantDefaultProvider {

	/**
	 * Configuration of a {@link TimeZoneDefaultProvider}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName("time-zone")
	public interface Config extends ConfiguredConstantDefaultProvider.Config<BooleanDefaultProvider> {

		@Override
		@Format(TimeZoneValueProvider.class)
		@Options(fun = ConfiguredTimeZones.class)
		TimeZone getValue();

	}

	/**
	 * Create a {@link TimeZoneDefaultProvider}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public TimeZoneDefaultProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

}