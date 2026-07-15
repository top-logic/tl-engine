/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.channel;

import junit.framework.TestCase;

import com.top_logic.layout.view.channel.ChannelVetoException;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.DirtyChannel;
import com.top_logic.layout.view.channel.ViewChannel.VetoListener;
import com.top_logic.layout.view.form.StateHandler;

/**
 * Integration tests for the dirty-check mechanism: channel veto on selection change,
 * {@link DirtyChannel} for tab switching, and error handling.
 */
public class TestDirtyCheckIntegration extends TestCase {

	/**
	 * A mutable {@link StateHandler} stub for use in integration tests.
	 */
	private static class MutableStubHandler implements StateHandler {

		boolean _dirty;

		boolean _hasErrors;

		private final String _description;

		MutableStubHandler(String description) {
			_description = description;
		}

		@Override
		public boolean isDirty() {
			return _dirty;
		}

		@Override
		public boolean hasErrors() {
			return _hasErrors;
		}

		@Override
		public void executeSave() {
			_dirty = false;
		}

		@Override
		public void executeDiscard() {
			_dirty = false;
		}

		@Override
		public String getDescription() {
			return _description;
		}
	}

	/**
	 * Simulates a dirty form vetoing a selection change, followed by user discard and successful
	 * retry via the continuation.
	 *
	 * <ol>
	 * <li>Create {@link DefaultViewChannel}, set "objectA".</li>
	 * <li>Register a dirty handler as veto listener.</li>
	 * <li>Try channel.set("objectB") - expect {@link ChannelVetoException}.</li>
	 * <li>Assert exactly one dirty handler, channel value still "objectA".</li>
	 * <li>Discard changes - handler becomes clean.</li>
	 * <li>Run continuation - channel is now "objectB".</li>
	 * </ol>
	 */
	public void testSelectionChangeVetoThenDiscard() {
		DefaultViewChannel channel = new DefaultViewChannel("selection");
		channel.set("objectA");

		MutableStubHandler handler = new MutableStubHandler("myForm");
		handler._dirty = true;

		VetoListener vetoListener = (sender, oldValue, newValue) -> handler.isDirty() ? handler : null;
		channel.addVetoListener(vetoListener);

		ChannelVetoException caught = null;
		try {
			channel.set("objectB");
			fail("Expected ChannelVetoException");
		} catch (ChannelVetoException ex) {
			caught = ex;
		}

		assertNotNull("Exception must have been thrown", caught);
		assertEquals("Exactly one dirty handler", 1, caught.getDirtyHandlers().size());
		assertSame("Handler must be the registered one", handler, caught.getDirtyHandlers().get(0));
		assertEquals("Channel value must remain unchanged", "objectA", channel.get());

		// User discards changes
		handler.executeDiscard();
		assertFalse("Handler must no longer be dirty after discard", handler.isDirty());

		// Retry via continuation
		caught.getContinuation().run();
		assertEquals("Channel must be updated after continuation", "objectB", channel.get());
	}

	/**
	 * Simulates a {@link DirtyChannel} tracking two forms in a tab scope. Verifies dirty/clean
	 * state transitions.
	 *
	 * <ol>
	 * <li>Create {@link DirtyChannel}, register 2 dirty handlers.</li>
	 * <li>Verify hasDirtyHandlers() and count.</li>
	 * <li>Save form1 - still dirty (form2 remains).</li>
	 * <li>Discard form2 - channel becomes clean.</li>
	 * </ol>
	 */
	public void testTabSwitchWithDirtyChannel() {
		DirtyChannel dirtyChannel = new DirtyChannel();

		MutableStubHandler form1 = new MutableStubHandler("form1");
		MutableStubHandler form2 = new MutableStubHandler("form2");

		// Register both as dirty
		dirtyChannel.updateState(form1, true);
		dirtyChannel.updateState(form2, true);

		assertTrue("Channel must have dirty handlers", dirtyChannel.hasDirtyHandlers());
		assertEquals("Both handlers must be tracked", 2, dirtyChannel.getDirtyHandlers().size());

		// Save form1 - form2 still dirty
		form1.executeSave();
		dirtyChannel.updateState(form1, false);

		assertTrue("Channel must still have dirty handlers after form1 save", dirtyChannel.hasDirtyHandlers());
		assertEquals("Only form2 must remain", 1, dirtyChannel.getDirtyHandlers().size());
		assertSame("form2 must be the remaining handler", form2, dirtyChannel.getDirtyHandlers().get(0));

		// Discard form2 - channel clean
		form2.executeDiscard();
		dirtyChannel.updateState(form2, false);

		assertFalse("Channel must be clean after discarding form2", dirtyChannel.hasDirtyHandlers());
		assertTrue("Dirty handler list must be empty", dirtyChannel.getDirtyHandlers().isEmpty());
	}

	/**
	 * Simulates multiple dirty handlers where one has validation errors. Verifies that the exception
	 * carries both handlers and that at least one reports errors (so the save button in the
	 * confirmation dialog can be hidden).
	 *
	 * <ol>
	 * <li>Create channel, set "initial".</li>
	 * <li>Create 2 handlers: one dirty without errors, one dirty with errors.</li>
	 * <li>Register veto listeners for both.</li>
	 * <li>Try channel.set("next") - expect {@link ChannelVetoException} with 2 handlers.</li>
	 * <li>Assert at least one handler has errors.</li>
	 * </ol>
	 */
	public void testCanSaveWithErrors() {
		DefaultViewChannel channel = new DefaultViewChannel("selection");
		channel.set("initial");

		MutableStubHandler cleanHandler = new MutableStubHandler("form-no-errors");
		cleanHandler._dirty = true;
		cleanHandler._hasErrors = false;

		MutableStubHandler errorHandler = new MutableStubHandler("form-with-errors");
		errorHandler._dirty = true;
		errorHandler._hasErrors = true;

		channel.addVetoListener((sender, oldValue, newValue) -> cleanHandler.isDirty() ? cleanHandler : null);
		channel.addVetoListener((sender, oldValue, newValue) -> errorHandler.isDirty() ? errorHandler : null);

		ChannelVetoException caught = null;
		try {
			channel.set("next");
			fail("Expected ChannelVetoException");
		} catch (ChannelVetoException ex) {
			caught = ex;
		}

		assertNotNull("Exception must have been thrown", caught);
		assertEquals("Both handlers must be in the exception", 2, caught.getDirtyHandlers().size());

		boolean anyWithErrors = caught.getDirtyHandlers().stream()
			.anyMatch(h -> h.hasErrors());
		assertTrue("At least one handler must have errors (save button must be hidden)", anyWithErrors);
	}
}
