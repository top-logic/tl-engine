/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.text.DateFormat;
import java.text.Format;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.format.FormatFactory;
import com.top_logic.basic.format.configured.FormatterService;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactDatePickerControl;

/**
 * {@link ConfigControlProvider} that creates a {@link ReactDatePickerControl} of a fixed
 * {@link com.top_logic.layout.react.control.form.ReactDatePickerControl.Kind}, showing the value in
 * the format of its {@link Config configuration}.
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
 *
 * <p>
 * The format decides the precision the field edits: a format that writes seconds edits seconds,
 * one that does not edits minutes. A value provider that stores no seconds is therefore mapped
 * with a format without seconds, so that the field never accepts a value the configuration cannot
 * write.
 * </p>
 *
 * @implNote The format is created for each control from {@link FormatFactory#newFormat}, in the
 *           time zone and locale of the current user. A format definition that creates no
 *           {@link DateFormat} is logged as an error, and the control shows the value in the
 *           {@link com.top_logic.layout.react.control.form.ReactDatePickerControl.Kind#displayFormat()
 *           default format of its kind} instead, the same as without a format. The precision is
 *           derived by {@link ReactDatePickerControl#precision}.
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

		/** Property name of {@link #getKind()}. */
		String KIND = "kind";

		/** Property name of {@link #getFormat()}. */
		String FORMAT = "format";

		/**
		 * Which part of a point in time the claimed property's value represents.
		 */
		@Name(KIND)
		@Mandatory
		ReactDatePickerControl.Kind getKind();

		/**
		 * The format the value is shown in, e.g. a short time format.
		 *
		 * <p>
		 * The format also decides whether the field edits seconds: it does if the format writes
		 * them. Without a format, the value is shown in the default format of the kind.
		 * </p>
		 */
		@Name(FORMAT)
		@DefaultContainer
		PolymorphicConfiguration<? extends FormatFactory> getFormat();

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
		return new ReactDatePickerControl(context, model, getConfig().getKind(), displayFormat());
	}

	/**
	 * The configured format in the current user's time zone and locale, or {@code null} for the
	 * default format of the kind.
	 */
	private DateFormat displayFormat() {
		FormatFactory factory = TypedConfigUtil.createInstance(getConfig().getFormat());
		if (factory == null) {
			return null;
		}
		Format format = factory.newFormat(FormatterService.getInstance().getConfig(), ThreadContext.getTimeZone(),
			ThreadContext.getLocale());
		if (format instanceof DateFormat dateFormat) {
			return dateFormat;
		}
		Logger.error("Format definition at '" + getConfig().location() + "' does not write dates: " + format,
			DatePickerFormatProvider.class);
		return null;
	}

}
