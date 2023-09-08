/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.format;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.TimeZone;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.NullDefault;

/**
 * {@link PatternBasedFormatDefinition} creating {@link DecimalFormat}s.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DecimalFormatDefinition extends PatternBasedFormatDefinition<DecimalFormatDefinition> {

	/**
	 * Configuration interface for {@link DecimalFormatDefinition}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PatternBasedFormatDefinition.Config<DecimalFormatDefinition> {

		/**
		 * Tag name to identify {@link DecimalFormatDefinition} in a polymorphic list.
		 */
		String TAG_NAME = "decimal";

		/**
		 * The {@link RoundingMode} to use.
		 * 
		 * <p>
		 * The default value is {@link RoundingMode#HALF_EVEN} (the Java built-in value).
		 * </p>
		 */
		@Nullable
		@NullDefault
		RoundingMode getRoundingMode();
		
		/**
		 * Whether the parsed value should be rounded to the number of fractional digits given in
		 * the format definition.
		 */
		boolean getNormalize();

		/**
		 * The result type the format should produced after parsing.
		 */
		NumberFormatResult getResultType();

	}

	private final RoundingMode _roundingMode;

	private boolean _normalize;

	private NumberFormatResult _resultType;

	/**
	 * Creates a new {@link DecimalFormatDefinition} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link DecimalFormatDefinition}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public DecimalFormatDefinition(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);

		_roundingMode = config.getRoundingMode();
		_normalize = config.getNormalize();
		_resultType = config.getResultType();
	}

	@Override
	protected Config config() {
		return (Config) super.config();
	}

	@Override
	public Format newFormat(FormatConfig config, TimeZone timeZone, Locale locale) {
		NumberFormat format = new DecimalFormat(getPattern(), DecimalFormatSymbols.getInstance(locale));
		format.setRoundingMode(_roundingMode == null ? config.getRoundingMode() : _roundingMode);
		if (_normalize) {
			format = NormalizingFormat.newInstance(format);
		}
		return _resultType.adapt(format);
	}

}

