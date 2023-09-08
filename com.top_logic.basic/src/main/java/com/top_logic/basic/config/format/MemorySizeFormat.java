/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.format;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.BidiMap;

import com.top_logic.basic.col.BidiHashMap;
import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;

/**
 * {@link ConfigurationValueProvider} for memory size specifications like "128m", "64k", "10g".
 * 
 * <p>
 * Supported qualifiers are "k" for kilobytes, "m" for megabytes, "g" gigabytes, "t" terrabytes, and
 * "p" petabytes. No qualifier means "bytes".
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MemorySizeFormat extends AbstractConfigurationValueProvider<Long> {

	private static final Pattern PATTERN =
		Pattern.compile("([1-9][0-9]*|0)\\s*(k|m|g|t|p|)", Pattern.CASE_INSENSITIVE);
	
	private static final BidiMap<String, Long> FACTOR = new BidiHashMap<>(
		new MapBuilder<String, Long>()
			.put("", 1L)
			.put("k", 1024L)
			.put("m", 1024L * 1024L)
			.put("g", 1024L * 1024L * 1024L)
			.put("t", 1024L * 1024L * 1024L * 1024L)
			.put("p", 1024L * 1024L * 1024L * 1024L * 1024L)
			.toMap());

	/**
	 * Singleton {@link MemorySizeFormat} instance.
	 */
	public static final MemorySizeFormat INSTANCE = new MemorySizeFormat();

	private MemorySizeFormat() {
		super(long.class);
	}

	@Override
	protected Long getValueNonEmpty(String propertyName, CharSequence propertyValue)
			throws ConfigurationException {
		Matcher matcher = PATTERN.matcher(propertyValue);
		if (matcher.matches()) {
			long number = Long.parseLong(matcher.group(1));
			return number * FACTOR.get(matcher.group(2).toLowerCase());
		} else {
			throw new ConfigurationException("Invalid size specification: '" + propertyValue
				+ "'. Expecting a value matching the pattern: " + PATTERN);
		}
	}

	@Override
	protected String getSpecificationNonNull(Long configValue) {
		long factor = 1;
		long value = configValue.longValue();
		int max = FACTOR.size();
		while ((--max > 0) && (value % 1024L == 0)) {
			factor *= 1024;
			value /= 1024;
		}
		if (value == 0) {
			return "0";
		}
		return value + FACTOR.getKey(Long.valueOf(factor));
	}
	
	@Override
	public Long defaultValue() {
		return Long.valueOf(0L);
	}
}