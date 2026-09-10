/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.common;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;

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
 */
public class ReactProgressControl extends ReactControl {

	private static final String REACT_MODULE = "TLProgress";

	/** State key for the displayed fraction, between 0 and 1. */
	public static final String FRACTION = "fraction";

	/** State key for the text displayed beside the bar, or {@code null} for a bar without one. */
	public static final String LABEL = "label";

	/**
	 * Creates a {@link ReactProgressControl}.
	 *
	 * @param context
	 *        The {@link ReactContext} for ID allocation and SSE registration.
	 * @param fraction
	 *        The initial fraction, between 0 and 1.
	 * @param label
	 *        The initial label, or {@code null} for a bar without one.
	 */
	public ReactProgressControl(ReactContext context, double fraction, String label) {
		super(context, null, REACT_MODULE);
		putState(FRACTION, Double.valueOf(clamp(fraction)));
		putState(LABEL, label);
	}

	/**
	 * Updates the displayed fraction.
	 *
	 * @param fraction
	 *        The new fraction, between 0 and 1.
	 */
	public void setFraction(double fraction) {
		putState(FRACTION, Double.valueOf(clamp(fraction)));
	}

	/**
	 * Updates the label displayed beside the bar.
	 *
	 * @param label
	 *        The new label, or {@code null} for a bar without one.
	 */
	public void setLabel(String label) {
		putState(LABEL, label);
	}

	/**
	 * Updates the fraction and the label together, in one patch.
	 *
	 * @param fraction
	 *        The new fraction, between 0 and 1.
	 * @param label
	 *        The new label, or {@code null} for a bar without one.
	 */
	public void setProgress(double fraction, String label) {
		Object tx = beginUpdate();
		setFraction(fraction);
		setLabel(label);
		commitUpdate(tx);
	}

	/**
	 * The given fraction as the bar displays it: within 0 and 1, and an empty bar for a number
	 * that is none.
	 */
	private static double clamp(double fraction) {
		if (Double.isNaN(fraction)) {
			return 0d;
		}
		return Math.max(0d, Math.min(1d, fraction));
	}

}
