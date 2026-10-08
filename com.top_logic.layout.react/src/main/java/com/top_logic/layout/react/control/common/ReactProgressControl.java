/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.common;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.state.ProgressState;

/**
 * A read-only control displaying a fraction as a bar with an optional label, via the
 * {@code TLProgress} React component.
 *
 * <p>
 * The fraction is a number between 0 and 1; a value outside that range is displayed at the end it
 * exceeds, so a caller whose two counts disagree draws a full or an empty bar rather than one
 * running past its track. What the fraction counts is the caller's business: the control is told a
 * number and a text, and knows nothing of what they measure.
 * </p>
 *
 * <p>
 * A {@link ProgressState#FRACTION__PROP fraction} of {@code null} is the indeterminate bar: the bar says that something is
 * going on without saying how far it has come, and the client animates a sweep over the track
 * instead of filling a share of it. A caller that learns the share only later starts with
 * {@link #setIndeterminate()} and switches to a number as soon as it has one, and back whenever it
 * loses it again.
 * </p>
 *
 * <p>
 * The state is described by {@link ProgressState}.
 * </p>
 */
public class ReactProgressControl extends ReactControl {

	private static final String REACT_MODULE = "TLProgress";

	/**
	 * Creates a {@link ReactProgressControl}.
	 *
	 * @param context
	 *        The {@link ReactContext} for ID allocation and SSE registration.
	 * @param fraction
	 *        The initial fraction, between 0 and 1, or {@code null} for the indeterminate bar.
	 * @param label
	 *        The initial label, or {@code null} for a bar without one.
	 */
	public ReactProgressControl(ReactContext context, Double fraction, String label) {
		super(context, null, REACT_MODULE);
		putState(ProgressState.FRACTION__PROP, clamp(fraction));
		putState(ProgressState.LABEL__PROP, label);
	}

	/**
	 * Updates the displayed fraction.
	 *
	 * @param fraction
	 *        The new fraction, between 0 and 1, or {@code null} for the indeterminate bar.
	 */
	public void setFraction(Double fraction) {
		putState(ProgressState.FRACTION__PROP, clamp(fraction));
	}

	/**
	 * Displays the bar without a share: something is going on, how far it has come is unknown.
	 *
	 * @see #setFraction(Double)
	 */
	public void setIndeterminate() {
		setFraction(null);
	}

	/**
	 * Updates the label displayed beside the bar.
	 *
	 * @param label
	 *        The new label, or {@code null} for a bar without one.
	 */
	public void setLabel(String label) {
		putState(ProgressState.LABEL__PROP, label);
	}

	/**
	 * Updates the fraction and the label together, in one patch.
	 *
	 * @param fraction
	 *        The new fraction, between 0 and 1, or {@code null} for the indeterminate bar.
	 * @param label
	 *        The new label, or {@code null} for a bar without one.
	 */
	public void setProgress(Double fraction, String label) {
		Object tx = beginUpdate();
		setFraction(fraction);
		setLabel(label);
		commitUpdate(tx);
	}

	/**
	 * The given fraction as the bar displays it: within 0 and 1, an empty bar for a number that is
	 * none, and the indeterminate bar for no number at all.
	 */
	private static Double clamp(Double fraction) {
		if (fraction == null) {
			return null;
		}
		double value = fraction.doubleValue();
		if (Double.isNaN(value)) {
			return Double.valueOf(0d);
		}
		return Double.valueOf(Math.max(0d, Math.min(1d, value)));
	}

}
