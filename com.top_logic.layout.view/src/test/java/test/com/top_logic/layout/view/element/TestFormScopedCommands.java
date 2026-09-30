/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.lang.reflect.Method;

import junit.framework.TestCase;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.view.element.FormElement;

/**
 * Tests the {@link CommandModel} in which a {@link FormElement} wraps its configured commands
 * before offering them to the surrounding scope.
 */
public class TestFormScopedCommands extends TestCase {

	/**
	 * The wrapper answers the command's execution with the form's context, and everything else
	 * with the command itself.
	 *
	 * <p>
	 * A method the wrapper does not override falls back to the interface default, which detaches
	 * that aspect of the command from the command: a form command declaring a keyboard gesture
	 * loses it (and with it its primary presentation and the dialog's Enter default), a command
	 * declaring a tooltip, a clique or a presentation of its own loses those. The fallback is
	 * silent, so the delegation is asserted method by method.
	 * </p>
	 *
	 * <p>
	 * The one exception is {@link CommandModel#executeCommand(ReactContext)}: it is the check every
	 * model inherits, which decides by the delegated {@link CommandModel#getExecutableState() state}
	 * before the wrapper {@link CommandModel#perform(ReactContext) performs} the command.
	 * </p>
	 */
	public void testEveryCommandAccessorIsDelegated() throws NoSuchMethodException {
		Class<?> wrapper = formCommandWrapper();
		Method check = CommandModel.class.getMethod("executeCommand", ReactContext.class);

		for (Method accessor : CommandModel.class.getMethods()) {
			if (accessor.equals(check)) {
				continue;
			}
			try {
				wrapper.getDeclaredMethod(accessor.getName(), accessor.getParameterTypes());
			} catch (NoSuchMethodException ex) {
				fail("A command offered through a form loses what it states through '"
					+ accessor.getName() + "': " + wrapper.getSimpleName() + " does not delegate it.");
			}
		}
	}

	/**
	 * The {@link CommandModel} the form wraps its commands in.
	 */
	private static Class<?> formCommandWrapper() {
		for (Class<?> nested : FormElement.class.getDeclaredClasses()) {
			if (CommandModel.class.isAssignableFrom(nested)) {
				return nested;
			}
		}
		fail("The form no longer wraps its commands in a " + CommandModel.class.getSimpleName()
			+ " of its own.");
		return null;
	}
}
