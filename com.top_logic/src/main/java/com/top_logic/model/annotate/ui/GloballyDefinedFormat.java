/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.ui;

import java.text.Format;
import java.util.Locale;
import java.util.TimeZone;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.format.FormatConfig;
import com.top_logic.basic.format.FormatDefinition;
import com.top_logic.basic.format.FormatFactory;
import com.top_logic.basic.format.configured.Formatter;
import com.top_logic.basic.format.configured.FormatterService;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.values.edit.annotation.OptionLabels;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.annotate.ui.FormatBase.FormatLabel;
import com.top_logic.model.annotate.ui.FormatBase.FormatOptions;

/**
 * {@link FormFactory} using a globally defined format registered in the {@link FormatterService}.
 * 
 * @see com.top_logic.model.annotate.ui.Format
 */
public class GloballyDefinedFormat<C extends GloballyDefinedFormat.Config<?>> extends AbstractConfiguredInstance<C>
		implements FormatFactory {

	private final FormatDefinition<?> _delegate;

	/**
	 * Configuration options for {@link GloballyDefinedFormat}.
	 */
	@TagName("format-ref")
	public interface Config<I extends GloballyDefinedFormat<?>> extends PolymorphicConfiguration<I> {

		/** see {@link #getFormatId()} */
		String FORMAT_ID = "format-id";

		/**
		 * The identifier of the referenced format.
		 * 
		 * @see Formatter#getFormat(String)
		 */
		@Name(FORMAT_ID)
		@Options(fun = FormatOptions.class)
		@OptionLabels(FormatLabel.class)
		String getFormatId();
	}

	/**
	 * Creates a {@link GloballyDefinedFormat} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public GloballyDefinedFormat(InstantiationContext context, C config) {
		super(context, config);

		_delegate = FormatterService.getInstance().getFormatDefinition(config.getFormatId());
	}

	@Override
	public Format newFormat(FormatConfig globalConfig, TimeZone timeZone, Locale locale) {
		return _delegate.newFormat(globalConfig, timeZone, locale);
	}

	@Override
	public String getPattern() {
		return _delegate.getPattern();
	}

}
