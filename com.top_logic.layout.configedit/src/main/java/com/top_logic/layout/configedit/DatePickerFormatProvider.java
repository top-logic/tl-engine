/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactDatePickerControl;

/**
 * {@link ConfigControlProvider} that creates a {@link ReactDatePickerControl} of a fixed
 * {@link com.top_logic.layout.react.control.form.ReactDatePickerControl.Kind}.
 *
 * <p>
 * The {@code impl} for a {@link ConfigControlService.FormatMapping} entry that claims a property
 * whose value provider narrows a {@code Date} to a specific part of a point in time, e.g.
 * {@link com.top_logic.basic.time.TimeOfDayAsDateValueProvider} to
 * {@link com.top_logic.layout.react.control.form.ReactDatePickerControl.Kind#TIME}. Registered per
 * value-provider class (see {@link ConfigControlService.Config#getFormats()}), not per Java type: a
 * formatted {@code Date} reaches this control through that mapping, ahead of the generic format
 * text field the built-in fallback would otherwise give it - the fallback cannot tell a time of day
 * apart from a plain date, both being {@code Date}, but the value provider's own class can.
 * </p>
 */
public class DatePickerFormatProvider extends AbstractConfiguredInstance<DatePickerFormatProvider.Config>
		implements ConfigControlProvider {

	/**
	 * Configuration options for {@link DatePickerFormatProvider}.
	 *
	 * <p>
	 * No {@code @ClassDefault} is needed: the type parameter is bound directly to
	 * {@link DatePickerFormatProvider} (there is no further specialization of this configuration),
	 * so {@link #getImplementationClass()}'s default already follows from that bound - see
	 * {@link PolymorphicConfiguration}'s own {@code @implNote}.
	 * </p>
	 */
	public interface Config extends PolymorphicConfiguration<DatePickerFormatProvider> {

		/**
		 * Which part of a point in time the claimed property's value represents.
		 */
		@Mandatory
		ReactDatePickerControl.Kind getKind();

	}

	/**
	 * Creates a {@link DatePickerFormatProvider}.
	 */
	@CalledByReflection
	public DatePickerFormatProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public ReactControl createControl(ReactContext context, ConfigFieldModel model) {
		return new ReactDatePickerControl(context, model, getConfig().getKind());
	}

}
