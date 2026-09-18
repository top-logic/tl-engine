/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control;

import junit.framework.TestCase;

import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.common.ReactProgressControl;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;

/**
 * Tests what a {@link ReactProgressControl} tells the client: a fraction the bar can be drawn from,
 * whatever number it was given, or no fraction at all where the share is unknown.
 *
 * <p>
 * The client draws the track from that one number, so a fraction beyond the range would draw a bar
 * running past its track - which is what a caller whose two counts disagree would produce. A bar
 * told no number states none, and the client sweeps over the track instead of filling a share of
 * it.
 * </p>
 */
public class TestReactProgressControl extends TestCase {

	/** A fraction within the range is passed on as it is. */
	public void testAFractionIsPassedOn() {
		assertEquals(0.25, fractionOf(control(0.25, null)), 0.0);
	}

	/** More than everything is a full bar, not one running past its track. */
	public void testMoreThanEverythingIsAFullBar() {
		assertEquals(1.0, fractionOf(control(1.5, null)), 0.0);
	}

	/** Less than nothing is an empty bar. */
	public void testLessThanNothingIsAnEmptyBar() {
		assertEquals(0.0, fractionOf(control(-0.2, null)), 0.0);
	}

	/** A number that is none is an empty bar rather than an undrawable width. */
	public void testNoNumberAtAllIsAnEmptyBar() {
		assertEquals(0.0, fractionOf(control(Double.NaN, null)), 0.0);
	}

	/** A bar without a label names none, so the client draws the bar alone. */
	public void testABarWithoutALabelNamesNone() {
		assertNull(control(0.5, null).scriptingScalarState().get(ReactProgressControl.LABEL));
	}

	/** Both are updated together: the fraction and the text describing it never disagree. */
	public void testTheFractionAndTheLabelAreUpdatedTogether() {
		ReactProgressControl control = control(0.25, "1 / 4");

		control.setProgress(0.75, "3 / 4");

		assertEquals(0.75, fractionOf(control), 0.0);
		assertEquals("3 / 4", control.scriptingScalarState().get(ReactProgressControl.LABEL));
	}

	/** A bar that knows no share states no fraction, and still says what it is busy with. */
	public void testABarWithoutAShareStatesNoFraction() {
		ReactProgressControl control = control(null, "Collecting");

		assertNoFraction(control);
		assertEquals("Collecting", control.scriptingScalarState().get(ReactProgressControl.LABEL));
	}

	/** A share learned later turns the sweeping bar into a filled one. */
	public void testAShareLearnedLaterFillsTheBar() {
		ReactProgressControl control = control(null, null);

		control.setFraction(Double.valueOf(0.5));

		assertEquals(0.5, fractionOf(control), 0.0);
	}

	/** A share lost again turns the bar back into a sweeping one. */
	public void testAShareLostAgainEmptiesTheFraction() {
		ReactProgressControl control = control(0.5, null);

		control.setIndeterminate();

		assertNoFraction(control);
	}

	/** Fraction and label are dropped together, so an indeterminate bar can name its phase. */
	public void testTheFractionIsDroppedTogetherWithTheLabel() {
		ReactProgressControl control = control(0.5, "1 / 2");

		control.setProgress(null, "Finishing");

		assertNoFraction(control);
		assertEquals("Finishing", control.scriptingScalarState().get(ReactProgressControl.LABEL));
	}

	private static void assertNoFraction(ReactProgressControl control) {
		Object fraction = control.scriptingScalarState().get(ReactProgressControl.FRACTION);
		assertNull("The bar must state no share: " + fraction, fraction);
	}

	private static double fractionOf(ReactProgressControl control) {
		Object fraction = control.scriptingScalarState().get(ReactProgressControl.FRACTION);
		assertTrue("The bar must tell the client a number: " + fraction, fraction instanceof Number);
		return ((Number) fraction).doubleValue();
	}

	private static ReactProgressControl control(Double fraction, String label) {
		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		return new ReactProgressControl(context, fraction, label);
	}

}
