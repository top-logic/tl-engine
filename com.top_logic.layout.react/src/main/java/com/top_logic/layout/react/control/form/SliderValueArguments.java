/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.layout.react.control.ReactCommand;

/**
 * Typed arguments of a slider's {@link ReactFormFieldControl#CMD_VALUE_CHANGED} command: the value
 * the handle stands on.
 *
 * <p>
 * A slider's value is a number, not the text the base field assumes - hence its own typed
 * arguments. It arrives as the most general number, and the {@link ReactSliderControl} turns it
 * into the kind of number the edited field holds.
 * </p>
 *
 * @implNote The conversion to the value type of the field is
 *           {@link ReactSliderControl#parseClientValue(Object)}.
 */
@Label("Set '{target}' to '{value}'")
public interface SliderValueArguments extends ReactCommand {

	/** @see #getValue() */
	String VALUE = "value";

	/**
	 * The value the handle stands on, {@code null} for a field holding no value.
	 */
	@Name(VALUE)
	@Nullable
	Double getValue();

}
