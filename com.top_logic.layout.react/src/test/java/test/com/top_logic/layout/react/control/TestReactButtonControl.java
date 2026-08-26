/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control;

import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Tests that a {@link ReactButtonControl} the user was never offered refuses the {@code click}
 * command, whether or not it was built from a
 * {@link com.top_logic.layout.react.control.button.CommandModel}.
 *
 * <p>
 * A hidden control keeps its React component tree - it is only styled away - so it stays mounted,
 * stays registered with the update queue, and its commands stay addressable. Nothing but the
 * server-side guard stands between a view-only form and the action behind such a button.
 * </p>
 */
public class TestReactButtonControl extends TestCase {

	/**
	 * A {@link ReactFormFieldControl} over a plain field model, standing in for any field that
	 * hands out an edit-mode adornment - e.g. the internationalized string input and its languages
	 * button.
	 */
	private static final class TestFieldControl extends ReactFormFieldControl {

		TestFieldControl(ReactContext context, AbstractFieldModel model) {
			super(context, model, "TLTextInput");
		}
	}

	private ReactContext createTestContext() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue());
	}

	/**
	 * Records whether the button's action ran, so a refused click can be told apart from one that
	 * ran and merely reported an error of its own.
	 */
	private static final class Trace {
		boolean _executed;
	}

	private ReactButtonControl createButton(ReactContext context, Trace trace) {
		return new ReactButtonControl(context, "Press me", ctx -> {
			trace._executed = true;
			return HandlerResult.DEFAULT_RESULT;
		});
	}

	/** A plain, offered button runs its action - the precondition every other test rests on. */
	public void testAnOfferedButtonIsPressed() {
		Trace trace = new Trace();
		ReactButtonControl button = createButton(createTestContext(), trace);

		HandlerResult result = button.executeClientCommand("click", Map.of());

		assertTrue(result.isSuccess());
		assertTrue("The action must run.", trace._executed);
	}

	/**
	 * A hidden button does not, even though it was built from a plain action rather than from a
	 * {@link com.top_logic.layout.react.control.button.CommandModel}.
	 */
	public void testAHiddenButtonIsNotPressed() {
		Trace trace = new Trace();
		ReactButtonControl button = createButton(createTestContext(), trace);
		button.setHidden(true);

		HandlerResult result = button.executeClientCommand("click", Map.of());

		assertFalse("A button the user was never offered must not be pressed.", result.isSuccess());
		assertFalse("The action must not run.", trace._executed);
	}

	/** Neither does a disabled one. */
	public void testADisabledButtonIsNotPressed() {
		Trace trace = new Trace();
		ReactButtonControl button = createButton(createTestContext(), trace);
		button.setDisabled(true);

		HandlerResult result = button.executeClientCommand("click", Map.of());

		assertFalse(result.isSuccess());
		assertFalse("The action must not run.", trace._executed);
	}

	/**
	 * The case this guard exists for: a field's edit-mode adornment on a field that may not be
	 * edited. {@link ReactFormFieldControl#setEditModeAdornment(com.top_logic.layout.react.control.ReactControl)}
	 * only hides it, which leaves it mounted and addressable - so the write behind it must be
	 * refused here.
	 */
	public void testAnAdornmentOfANonEditableFieldIsNotPressed() {
		ReactContext context = createTestContext();
		AbstractFieldModel model = new AbstractFieldModel("value");
		model.setEditable(false);
		TestFieldControl field = new TestFieldControl(context, model);
		Trace trace = new Trace();
		ReactButtonControl adornment = createButton(context, trace);
		field.setEditModeAdornment(adornment);

		HandlerResult result = adornment.executeClientCommand("click", Map.of());

		assertFalse("A view-only field must not offer a way to write its value.", result.isSuccess());
		assertFalse("The action must not run.", trace._executed);
	}

	/** Making the field editable again offers the adornment again. */
	public void testAnAdornmentOfAnEditableFieldIsPressed() {
		ReactContext context = createTestContext();
		AbstractFieldModel model = new AbstractFieldModel("value");
		model.setEditable(false);
		TestFieldControl field = new TestFieldControl(context, model);
		Trace trace = new Trace();
		ReactButtonControl adornment = createButton(context, trace);
		field.setEditModeAdornment(adornment);

		model.setEditable(true);
		HandlerResult result = adornment.executeClientCommand("click", Map.of());

		assertTrue(result.isSuccess());
		assertTrue("The action must run again once the field accepts input.", trace._executed);
	}

}
