/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.annotation.EnumDefaultValue;

/**
 * Where a form field renders its label relative to the input.
 *
 * <p>
 * Used both at the layout level ({@link ReactFormLayoutControl}, where {@link #AUTO} resolves
 * responsively between {@link #SIDE} and {@link #TOP} by available width) and at the field level
 * ({@link ReactFormFieldChromeControl}, where a {@code null} position inherits from the enclosing
 * layout and {@link #AFTER} trails the input, e.g. for a checkbox).
 * </p>
 *
 * <p>
 * The {@link #getExternalName() external name} is both the value written in a configuration and the
 * value sent to the client.
 * </p>
 */
public enum LabelPosition implements ExternallyNamed {

	/** Label beside the input. */
	SIDE("side"),

	/** Label above the input. */
	TOP("top"),

	/** Label after the input (e.g. trailing a checkbox); field level only. */
	AFTER("after"),

	/** No label at all; the input spans the full field width. Field level only. */
	HIDDEN("hidden"),

	/**
	 * Resolve responsively between {@link #SIDE} and {@link #TOP} from the available width; layout
	 * level only.
	 */
	AUTO("auto");

	/**
	 * Default value provider answering {@link LabelPosition#AUTO}, for a configuration property
	 * whose label position is the responsive one unless stated otherwise.
	 */
	public static class AutoDefault extends EnumDefaultValue {
		@Override
		public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
			return AUTO;
		}
	}

	private final String _externalName;

	LabelPosition(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

}
