/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.overlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.layout.react.dirty.UnsavedChanges;

import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.I18NConstants;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.overlay.DirtyConfirmDialogControl;
import com.top_logic.layout.react.control.overlay.ReactDialogManagerControl;
import com.top_logic.layout.react.dirty.ChannelVetoException;
import com.top_logic.layout.react.dirty.StateHandler;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.util.Resources;

/**
 * Tests that a change of the display unsaved changes refuse is put to the user and applied again
 * once they answered - what
 * {@link DirtyConfirmDialogControl#guard(ReactContext, com.top_logic.layout.react.control.overlay.DialogManager, Runnable, Runnable)}
 * does with a step that a form standing in its way lets fail.
 *
 * <p>
 * The step of these tests is refused by the unsaved changes it is given and goes through once they
 * are saved or discarded, which is what the dialog's buttons do to them.
 * </p>
 */
public class TestDirtyConfirmGuard extends TestCase {

	/** The state key holding a button's label, by which the dialog's actions are told apart. */
	private static final String LABEL = "label";

	/** The command a button runs when it is pressed. */
	private static final String CMD_CLICK = "click";

	/** The context the dialog is opened in. */
	private ReactContext _context;

	/** Where the question about the unsaved changes is put to the user. */
	private ReactDialogManagerControl _dialogs;

	/** The unsaved changes the step is refused by, in the order they refuse it. */
	private List<UnsavedChanges> _refusing;

	private Step _step;

	/** How often the refusal was reported to the caller instead of being put to the user. */
	private int _refusals;

	/**
	 * A change of the display that is refused as long as one of the unsaved changes it meets is
	 * unresolved, and is applied once they all are.
	 */
	private final class Step implements Runnable {

		/** How often the step was applied, successfully or not. */
		int _runs;

		/** Whether the step got through. */
		boolean _applied;

		@Override
		public void run() {
			_runs++;
			for (UnsavedChanges changes : _refusing) {
				if (changes.isDirty()) {
					throw new ChannelVetoException(List.<StateHandler> of(changes), () -> {
						// The retry is run by the guard, not by the veto's own continuation.
					});
				}
			}
			_applied = true;
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_dialogs = new ReactDialogManagerControl(_context);
		_refusing = new ArrayList<>();
		_step = new Step();
		_refusals = 0;
	}

	/** Runs the step under the guard, with the dialog manager the test observes. */
	private void guard() {
		DirtyConfirmDialogControl.guard(_context, _dialogs, _step, this::refused);
	}

	/** What the caller does with a refusal that cannot be put to the user. */
	private void refused() {
		_refusals++;
	}

	/**
	 * Tests that a step nothing refuses is applied where it stands, without a question the user
	 * would have to answer.
	 */
	public void testUnrefusedStepIsApplied() {
		guard();

		assertTrue("The step must be applied.", _step._applied);
		assertEquals("The step must not be applied twice.", 1, _step._runs);
		assertFalse("Nothing refused the step, so nothing is asked.", isDialogOpen());
	}

	/**
	 * Tests that a refused step is not applied but asked about, and that discarding the unsaved
	 * changes applies it.
	 */
	public void testDiscardAppliesTheStep() {
		UnsavedChanges changes = refusedBy();

		guard();

		assertFalse("The unsaved changes refuse the step.", _step._applied);
		assertEquals(1, _step._runs);
		assertTrue("The refusal must be put to the user.", isDialogOpen());

		press(I18NConstants.BUTTON_DISCARD);

		assertFalse("The discarded input is resolved.", changes.isDirty());
		assertTrue("The step must be applied once the user answered.", _step._applied);
		assertEquals("The step is applied again, not a third time.", 2, _step._runs);
		assertFalse("The answered question is gone.", isDialogOpen());
		assertEquals("A question was asked, so nothing was reported to the caller.", 0, _refusals);
	}

	/**
	 * Tests that saving the unsaved changes applies the step just as discarding them does.
	 */
	public void testSaveAppliesTheStep() {
		UnsavedChanges changes = refusedBy();

		guard();
		press(I18NConstants.BUTTON_SAVE);

		assertFalse("The saved input is resolved.", changes.isDirty());
		assertTrue(_step._applied);
		assertEquals(2, _step._runs);
		assertFalse(isDialogOpen());
	}

	/**
	 * Tests that the user keeping their input leaves the step unapplied: the display stays where the
	 * refusal keeps it, and nothing is run a second time.
	 */
	public void testCancelLeavesTheStepUnapplied() {
		UnsavedChanges changes = refusedBy();

		guard();
		press(ButtonType.CANCEL.getButtonLabelKey());

		assertTrue("The input the user kept is still unsaved.", changes.isDirty());
		assertFalse("The step must not be applied against the user's answer.", _step._applied);
		assertEquals("The step must not be run again.", 1, _step._runs);
		assertFalse(isDialogOpen());
	}

	/**
	 * Tests that a second form refusing the retry is asked about in turn, and that the step is
	 * applied once both are resolved.
	 */
	public void testSecondRefusalIsAskedAgain() {
		UnsavedChanges first = refusedBy();
		UnsavedChanges second = refusedBy();

		guard();
		press(I18NConstants.BUTTON_DISCARD);

		assertFalse(first.isDirty());
		assertTrue("The form behind the first one refuses the retry.", second.isDirty());
		assertFalse("The step is refused a second time.", _step._applied);
		assertEquals(2, _step._runs);
		assertTrue("The second refusal must be put to the user as well.", isDialogOpen());

		press(I18NConstants.BUTTON_DISCARD);

		assertFalse(second.isDirty());
		assertTrue("Nothing refuses the step any more.", _step._applied);
		assertEquals(3, _step._runs);
		assertFalse(isDialogOpen());
	}

	/**
	 * Tests that a refusal nobody can ask about is reported to the caller, which gives up what the
	 * step was part of rather than applying it over unsaved input.
	 */
	public void testRefusalWithoutDialogManagerIsReported() {
		refusedBy();

		DirtyConfirmDialogControl.guard(_context, null, _step, this::refused);

		assertEquals("The caller must be told that nothing could ask.", 1, _refusals);
		assertFalse("The step must not be applied.", _step._applied);
		assertEquals("The step must not be run again.", 1, _step._runs);
	}

	/**
	 * Tests that a caller that has nothing to give up may leave the report out: the refusal then
	 * simply leaves the step unapplied.
	 */
	public void testRefusalWithoutDialogManagerAndWithoutReport() {
		refusedBy();

		DirtyConfirmDialogControl.guard(_context, null, _step, null);

		assertFalse(_step._applied);
		assertEquals(1, _step._runs);
	}

	/** Adds unsaved changes the step is refused by. */
	private UnsavedChanges refusedBy() {
		UnsavedChanges changes = new UnsavedChanges();
		_refusing.add(changes);
		return changes;
	}

	/** Whether a dialog asks the user something. */
	private boolean isDialogOpen() {
		return !_dialogs.displayedChildren().isEmpty();
	}

	/** Presses the dialog button carrying the given label. */
	private void press(ResKey label) {
		String text = Resources.getInstance().getString(label);
		for (ReactButtonControl button : dialogButtons()) {
			if (text.equals(button.scriptingScalarState().get(LABEL))) {
				button.executeClientCommand(CMD_CLICK, Map.of());
				return;
			}
		}
		fail("The dialog offers no '" + text + "' button.");
	}

	/** The buttons the open dialogs offer. */
	private List<ReactButtonControl> dialogButtons() {
		List<ReactButtonControl> result = new ArrayList<>();
		collectButtons(_dialogs, result);
		return result;
	}

	private static void collectButtons(ReactControl control, List<ReactButtonControl> result) {
		for (ReactControl child : control.displayedChildren()) {
			if (child instanceof ReactButtonControl button) {
				result.add(button);
			}
			collectButtons(child, result);
		}
	}

	/**
	 * Suite requiring the resources and images the dialog's buttons are built from.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestDirtyConfirmGuard.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE,
				ThemeFactory.Module.INSTANCE));
	}

}
