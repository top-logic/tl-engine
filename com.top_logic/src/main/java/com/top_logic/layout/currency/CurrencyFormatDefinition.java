/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.currency;

import java.text.DecimalFormat;
import java.util.Currency;
import java.util.Locale;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.format.DecimalFormatDefinition;
import com.top_logic.basic.format.FormatConfig;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * Definition of a decimal format that displays an additional {@link Currency}.
 */
@InApp
@Label("Currency format")
public class CurrencyFormatDefinition extends DecimalFormatDefinition<CurrencyFormatDefinition.Config> {

	/**
	 * Configuration interface for {@link CurrencyFormatDefinition}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends DecimalFormatDefinition.Config<CurrencyFormatDefinition> {

		/**
		 * Tag name to identify {@link CurrencyFormatDefinition}.
		 */
		String TAG_NAME = "currency";

		/**
		 * The currency to be displayed.
		 */
		@com.top_logic.basic.config.annotation.Format(CurrencyConfigFormat.class)
		@Options(fun = CurrencyOptions.class)
		@Mandatory
		Currency getCurrency();

	}

	private Currency _currency;

	/**
	 * Creates a new {@link CurrencyFormatDefinition} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link CurrencyFormatDefinition}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public CurrencyFormatDefinition(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);

		_currency = config.getCurrency();
	}

	@Override
	protected DecimalFormat createDisplayFormat(FormatConfig globalConfig, Locale locale) {
		DecimalFormat displayFormat = super.createDisplayFormat(globalConfig, locale);
		displayFormat.setCurrency(_currency);
		return displayFormat;
	}

}

