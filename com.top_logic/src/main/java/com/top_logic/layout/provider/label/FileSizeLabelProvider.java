/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider.label;

import java.text.Format;
import java.text.NumberFormat;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.LabelProvider;
import com.top_logic.mig.html.HTMLFormatter;

/**
 * {@link LabelProvider}, that converts a file size - long value in byte - into a human readable
 * string, using unit prefixes.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class FileSizeLabelProvider implements LabelProvider {

	/**
	 * Static instance of an {@link FileSizeLabelProvider}, with 0 minimum fraction digits, 2
	 * maximum fraction digits and single space as unit separator.
	 */
	public static final FileSizeLabelProvider INSTANCE = new FileSizeLabelProvider(0, 2, " ");

	private static final String[] units = { "B", "KB", "MB", "GB", "TB", "PB" };

	/**
	 * Configuration of {@link FileSizeLabelProvider}.
	 */
	public interface Config extends PolymorphicConfiguration<FileSizeLabelProvider> {

		/**
		 * Configuration name of property, that holds unit separator string.
		 */
		public static final String UNIT_SEPARATOR_STRING = "unit-separator";

		/**
		 * Configuration name of property, that holds maximum fraction digit count.
		 */
		public static final String MAX_FRACTION_DIGITS = "max-fraction-digits";

		/**
		 * Configuration name of property, that holds minimum fraction digit count.
		 */
		public static final String MIN_FRACTION_DIGITS = "min-fraction-digits";

		/**
		 * separator string between number and unit.
		 */
		@Name(UNIT_SEPARATOR_STRING)
		@StringDefault(" ")
		String getUnitSeparatorString();

		/**
		 * maximum amount of displayed fraction digits.
		 */
		@Name(MAX_FRACTION_DIGITS)
		@IntDefault(2)
		int getMaximumFractionDigits();

		/**
		 * minimum amount of displayed fraction digits.
		 */
		@Name(MIN_FRACTION_DIGITS)
		@IntDefault(0)
		int getMinimumFractionDigits();
	}

	private String _unitSeparator;
	private Format _sizeFormat;

	/**
	 * Creates a {@link FileSizeLabelProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public FileSizeLabelProvider(InstantiationContext context, Config config) {
		this(config.getMinimumFractionDigits(), config.getMaximumFractionDigits(), config.getUnitSeparatorString());
	}

	/**
	 * Create a new {@link FileSizeLabelProvider}.
	 */
	public FileSizeLabelProvider(int minFractionDigits, int maxFractionDigits, String unitSeparator) {
		this(createSizeFormat(minFractionDigits, maxFractionDigits), unitSeparator);
	}

	/**
	 * Create a new {@link FileSizeLabelProvider}.
	 */
	public FileSizeLabelProvider(Format sizeFormat, String unitSeparator) {
		_sizeFormat = sizeFormat;
		_unitSeparator = unitSeparator;
	}

	@Override
	public String getLabel(Object object) {
		if (object == null) {
			return getFormattedString(0, 0);
		}

		double convertedFileSize = (long) object;
		int i = 0;
		while ((convertedFileSize > 1024) && (i < (units.length - 1))) {
			convertedFileSize /= 1024;
			i++;
		}

		return getFormattedString(convertedFileSize, i);
	}

	private String getFormattedString(double convertedFileSize, int unitIndex) {
		StringBuilder result = new StringBuilder();
		if (unitIndex > 0) {
			result.append(_sizeFormat.format(convertedFileSize));
		} else {
			result.append(String.valueOf(Math.round(convertedFileSize)));
		}
		result.append(_unitSeparator);
		result.append(units[unitIndex]);

		return result.toString();
	}

	private static Format createSizeFormat(int minFractionDigits, int maxFractionDigits) {
		NumberFormat sizeFormat = (NumberFormat) HTMLFormatter.getInstance().getNumberFormat().clone();
		sizeFormat.setMinimumFractionDigits(minFractionDigits);
		sizeFormat.setMaximumFractionDigits(maxFractionDigits);
		return sizeFormat;
	}
}
