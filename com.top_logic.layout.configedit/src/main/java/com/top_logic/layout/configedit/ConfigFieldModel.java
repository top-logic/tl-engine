/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.Objects;

import com.top_logic.basic.config.ConfigurationChange;
import com.top_logic.basic.config.ConfigurationChange.Kind;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationListener;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.values.edit.Labels;

/**
 * A {@link AbstractFieldModel} that binds to a single {@link PropertyDescriptor} on a
 * {@link ConfigurationItem}.
 *
 * <p>
 * Reads and writes go through the configuration API ({@link ConfigurationItem#value(PropertyDescriptor)} /
 * {@link ConfigurationItem#update(PropertyDescriptor, Object)}). External changes to the
 * configuration are propagated to {@link com.top_logic.layout.form.model.FieldModelListener}s via
 * a {@link ConfigurationListener}.
 * </p>
 *
 * <p>
 * A {@link Number} arriving through {@link #setValue(Object)} is coerced to the property's exact
 * numeric type first (a control such as a number input hands back a plain {@link Double},
 * whatever the property's own type is). A value that would lose information in that conversion
 * (a fractional value for an integral property) is rejected as an {@link #getInputError() input
 * error} instead of being truncated silently.
 * </p>
 */
public class ConfigFieldModel extends AbstractFieldModel implements ConfigurationListener {

	private final ConfigurationItem _config;

	private final PropertyDescriptor _property;

	/**
	 * Creates a {@link ConfigFieldModel}.
	 *
	 * @param config
	 *        The configuration item to bind to.
	 * @param property
	 *        The property of the configuration item.
	 */
	public ConfigFieldModel(ConfigurationItem config, PropertyDescriptor property) {
		super(config.value(property));
		_config = config;
		_property = property;
		setMandatory(property.isMandatory());
		setNullable(property.isNullable());
		config.addConfigurationListener(property, this);
	}

	@Override
	public Object getValue() {
		return _config.value(_property);
	}

	@Override
	public void setValue(Object value) {
		if (value instanceof Number number) {
			Object coerced = coerceNumber(number, _property.getType());
			if (coerced == REJECTED_NUMBER) {
				setError(I18NConstants.ERROR_INVALID_VALUE__VALUE_PROPERTY.fill(value,
					Labels.propertyLabel(_property, false)));
				return;
			}
			value = coerced;
		}

		// Clear a previously rejected value's error before the redundant-write guard below can
		// return early: re-entering the value the property already holds is itself well-formed
		// input and must not leave a stale error on display next to a now-valid value.
		setError(null);

		Object oldValue = getValue();
		if (Objects.equals(oldValue, value)) {
			return;
		}
		_config.update(_property, value);
		// The ConfigurationListener callback (onChange) fires the FieldModelListener notification.
	}

	/**
	 * Sentinel returned by {@link #coerceNumber(Number, Class)} for a value that cannot be
	 * converted to the target type without losing information.
	 */
	private static final Object REJECTED_NUMBER = new Object();

	/**
	 * Converts the given number to the given numeric property type, or answers
	 * {@link #REJECTED_NUMBER} if that conversion would lose information (e.g. a fractional value
	 * for an integral type).
	 *
	 * <p>
	 * A control such as a number input hands back a plain {@link Double} regardless of the
	 * property's own numeric type ({@link com.top_logic.layout.react.control.form.ReactNumberInputControl#parseClientValue(Object)}
	 * only knows how many decimal places to display, not the target type), so the value must be
	 * coerced here, where the exact {@link PropertyDescriptor#getType() property type} is known.
	 * </p>
	 *
	 * <p>
	 * Only the numeric types the configuration framework natively supports as a {@code PLAIN}
	 * property are handled ({@code byte}, {@code short}, {@code int}, {@code long}, {@code float},
	 * {@code double}, and their wrapper classes - see {@code BuiltInFormats.VALUE_PROVIDER_BY_TYPE}
	 * in {@code com.top_logic.basic.config.format}). {@code BigInteger}/{@code BigDecimal} have no
	 * built-in value provider there and are not handled. A number for any other type is passed
	 * through unchanged, leaving the existing type check in
	 * {@code PropertyDescriptorImpl#checkExpectedType} to reject it as before.
	 * </p>
	 */
	private static Object coerceNumber(Number value, Class<?> type) {
		Class<?> wrapper = wrapperType(type);
		if (wrapper == value.getClass()) {
			// Already the expected type.
			return value;
		}
		if (wrapper == Float.class) {
			return Float.valueOf(value.floatValue());
		}
		if (wrapper == Double.class) {
			return Double.valueOf(value.doubleValue());
		}
		if (wrapper == Integer.class || wrapper == Long.class || wrapper == Short.class || wrapper == Byte.class) {
			double asDouble = value.doubleValue();
			if (asDouble != Math.rint(asDouble) || Double.isInfinite(asDouble)) {
				// A fractional value would be truncated: reject rather than silently lose the
				// digits the user typed.
				return REJECTED_NUMBER;
			}
			if (wrapper == Integer.class) {
				return Integer.valueOf(value.intValue());
			}
			if (wrapper == Long.class) {
				return Long.valueOf(value.longValue());
			}
			if (wrapper == Short.class) {
				return Short.valueOf(value.shortValue());
			}
			return Byte.valueOf(value.byteValue());
		}
		// Not a numeric property type this model coerces (e.g. BigInteger/BigDecimal, which the
		// configuration framework does not support as a PLAIN property type in the first place).
		return value;
	}

	/**
	 * The wrapper class for the given type, or the type itself if it already is not a primitive.
	 */
	private static Class<?> wrapperType(Class<?> type) {
		if (type == int.class) {
			return Integer.class;
		}
		if (type == long.class) {
			return Long.class;
		}
		if (type == short.class) {
			return Short.class;
		}
		if (type == byte.class) {
			return Byte.class;
		}
		if (type == float.class) {
			return Float.class;
		}
		if (type == double.class) {
			return Double.class;
		}
		return type;
	}

	@Override
	public void onChange(ConfigurationChange change) {
		if (change.getKind() == Kind.SET) {
			fireValueChanged(change.getOldValue(), change.getNewValue());
		}
	}

	/**
	 * The property this model is bound to.
	 */
	public PropertyDescriptor getProperty() {
		return _property;
	}

	/**
	 * The configuration item this model is bound to.
	 */
	public ConfigurationItem getConfig() {
		return _config;
	}

	/**
	 * Detaches this model from the configuration by removing the listener.
	 */
	public void detach() {
		_config.removeConfigurationListener(_property, this);
	}
}
