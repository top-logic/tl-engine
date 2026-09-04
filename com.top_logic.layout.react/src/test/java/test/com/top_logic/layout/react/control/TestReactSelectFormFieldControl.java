/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control;

import java.util.List;

import junit.framework.TestCase;

import com.top_logic.layout.form.model.SimpleSelectFieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.form.ReactSelectFormFieldControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;

/**
 * Tests that a {@link ReactSelectFormFieldControl} tells the client about the value it holds, even
 * where that value is not among the options offered.
 *
 * <p>
 * The client picks what to display by matching the value against the options it was given. A value
 * missing from them is shown as nothing at all while read-only, and while editable as whatever a
 * {@code <select>} falls back to - its first option. The field then displays a value nobody
 * configured, and saving the form would make that display true.
 * </p>
 */
public class TestReactSelectFormFieldControl extends TestCase {

	private static final String OPTIONS = "\"options\"";

	private ReactContext createTestContext() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue());
	}

	private ReactSelectFormFieldControl createSelect(Object value, List<?> options) {
		return new ReactSelectFormFieldControl(createTestContext(),
			new SimpleSelectFieldModel(value, options, false), String::valueOf);
	}

	/** The ordinary case: what is offered is what is sent. */
	public void testTheOfferedOptionsAreSent() {
		String state = createSelect("Group", List.of("Contact", "Group")).stateAsJSON();

		assertTrue(state, state.contains(OPTIONS));
		assertTrue("Every option must reach the client.", state.contains("Contact"));
		assertTrue(state.contains("Group"));
	}

	/**
	 * A value none of the options names is added to them, so the client can show it.
	 *
	 * <p>
	 * Counted rather than merely searched for: the value is part of the field state anyway, so its
	 * bare presence in the JSON would say nothing about the options at all.
	 * </p>
	 */
	public void testTheValueInUseIsSentEvenWhenItIsNoOption() {
		String state = createSelect("MetaAttribute", List.of("Contact", "Group")).stateAsJSON();

		assertEquals("The two offered options plus the value in use: " + state,
			3, optionCount(state));
	}

	/** ...and it comes first, where it cannot be overlooked. */
	public void testTheValueInUseComesFirst() {
		String state = createSelect("MetaAttribute", List.of("Contact", "Group")).stateAsJSON();

		assertTrue("The value in use must precede the options offered: " + state,
			state.indexOf("MetaAttribute") < state.indexOf("Contact"));
	}

	/** An empty field adds nothing - there is no value to speak of. */
	public void testNothingIsAddedForAnEmptyField() {
		String state = createSelect(null, List.of("Contact")).stateAsJSON();

		assertEquals("Only the offered option, nothing extra: " + state, 1, optionCount(state));
	}

	/**
	 * A value set later is offered too - the options a field sends depend on the value it holds, so
	 * a new value can change them.
	 */
	public void testAValueSetLaterIsOfferedAsWell() {
		SimpleSelectFieldModel model = new SimpleSelectFieldModel("Contact", List.of("Contact"), false);
		ReactSelectFormFieldControl select =
			new ReactSelectFormFieldControl(createTestContext(), model, String::valueOf);

		model.setValue("MetaAttribute");

		assertEquals("The one offered option plus the value written afterwards: "
			+ select.stateAsJSON(), 2, optionCount(select.stateAsJSON()));
	}

	/**
	 * How many options the given state carries, counted by their labels - an option's own text
	 * appears twice, as its value and as its label, so the text itself cannot be counted.
	 */
	private int optionCount(String state) {
		return occurrences(state, "\"label\"");
	}

	private int occurrences(String text, String part) {
		int count = 0;
		for (int i = text.indexOf(part); i >= 0; i = text.indexOf(part, i + part.length())) {
			count++;
		}
		return count;
	}

}
