/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.form.FieldValueArguments;
import com.top_logic.layout.react.control.form.ReactCheckboxControl;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.form.ReactI18NStringInputControl;
import com.top_logic.layout.react.control.form.ReactNumberInputControl;
import com.top_logic.layout.react.control.form.ReactPasswordInputControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.TypingFieldState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Tests the {@link ReactFormFieldControl#COMMIT_COMMAND commit} of a finished edit: which fields
 * ask the client for it, and how a commit reaches the
 * {@link ReactFormFieldControl#setCommitListener(Function) commit listener}.
 */
public class TestReactFieldCommit extends TestCase {

	private static final ResKey REFUSAL = ResKey.text("Refused.");

	/**
	 * A commit reports the value the field holds - the one typed before - to the commit listener,
	 * once per commit.
	 */
	public void testCommitReportsCurrentValue() {
		ReactFormFieldControl field = text(new AbstractFieldModel(null));
		List<Object> reported = new ArrayList<>();
		field.setCommitListener(value -> {
			reported.add(value);
			return HandlerResult.DEFAULT_RESULT;
		});

		type(field, "abc");
		assertEquals("Typing alone is no finished edit.", List.of(), reported);

		HandlerResult result = commit(field);

		assertTrue(result.isSuccess());
		assertEquals(List.of("abc"), reported);
	}

	/**
	 * The result of the commit listener is the result of the commit, so that a refusal reaches the
	 * user.
	 */
	public void testCommitReturnsListenerResult() {
		ReactFormFieldControl field = text(new AbstractFieldModel("x"));
		HandlerResult refusal = HandlerResult.notExecutable(ExecutableState.createDisabledState(REFUSAL));
		field.setCommitListener(value -> refusal);

		HandlerResult result = commit(field);

		assertSame(refusal, result);
		assertFalse(result.isSuccess());
	}

	/**
	 * Without a listener a commit succeeds without further effect.
	 */
	public void testCommitWithoutListener() {
		ReactFormFieldControl field = text(new AbstractFieldModel("x"));

		HandlerResult result = commit(field);

		assertSame(HandlerResult.DEFAULT_RESULT, result);
		assertEquals("x", field.getFieldModel().getValue());
	}

	/**
	 * A listener that was removed is not called any more, and the client is no longer asked for
	 * commits.
	 */
	public void testRemovedListener() {
		ReactFormFieldControl field = text(new AbstractFieldModel("x"));
		List<Object> reported = new ArrayList<>();
		field.setCommitListener(value -> {
			reported.add(value);
			return HandlerResult.DEFAULT_RESULT;
		});
		assertTrue(commitEdits(field));

		field.setCommitListener(null);

		assertFalse(commitEdits(field));
		assertSame(HandlerResult.DEFAULT_RESULT, commit(field));
		assertEquals(List.of(), reported);
	}

	/**
	 * A field that may not be edited drops the commit: the listener does not run.
	 */
	public void testNonEditableFieldDropsCommit() {
		AbstractFieldModel model = new AbstractFieldModel("x");
		ReactFormFieldControl field = text(model);
		List<Object> reported = new ArrayList<>();
		field.setCommitListener(value -> {
			reported.add(value);
			return HandlerResult.notExecutable(ExecutableState.createDisabledState(REFUSAL));
		});
		model.setEditable(false);

		HandlerResult result = commit(field);

		assertSame(HandlerResult.DEFAULT_RESULT, result);
		assertEquals(List.of(), reported);
	}

	/**
	 * The client is asked for commits only once a listener is set.
	 */
	public void testCommitEditsNeedsListener() {
		ReactFormFieldControl field = text(new AbstractFieldModel(null));
		assertFalse(commitEdits(field));

		field.setCommitListener(value -> HandlerResult.DEFAULT_RESULT);

		assertTrue(commitEdits(field));
	}

	/**
	 * Every typed field commits edits: a single-line text, a text area (which commits when it is
	 * left), a number, a password and an internationalized text.
	 */
	public void testTypedFieldsCommitEdits() {
		ReactTextInputControl area = text(new AbstractFieldModel(null));
		area.setMultiline(3);

		List<ReactFormFieldControl> typed = List.of(
			text(new AbstractFieldModel(null)),
			area,
			new ReactNumberInputControl(context(), new AbstractFieldModel(null),
				NumberFormat.getInstance(Locale.GERMANY)),
			new ReactPasswordInputControl(context(), new AbstractFieldModel(null)),
			new ReactI18NStringInputControl(context(), new AbstractFieldModel(null)));
		for (ReactFormFieldControl field : typed) {
			String name = field.getClass().getSimpleName();
			assertTrue(name, field.commitsEdits());

			field.setCommitListener(value -> HandlerResult.DEFAULT_RESULT);

			assertTrue(name, commitEdits(field));
		}
	}

	/**
	 * A field that is clicked has no edit to finish: a checkbox is not asked for commits, even with
	 * a listener.
	 */
	public void testCheckboxDoesNotCommitEdits() {
		ReactCheckboxControl checkbox = new ReactCheckboxControl(context(), new AbstractFieldModel(Boolean.FALSE));
		assertFalse(checkbox.commitsEdits());

		checkbox.setCommitListener(value -> HandlerResult.DEFAULT_RESULT);

		assertFalse(commitEdits(checkbox));
	}

	private static boolean commitEdits(ReactFormFieldControl field) {
		return Boolean.TRUE.equals(field.scriptingScalarState().get(TypingFieldState.COMMIT_EDITS__PROP));
	}

	private static void type(ReactFormFieldControl field, String text) {
		field.executeCommand(ReactFormFieldControl.CMD_VALUE_CHANGED, Map.of(FieldValueArguments.VALUE, text));
	}

	private static HandlerResult commit(ReactFormFieldControl field) {
		return field.executeCommand(ReactFormFieldControl.COMMIT_COMMAND, Map.of());
	}

	private static ReactTextInputControl text(AbstractFieldModel model) {
		return new ReactTextInputControl(context(), model);
	}

	private static ReactContext context() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	/**
	 * The test suite, started with the resource bundles the controls need.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestReactFieldCommit.class, ResourcesModule.Module.INSTANCE));
	}

}
