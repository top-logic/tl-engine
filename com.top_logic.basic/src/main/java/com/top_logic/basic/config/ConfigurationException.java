/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import static com.top_logic.basic.util.ResKey.*;

import com.top_logic.basic.exception.I18NException;
import com.top_logic.basic.util.ResKey;

/**
 * Exception that announces a fatal configuration problem. 
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConfigurationException extends I18NException {

	private final String _propertyName;

	private final String _propertyValue;

	/**
	 * Creates a {@link ConfigurationException}.
	 * 
	 * @param errorKey
	 *        See {@link #getErrorKey()}.
	 * 
	 * @deprecated Use {@link #ConfigurationException(ResKey, String, CharSequence)}.
	 */
	@Deprecated
	public ConfigurationException(ResKey errorKey) {
		this(errorKey, null, null);
	}

	/**
	 * Creates a {@link ConfigurationException}.
	 * 
	 * @param errorKey
	 *        See {@link #getErrorKey()}.
	 * @param propertyName
	 *        See {@link #getPropertyName()}.
	 * @param propertyValue
	 *        See {@link #getPropertyValue()}.
	 */
	public ConfigurationException(ResKey errorKey, String propertyName, CharSequence propertyValue) {
		super(errorKey);

		_propertyName = propertyName;
		_propertyValue = toStringNullSafe(propertyValue);
	}

	/**
	 * Creates a {@link ConfigurationException}.
	 * 
	 * @deprecated Use {@link #ConfigurationException(ResKey, String, CharSequence, Throwable)}
	 */
	@Deprecated
	public ConfigurationException(ResKey errorKey, Throwable cause) {
		this(errorKey, null, null, cause);
	}

	/**
	 * Creates a {@link ConfigurationException}.
	 * 
	 * @param errorKey
	 *        See {@link #getErrorKey()}.
	 * @param propertyName
	 *        See {@link #getPropertyName()}.
	 * @param propertyValue
	 *        See {@link #getPropertyValue()}.
	 * @param cause
	 *        See {@link #getCause()}.
	 */
	public ConfigurationException(ResKey errorKey, String propertyName, CharSequence propertyValue, Throwable cause) {
		super(errorKey, cause);

		_propertyName = propertyName;
		_propertyValue = toStringNullSafe(propertyValue);
	}

	private static String toStringNullSafe(CharSequence propertyValue) {
		if (propertyValue == null) {
			return null;
		}
		return propertyValue.toString();
	}

	/**
	 * Creates a {@link ConfigurationException}.
	 * 
	 * @deprecated Use {@link #ConfigurationException(ResKey, String, CharSequence)}
	 */
	@Deprecated
	public ConfigurationException(String message) {
		this(text(message));
	}

	/**
	 * Creates a {@link ConfigurationException}.
	 * 
	 * @deprecated Use {@link #ConfigurationException(ResKey, Throwable)}
	 */
	@Deprecated
	public ConfigurationException(String message, Throwable cause) {
		this(text(message), cause);
	}

	/**
	 * Configuration problem that is due to another nested exception that cannot be handled.
	 * 
	 * @deprecated Use {@link #ConfigurationException(ResKey, String, CharSequence, Throwable)}
	 */
	@Deprecated
	public ConfigurationException(String message, String propertyName, CharSequence propertyValue, Throwable ex) {
		this(text(message), propertyName, propertyValue, ex);
	}

	/**
	 * Configuration problem that arises from a configuration inconsistency.
	 * 
	 * @deprecated Use {@link #ConfigurationException(ResKey, String, CharSequence)}
	 */
	@Deprecated
	public ConfigurationException(String message, String propertyName, CharSequence propertyValue) {
		this(text(message), propertyName, propertyValue);
	}

	/**
	 * The name of the property that could not be configured.
	 */
	public String getPropertyName() {
		return _propertyName;
	}

	/**
	 * The value configuration that is illegal. 
	 */
	public String getPropertyValue() {
		return _propertyValue;
	}

	@Override
	protected ResKey getMessageKey() {
		ResKey message = super.getMessageKey();
		if (_propertyName != null) {
			if (_propertyValue != null) {
				return I18NConstants.ERROR_INVALID_CONFIGURATION_VALUE__PROPERTY_VALUE_DETAIL.fill(
					_propertyName, _propertyValue, message);
			} else {
				return I18NConstants.ERROR_INVALID_CONFIGURATION_VALUE__PROPERTY_DETAIL.fill(
					_propertyName, message);
			}
		} else {
			return message;
		}
	}

}
