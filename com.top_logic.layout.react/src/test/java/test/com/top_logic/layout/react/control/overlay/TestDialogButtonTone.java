/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.overlay;

import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.json.JSON;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ButtonAppearance;
import com.top_logic.layout.react.control.button.ButtonTone;
import com.top_logic.layout.react.control.button.KeyStroke;
import com.top_logic.layout.react.control.button.MessageButtons;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.overlay.ConfirmDialogControl;
import com.top_logic.layout.react.control.overlay.DialogHandle;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.react.control.overlay.DialogResultHandler;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.ButtonState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Tests the tone of the buttons a dialog offers: an answer that destroys or discards what the user
 * has is drawn as destructive, every other answer is not.
 */
public class TestDialogButtonTone extends TestCase {

	/** Discarding unsaved changes is destructive, without a command chain telling so. */
	public void testDiscardIsDestructive() {
		ReactButtonControl discard = MessageButtons.discard(createContext(), ctx -> HandlerResult.DEFAULT_RESULT);

		assertEquals(ButtonTone.DANGER.getExternalName(), state(discard).get(ButtonState.TONE__PROP));
	}

	private static ReactContext createContext() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	/** The state the control sends to the client. */
	static Map<?, ?> state(ReactControl control) {
		try {
			return (Map<?, ?>) JSON.fromString(control.stateAsJSON());
		} catch (JSON.ParseException ex) {
			throw new AssertionError("The state sent to the client is not JSON.", ex);
		}
	}

	/**
	 * "Delete it?": the affirmative answer lets the deletion happen and is drawn destructive - and
	 * stays the dialog's default, primary and on Enter. Declining destroys nothing.
	 */
	public void testTheConfirmationOfADestructionIsRedAndStaysOnEnter() {
		Dialogs dialogs = new Dialogs();
		ConfirmDialogControl.openDialog(createContext(), dialogs, "Confirm", "Delete the milestone?",
			"Delete now", "Keep it", () -> {
				// Nothing to do.
			}, null, ButtonTone.DANGER);

		Map<?, ?> confirm = state(button(dialogs.opened(), "Delete now"));
		assertEquals(ButtonTone.DANGER.getExternalName(), confirm.get(ButtonState.TONE__PROP));
		assertEquals(ButtonAppearance.PRIMARY.getExternalName(), confirm.get(ButtonState.APPEARANCE__PROP));
		assertEquals(KeyStroke.ENTER.toString(), confirm.get(ButtonState.KEY_GESTURE__PROP));
		assertNull("Declining destroys nothing.", state(button(dialogs.opened(), "Keep it")).get(ButtonState.TONE__PROP));
	}

	/** An ordinary question keeps its primary answer without a tone. */
	public void testAnOrdinaryConfirmationStaysPrimary() {
		Dialogs dialogs = new Dialogs();
		ConfirmDialogControl.openDialog(createContext(), dialogs, "Confirm", "Replace the file?",
			"Replace", "Keep it", () -> {
				// Nothing to do.
			}, null, ButtonTone.DEFAULT);

		Map<?, ?> confirm = state(button(dialogs.opened(), "Replace"));
		assertNull(confirm.get(ButtonState.TONE__PROP));
		assertEquals(ButtonAppearance.PRIMARY.getExternalName(), confirm.get(ButtonState.APPEARANCE__PROP));
	}

	/** The button of the given label in the displayed tree below the given control. */
	private static ReactButtonControl button(ReactControl control, String label) {
		if (control instanceof ReactButtonControl button && button.scriptingScalarState().containsValue(label)) {
			return button;
		}
		for (ReactControl child : control.displayedChildren()) {
			ReactButtonControl found = button(child, label);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	/** Stands in for the dialogs of a window: holds the one that was opened. */
	private static class Dialogs implements DialogManager {

		private ReactControl _opened;

		@Override
		public DialogHandle openDialog(boolean closeOnBackdrop, ReactControl child,
				DialogResultHandler<Void> handler) {
			_opened = child;
			return null;
		}

		@Override
		public void closeTopDialog(DialogResult<Void> result) {
			_opened = null;
		}

		@Override
		public void closeDialogsAbove(DialogHandle dialog) {
			throw new UnsupportedOperationException("Nothing is stacked on a confirmation.");
		}

		ReactControl opened() {
			assertNotNull("The user is asked.", _opened);
			return _opened;
		}
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module, the resources the standard labels are
	 * resolved from, and the {@link ThemeFactory} the buttons take their icons from.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestDialogButtonTone.class, TypeIndex.Module.INSTANCE,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE,
				ThemeFactory.Module.INSTANCE));
	}
}
