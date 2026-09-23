/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * The shape a number is edited in: typed as text, or set by dragging a handle along a track.
 *
 * <p>
 * Both edit the same value and show it in the format the field asks for. They differ in what the
 * user does with it: an input takes any number the format reads, while a slider offers a range with
 * a smallest step and lets the value be picked without typing - and shows where in that range it
 * lies.
 * </p>
 *
 * @see ReactSliderControl
 */
public enum NumberDisplay implements ExternallyNamed {

	/**
	 * Edit the number as text, in the format it is displayed in.
	 *
	 * <p>
	 * The shape for a number of any size and any precision, and the only one for a value that has
	 * no range to lie in.
	 * </p>
	 */
	INPUT(NumberDisplay.INPUT_NAME),

	/**
	 * Edit the number by dragging a handle along a track, between a smallest and a largest value.
	 *
	 * <p>
	 * The shape for a value whose range is part of what it means - a percentage, a rating, a
	 * threshold within known limits: the handle says at a glance how much of the range is used, and
	 * the value is set with one gesture rather than typed. It needs the bounds of the range, which
	 * is why they are configured together with it.
	 * </p>
	 */
	SLIDER(NumberDisplay.SLIDER_NAME);

	/**
	 * The {@link #getExternalName() name} {@link #INPUT} is configured under.
	 */
	public static final String INPUT_NAME = "input";

	/**
	 * The {@link #getExternalName() name} {@link #SLIDER} is configured under.
	 *
	 * <p>
	 * The name is also what a constraint compares against where a configuration option is required
	 * by the slider alone, an annotation carrying a value only as a constant.
	 * </p>
	 */
	public static final String SLIDER_NAME = "slider";

	private final String _externalName;

	private NumberDisplay(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

}
