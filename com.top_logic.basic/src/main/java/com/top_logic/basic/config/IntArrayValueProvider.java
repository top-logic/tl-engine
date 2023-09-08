/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;


/**
 * {@link AbstractConfigurationValueProvider} for serializing an array of {@link Integer}s.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class IntArrayValueProvider extends AbstractConfigurationValueProvider<int[]> {

	/**
	 * The default instance of the {@link #IntArrayValueProvider()}.
	 * <p>
	 * The radix is {@value #DEFAULT_RADIX}. <br/>
	 * The separator is '{@value #DEFAULT_SEPARATOR}'.
	 * </p>
	 */
	public static final IntArrayValueProvider INSTANCE = new IntArrayValueProvider();

	/**
	 * The minimum value for the radix.
	 * 
	 * @see Character#MIN_RADIX
	 * @see #getRadix()
	 */
	public static final int MIN_RADIX = Character.MIN_RADIX;

	/**
	 * The maximum value for the radix.
	 * 
	 * @see Character#MAX_RADIX
	 * @see #getRadix()
	 */
	public static final int MAX_RADIX = Character.MAX_RADIX;

	/**
	 * If the radix is not given in the constructor, it has this value.
	 * 
	 * @see #getRadix()
	 */
	public static final int DEFAULT_RADIX = 10;

	/**
	 * If the separator is not given in the constructor, it has this value.
	 * 
	 * @see #getSeparator()
	 */
	public static final char DEFAULT_SEPARATOR = ',';

	private final int _radix;

	private final char _separatorChar;

	private final String _separatorString;

	/**
	 * Create an {@link IntArrayValueProvider}.
	 * <p>
	 * The radix is {@value #DEFAULT_RADIX}. <br/>
	 * The separator is '{@value #DEFAULT_SEPARATOR}'.
	 * </p>
	 */
	protected IntArrayValueProvider() {
		this(DEFAULT_RADIX, DEFAULT_SEPARATOR);
	}

	/**
	 * Create an {@link IntArrayValueProvider}.
	 * <p>
	 * The radix is {@value #DEFAULT_RADIX}.
	 * </p>
	 * 
	 * @param separator
	 *        The separator between the numbers. The values are automatically trimmed after
	 *        splitting. It this value is not given, it defaults to '{@value #DEFAULT_SEPARATOR}'.
	 *        See: {@link #getSeparator()}
	 */
	protected IntArrayValueProvider(char separator) {
		this(DEFAULT_RADIX, separator);
	}

	/**
	 * Create an {@link IntArrayValueProvider}.
	 * <p>
	 * The separator is '{@value #DEFAULT_SEPARATOR}'.
	 * </p>
	 * 
	 * @param radix
	 *        The radix or base in which the numbers should be formatted to and parsed from. It has
	 *        to be in the range: {@value #MIN_RADIX} <= radix <= {@value #MAX_RADIX}. If this value
	 *        is not given, it defaults to {@value #DEFAULT_RADIX}. See: {@link #getRadix()}
	 */
	protected IntArrayValueProvider(int radix) {
		this(radix, DEFAULT_SEPARATOR);
	}

	/**
	 * Create an {@link IntArrayValueProvider}.
	 * 
	 * @param radix
	 *        The radix or base in which the numbers should be formatted to and parsed from. It has
	 *        to be in the range: {@value #MIN_RADIX} <= radix <= {@value #MAX_RADIX}. If this value
	 *        is not given, it defaults to {@value #DEFAULT_RADIX}. See: {@link #getRadix()}
	 * @param separator
	 *        The separator between the numbers. The values are automatically trimmed after
	 *        splitting. It this value is not given, it defaults to '{@value #DEFAULT_SEPARATOR}'.
	 *        See: {@link #getSeparator()}
	 */
	protected IntArrayValueProvider(int radix, char separator) {
		super(int[].class);
		if (radix < MIN_RADIX) {
			throw new IllegalArgumentException("Radix is " + radix + " but has to be at least " + MIN_RADIX + ".");
		}
		if (radix > MAX_RADIX) {
			throw new IllegalArgumentException("Radix is " + radix + " but has to be at most " + MAX_RADIX + ".");
		}
		_radix = radix;
		_separatorChar = separator;
		_separatorString = String.valueOf(separator);
	}

	@Override
	protected int[] getValueEmpty(String propertyName) throws ConfigurationException {
		return defaultValue();
	}

	@Override
	public int[] getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		String[] values = propertyValue.toString().split(_separatorString);
		int[] result = new int[values.length];
		int index = 0;
		for (String value : values) {
			result[index] = Integer.parseInt(value.trim(), _radix);
			index += 1;
		}
		return result;
	}

	@Override
	public String getSpecificationNonNull(int[] numbers) {
		StringBuilder result = new StringBuilder();
		boolean addSeparator = false;
		for (int number : numbers) {
			if (addSeparator) {
				result.append(_separatorChar);
			} else {
				addSeparator = true;
			}
			result.append(Integer.toString(number, _radix));
		}
		return result.toString();
	}

	@Override
	public int[] defaultValue() {
		return new int[0];
	}

	@Override
	public boolean isLegalValue(Object value) {
		return value instanceof int[];
	}

	@Override
	public Object normalize(Object value) {
		if (value == null) {
			return defaultValue();
		}
		return value;
	}

	/**
	 * The radix or base in which the numbers should be formatted to and parsed from.
	 * <p>
	 * It is in the range: {@value #MIN_RADIX} <= radix <= {@value #MAX_RADIX}. The default value is
	 * {@value #DEFAULT_RADIX}.
	 * </p>
	 */
	public int getRadix() {
		return _radix;
	}

	/**
	 * The separator between the numbers.
	 * <p>
	 * The default value is ' {@value #DEFAULT_SEPARATOR}'. <br/>
	 * The values are automatically trimmed after splitting.
	 * </p>
	 */
	public char getSeparator() {
		return _separatorChar;
	}

}
