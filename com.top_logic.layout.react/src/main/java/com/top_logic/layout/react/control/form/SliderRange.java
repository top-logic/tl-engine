/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

/**
 * The range a {@link ReactSliderControl} travels: its bounds, the distance between two values it
 * can set, and the unit its values are written with.
 *
 * <p>
 * A range is {@link #usable() usable} when both bounds are given, the lower one is below the upper
 * one, and the {@link #effectiveStep() step} is above zero. A slider over a range that is not usable
 * cannot be moved.
 * </p>
 *
 * @param min
 *        The smallest value that can be set, {@code null} if not known.
 * @param max
 *        The largest value that can be set, {@code null} if not known.
 * @param step
 *        The distance between two values that can be set, {@code null} for whole numbers
 *        ({@link #DEFAULT_STEP}).
 * @param unit
 *        The unit written after each value, {@code null} for none.
 */
public record SliderRange(Double min, Double max, Double step, String unit) {

	/**
	 * The {@link #effectiveStep() step} of a range that states none: the value moves by whole
	 * numbers.
	 */
	public static final double DEFAULT_STEP = 1.0;

	/**
	 * A range without bounds, which is not {@link #usable() usable}.
	 */
	public static final SliderRange NONE = new SliderRange(null, null, null, null);

	/**
	 * The distance between two values that can be set, {@link #DEFAULT_STEP} if the range states
	 * none.
	 */
	public double effectiveStep() {
		return step == null ? DEFAULT_STEP : step.doubleValue();
	}

	/**
	 * Whether a slider can travel this range: both bounds are given, the lower one is below the
	 * upper one, and the {@link #effectiveStep() step} is above zero.
	 */
	public boolean usable() {
		return min != null && max != null && min.doubleValue() < max.doubleValue() && effectiveStep() > 0;
	}

}
