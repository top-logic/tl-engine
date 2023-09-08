/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.annotation.Format;


/**
 * Width or height specification.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Format(DisplayDimension.ValueProvider.class)
public final class DisplayDimension {

	/**
	 * Size of zero pixel.
	 */
	public static final DisplayDimension ZERO = new DisplayDimension(0, DisplayUnit.PIXEL);
	
	/**
	 * Size of 100%.
	 */
	public static final DisplayDimension HUNDERED_PERCENT = new DisplayDimension(100, DisplayUnit.PERCENT);
	
	/**
	 * Size of 50%.
	 */
	public static final DisplayDimension FIFTY_PERCENT = new DisplayDimension(50, DisplayUnit.PERCENT);

	/**
	 * Size of 0%.
	 * 
	 * <p>
	 * The rendering effect can differ from {@link #ZERO}, if used e.g. for table cells.
	 * </p>
	 */
	public static final DisplayDimension ZERO_PERCENT = dim(0, DisplayUnit.PERCENT);

	private final float value;
	private final DisplayUnit unit;

	/**
	 * Creates a {@link DisplayDimension}.
	 *
	 * @param value
	 *        See {@link #getValue()}.
	 * @param unit
	 *        See {@link #getUnit()}.
	 * 
	 * @see #dim(float, DisplayUnit) Public access
	 */
	private DisplayDimension(float value, DisplayUnit unit) {
		switch (unit) {
		case PERCENT:
			if (value > 100 || value < 0) {
				throw new IllegalArgumentException("Unit '" + unit + "' needs a size leq 100 and geq 0, was " + value + ".");
			}
			break;

		case PIXEL:
			if (value < 0) {
				throw new IllegalArgumentException("Unit '" + unit + "' needs a size geq 0, was " + value + ".");
			}
			break;
		}
		
		this.value = value;
		this.unit = unit;
	}

	/**
	 * The value of this {@link DisplayDimension} without {@link #getUnit()}.
	 */
	public float getValue() {
		return value;
	}

	/**
	 * The unit of this {@link DisplayDimension}.
	 */
	public DisplayUnit getUnit() {
		return unit;
	}

	/**
	 * Parse a dimension string.
	 * 
	 * @param dimensionString the result of {@link #toString()}
	 * @return A {@link DisplayDimension} that  
	 */
	public static DisplayDimension parseDimension(String dimensionString) throws IllegalArgumentException {
		float value;
		DisplayUnit unit;
		
		if (dimensionString == null) {
			throw new IllegalArgumentException("Dimension definition must not be 'null'.");
		}

		if (dimensionString.endsWith(DisplayUnit.PERCENT.getExternalName())) {
			unit = DisplayUnit.PERCENT;
		} else if (dimensionString.endsWith(DisplayUnit.PIXEL.getExternalName())) {
			unit = DisplayUnit.PIXEL;
		} else {
			// Assume no unit at all. If there is an unknown unit given, parsing
			// fails below.
			unit = null;
		}
		
		try {
			String valueString;
			if (unit == null) {
				valueString = dimensionString;
				
				// Pixel is default.
				unit = DisplayUnit.PIXEL;
			} else {
				valueString = dimensionString.substring(0, dimensionString.length() - unit.getExternalName().length());
			}
			
			value = Float.parseFloat(valueString);
		} catch (NumberFormatException nfex) {
			throw new IllegalArgumentException("Dimension can not be parsed: " + dimensionString);
		}
		
		return dim(value, unit);
	}
	
	@Override
	public String toString() {
		int intValue = (int) value;
		if (value == intValue) {
			// Prevent fractional digits.
			return intValue + unit.getExternalName();
		} else {
			return value + unit.getExternalName();
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		
		if (! (obj instanceof DisplayDimension)) {
			return false;
		}
		
		return equalsDimension(this, (DisplayDimension) obj); 
	}

	protected static boolean equalsDimension(DisplayDimension d1, DisplayDimension d2) {
		return d1.value == d2.value && d1.unit == d2.unit;
	}

	@Override
	public int hashCode() {
		return Float.floatToIntBits(value) + unit.hashCode();
	}

	/**
	 * Creates a {@link DisplayDimension} in {@link DisplayUnit#PIXEL}.
	 */
	public static DisplayDimension px(float value) {
		return dim(value, DisplayUnit.PIXEL);
	}

	/**
	 * Creates a {@link DisplayDimension} in {@link DisplayUnit#PERCENT}.
	 */
	public static DisplayDimension percent(float value) {
		return dim(value, DisplayUnit.PERCENT);
	}
	
	/**
	 * Creates a {@link DisplayDimension} with the given values.
	 * 
	 * @param value
	 *        See {@link #getValue()}.
	 * @param unit
	 *        See {@link #getUnit()}.
	 * @return The {@link DisplayDimension} with the given values.
	 */
	public static DisplayDimension dim(float value, DisplayUnit unit) {
		// Reuse constants.
		if (value == 100.0f && unit == DisplayUnit.PERCENT) {
			return HUNDERED_PERCENT;
		}
		if (value == 0.0f && unit == DisplayUnit.PIXEL) {
			return ZERO;
		}
		return new DisplayDimension(value, unit);
	}

	/**
	 * {@link ConfigurationValueProvider} for {@link DisplayDimension}s.
	 */
	public static class ValueProvider extends AbstractConfigurationValueProvider<DisplayDimension> {
	
		/**
		 * Singleton {@link DisplayDimension.ValueProvider} instance.
		 */
		public static final DisplayDimension.ValueProvider INSTANCE = new DisplayDimension.ValueProvider();

		/**
		 * Creates a {@link ValueProvider}.
		 */
		private ValueProvider() {
			super(DisplayDimension.class);
		}
	
		@Override
		protected DisplayDimension getValueNonEmpty(String propertyName, CharSequence propertyValue)
				throws ConfigurationException {
			try {
				return parseDimension(propertyValue.toString());
			} catch (IllegalArgumentException ex) {
				throw new ConfigurationException("Invalid dimension format: " + propertyValue, ex);
			}
		}
	
		@Override
		protected String getSpecificationNonNull(DisplayDimension configValue) {
			return configValue.toString();
		}

	}
	
}
