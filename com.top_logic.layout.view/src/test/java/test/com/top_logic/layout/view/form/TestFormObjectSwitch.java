/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import junit.framework.TestCase;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.channel.ChannelVetoException;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.FormModel;
import com.top_logic.layout.view.form.FormModelListener;
import com.top_logic.layout.view.form.FormParticipant;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TransientObject;
import com.top_logic.model.listen.ModelChangeEvent;

/**
 * Tests which object a {@link FormControl} announces to its {@link FormModelListener}s when it stops
 * displaying its current one.
 *
 * <p>
 * A form that leaves an object must notify its fields once, for the object it switches to: a field
 * rebound to the object being left computes its state (options, constraints, validation) for an
 * object the form is about to stop displaying.
 * </p>
 */
public class TestFormObjectSwitch extends TestCase {

	/**
	 * An object switch while editing notifies once, for the object the form switches to.
	 */
	public void testSwitchWhileEditing() {
		TLObject first = new MockTLObject();
		TLObject second = new MockTLObject();

		FormControl form = newForm(first);
		ViewChannel input = newInput(form, first);
		form.enterEditMode();

		Recorder recorder = record(form);
		input.set(second);

		assertEquals("An object switch notifies exactly once.", List.of(second), recorder.objects());
		assertFalse("The edit session of the left object has ended.", form.isEditMode());
		assertSame("The form displays the object its input channel delivered.", second,
			form.getCurrentObject());
	}

	/**
	 * An object switch in view mode notifies once, for the object the form switches to.
	 */
	public void testSwitchInViewMode() {
		TLObject first = new MockTLObject();
		TLObject second = new MockTLObject();

		FormControl form = newForm(first);
		ViewChannel input = newInput(form, first);

		Recorder recorder = record(form);
		input.set(second);

		assertEquals("An object switch notifies exactly once.", List.of(second), recorder.objects());
		assertSame("The form displays the object its input channel delivered.", second,
			form.getCurrentObject());
	}

	/**
	 * Cancelling keeps the displayed object, so the notification carries it: the fields drop the
	 * overlay values and show the base values again.
	 */
	public void testCancelNotifiesForSameObject() {
		TLObject object = new MockTLObject();

		FormControl form = newForm(object);
		newInput(form, object);
		form.enterEditMode();

		Recorder recorder = record(form);
		form.executeCancel();

		assertEquals("Leaving edit mode notifies for the displayed object.", List.of(object),
			recorder.objects());
		assertFalse("The edit session has ended.", form.isEditMode());
	}

	/**
	 * Saving keeps the displayed object, so the notification carries it.
	 *
	 * <p>
	 * The form is clean, which is the state a save can reach without a knowledge base: persisting
	 * overlay changes opens a transaction.
	 * </p>
	 */
	public void testSaveNotifiesForSameObject() {
		TLObject object = new MockTLObject();

		FormControl form = newForm(object);
		newInput(form, object);
		form.enterEditMode();

		Recorder recorder = record(form);
		form.executeSave();

		assertEquals("Leaving edit mode notifies for the displayed object.", List.of(object),
			recorder.objects());
		assertFalse("The edit session has ended.", form.isEditMode());
	}

	/**
	 * The deletion of the displayed object notifies once, reporting that the form displays nothing.
	 */
	public void testCurrentObjectDeleted() {
		TLObject object = new MockTLObject();

		FormControl form = newForm(object);
		newInput(form, object);
		form.enterEditMode();

		Recorder recorder = record(form);
		form.notifyChange(new DeletionEvent(object));

		assertEquals("The deletion notifies exactly once.", 1, recorder.objects().size());
		assertNull("The form displays nothing after its object was deleted.", recorder.objects().get(0));
		assertFalse("The edit session of the deleted object has ended.", form.isEditMode());
		assertNull("The form displays nothing after its object was deleted.", form.getCurrentObject());
	}

	/**
	 * A form holding changes blocks the object switch.
	 */
	public void testDirtyFormVetoesSwitch() {
		MockTLObject object = new MockTLObject();
		TLObject second = new MockTLObject();

		FormControl form = newForm(object);
		ViewChannel input = newInput(form, object);
		form.enterEditMode();
		form.registerParticipant(new DirtyParticipant());

		assertTrue("The registered participant holds changes.", form.isDirty());
		try {
			input.set(second);
			fail("A form holding changes blocks the object switch.");
		} catch (ChannelVetoException ex) {
			// Expected.
		}
		assertTrue("The vetoed switch left the edit session running.", form.isEditMode());
		assertSame("The vetoed switch left the channel at its object.", object, input.get());
	}

	/**
	 * A form whose object was deleted holds nothing to protect, so the object switch goes through.
	 */
	public void testInvalidObjectVetoesNothing() {
		MockTLObject object = new MockTLObject();
		TLObject second = new MockTLObject();

		FormControl form = newForm(object);
		ViewChannel input = newInput(form, object);
		form.enterEditMode();
		form.registerParticipant(new DirtyParticipant());

		object.setValid(false);

		assertFalse("The changes made to a deleted object cannot be kept.", form.isDirty());

		Recorder recorder = record(form);
		input.set(second);

		assertEquals("An object switch notifies exactly once.", List.of(second), recorder.objects());
		assertFalse("The edit session of the deleted object has ended.", form.isEditMode());
		assertSame("The form displays the object its input channel delivered.", second,
			form.getCurrentObject());
	}

	private static FormControl newForm(TLObject initialObject) {
		return new FormControl(new DefaultReactContext("", "test", new SSEUpdateQueue(),
				new ReactWindowRegistry("test")), initialObject,
			"no model", NoTokenHandling.INSTANCE);
	}

	/**
	 * Binds the given form to an input channel that already delivers the form's initial object.
	 */
	private static ViewChannel newInput(FormControl form, TLObject initialObject) {
		ViewChannel input = new DefaultViewChannel("input");
		input.set(initialObject);
		form.setInputChannel(input);
		return input;
	}

	private static Recorder record(FormControl form) {
		Recorder recorder = new Recorder();
		form.addFormModelListener(recorder);
		return recorder;
	}

	/**
	 * {@link FormModelListener} recording the object the form displays at the time of each
	 * notification.
	 */
	private static class Recorder implements FormModelListener {

		private final List<TLObject> _objects = new ArrayList<>();

		@Override
		public void onFormStateChanged(FormModel source) {
			_objects.add(source.getCurrentObject());
		}

		/**
		 * The displayed objects, one entry per {@link #onFormStateChanged(FormModel)} call.
		 */
		public List<TLObject> objects() {
			return _objects;
		}
	}

	/**
	 * {@link ModelChangeEvent} reporting the deletion of a single object.
	 */
	private static class DeletionEvent implements ModelChangeEvent {

		private final TLObject _deleted;

		/**
		 * Creates a {@link DeletionEvent}.
		 *
		 * @param deleted
		 *        The deleted object.
		 */
		public DeletionEvent(TLObject deleted) {
			_deleted = deleted;
		}

		@Override
		public ChangeType getChange(TLObject existingObject) {
			return existingObject == _deleted ? ChangeType.DELETED : ChangeType.NONE;
		}

		@Override
		public Stream<? extends TLObject> getUpdated() {
			return Stream.empty();
		}

		@Override
		public Stream<? extends TLObject> getUpdated(TLStructuredType type) {
			return Stream.empty();
		}

		@Override
		public Stream<? extends TLObject> getCreated() {
			return Stream.empty();
		}

		@Override
		public Stream<? extends TLObject> getCreated(TLStructuredType type) {
			return Stream.empty();
		}

		@Override
		public Stream<? extends TLObject> getDeleted() {
			return Stream.of(_deleted);
		}

		@Override
		public Stream<? extends TLObject> getDeleted(TLStructuredType type) {
			return Stream.of(_deleted);
		}
	}

	/**
	 * {@link FormParticipant} that reports unsaved changes; nothing else of it is used by these
	 * tests.
	 */
	private static class DirtyParticipant implements FormParticipant {

		@Override
		public boolean validate() {
			return true;
		}

		@Override
		public void persist(Transaction tx) {
			// Nothing to persist.
		}

		@Override
		public void cancel() {
			// Nothing to cancel.
		}

		@Override
		public void revealAll() {
			// No hidden errors.
		}

		@Override
		public boolean isDirty() {
			return true;
		}
	}

	/**
	 * Object to display in the form; no attribute of it is read by these tests.
	 */
	private static class MockTLObject extends TransientObject {

		private boolean _valid = true;

		@Override
		public boolean tValid() {
			return _valid;
		}

		/**
		 * Sets whether this object still exists.
		 */
		public void setValid(boolean valid) {
			_valid = valid;
		}
	}

}
