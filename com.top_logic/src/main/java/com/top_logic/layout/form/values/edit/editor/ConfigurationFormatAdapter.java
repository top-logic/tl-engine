/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.editor;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.HashSet;
import java.util.Set;

import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.exception.I18NFailure;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.DescriptiveParsePosition;
import com.top_logic.util.Resources;

/**
 * {@link Format} adapter to a {@link ConfigurationValueProvider} usable in a {@link ComplexField}
 * for editing configuration values.
 * 
 * <p>
 * This {@link Format} is able to report internationalized error messages, if the context provides a
 * {@link DescriptiveParsePosition}, see {@link #parseObject(String, ParsePosition)}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ConfigurationFormatAdapter extends Format {

	private final ConfigurationValueProvider<Object> _provider;

	/**
	 * Creates a {@link ConfigurationFormatAdapter}.
	 * @param provider
	 *        See {@link #getProvider()}.
	 */
	public ConfigurationFormatAdapter(ConfigurationValueProvider<Object> provider) {
		_provider = provider;
	}

	/**
	 * The {@link ConfigurationValueProvider} being adapted.
	 */
	public ConfigurationValueProvider<Object> getProvider() {
		return _provider;
	}

	@Override
	public StringBuffer format(Object obj, StringBuffer buffer, FieldPosition pos) {
		buffer.append(_provider.getSpecification(obj));
		return buffer;
	}

	@Override
	public Object parseObject(String source, ParsePosition pos) {
		try {
			Object result = _provider.getValue(null,
				source.subSequence(pos.getIndex(), source.length()));
			// Mark everything read.
			pos.setIndex(source.length());
			return result;
		} catch (Exception ex) {
			// Unfortunately, the concrete error cannot be communicated.
			pos.setErrorIndex(pos.getIndex());
			if (pos instanceof DescriptiveParsePosition) {
				Resources resources = Resources.getInstance();
				String errorMessage = resources.getString(detail(ex));
				((DescriptiveParsePosition) pos).setErrorDescription(errorMessage);
			}
			return null;
		}
	}

	private static ResKey detail(Throwable ex) {
		return toKey(null, ex);
	}

	private static ResKey toKey(Set<ResKey> seen, Throwable ex) {
		ResKey localKey = localKey(seen == null || seen.isEmpty(), ex);
		Throwable cause = ex.getCause();
		if (localKey == null) {
			if (cause == null) {
				return null;
			} else {
				return toKey(seen, cause);
			}
		} else {
			return join(seen, localKey, cause);
		}
	}

	private static ResKey join(Set<ResKey> seen, ResKey message, Throwable cause) {
		if (cause != null) {
			if (seen == null) {
				seen = new HashSet<>();
			}
			boolean isNew = seen.add(message);

			// Descend to cause.
			ResKey detail = toKey(seen, cause);

			if (isNew) {
				if (detail != null) {
					return I18NConstants.JOIN__A_B.fill(message, detail);
				} else {
					return message;
				}
			} else {
				return detail;
			}
		} else {
			if (seen == null || seen.add(message)) {
				return message;
			} else {
				return null;
			}
		}
	}

	private static ResKey localKey(boolean first, Throwable ex) {
		if (ex instanceof I18NFailure) {
			return ((I18NFailure) ex).getErrorKey();
		} else if (first) {
			String result = ex.getMessage();
			if (result == null) {
				result = ex.getClass().getSimpleName();
			}
			return ResKey.text(result);
		} else {
			return null;
		}
	}
}