/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.form.AbstractCompositionControl;
import com.top_logic.layout.view.form.AttributeRowSetBinding;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.QueryRowSetBinding;
import com.top_logic.layout.view.form.RowSetBinding;
import com.top_logic.layout.view.form.RowSetBinding.RemoveMode;
import com.top_logic.layout.view.form.RowSetEditSession;
import com.top_logic.layout.view.form.RowSetOwner;
import com.top_logic.layout.view.form.TLObjectOverlay;
import com.top_logic.model.TLObject;

/**
 * Tests {@link RowSetEditSession}s editing the rows of a form object and, nested on top of it, the
 * parts of a row: new rows are saved together with their new parts, a nested session reaches the
 * row only when committed, and a display can come and go while a session runs.
 */
public class TestRowSetEditSession extends AbstractModelAccessTest {

	private static final String NEW_STEP = "new step";

	private static final String NEW_SUBSTEP = "new substep";

	private static final String OLD_SUBSTEP = "old substep";

	private static final String RENAMED_SUBSTEP = "renamed substep";

	/**
	 * A new row holding new parts in a composition of its own is saved together with its parts.
	 */
	public void testNewRowSavedWithItsParts() {
		becomeUser(_responsible);
		FormControl form = newForm();
		CompositionControl steps = new CompositionControl(form, STEPS);
		steps.init();
		assertTrue(form.enterEditMode());

		TLObject step = steps.addRow(row -> row.tUpdateByName(NAME, NEW_STEP));
		assertNotNull(step);
		RowSetEditSession substeps = nestedSession(form, steps, step);
		assertNotNull(substeps.addRow(row -> row.tUpdateByName(NAME, NEW_SUBSTEP)));
		substeps.commit();

		form.executeSave();

		List<?> savedSteps = (List<?>) _project.tValueByName(STEPS);
		assertEquals(1, savedSteps.size());
		TLObject savedStep = (TLObject) savedSteps.get(0);
		assertFalse(savedStep.tTransient());
		assertEquals(NEW_STEP, savedStep.tValueByName(NAME));
		TLObject savedSubstep = single(savedStep.tValueByName(SUBSTEPS));
		assertFalse(savedSubstep.tTransient());
		assertEquals(NEW_SUBSTEP, savedSubstep.tValueByName(NAME));
		assertSame(savedStep, savedSubstep.tContainer());
	}

	/**
	 * A nested session on an existing row edits the parts of the row on a level of its own: ended
	 * without commit it leaves the row as it was, committed its changes reach the row and are saved
	 * with the form.
	 */
	public void testNestedSessionOfExistingRow() {
		TLObject step = createStepWithSubstep();
		TLObject oldSubstep = single(step.tValueByName(SUBSTEPS));

		becomeUser(_responsible);
		FormControl form = newForm();
		CompositionControl steps = new CompositionControl(form, STEPS);
		steps.init();
		assertTrue(form.enterEditMode());
		TLObject stepRow = single(steps.session().currentRows());

		RowSetEditSession cancelled = nestedSession(form, steps, stepRow);
		single(cancelled.currentRows()).tUpdateByName(NAME, RENAMED_SUBSTEP);
		assertNotNull(cancelled.addRow(row -> row.tUpdateByName(NAME, NEW_SUBSTEP)));
		cancelled.end();
		assertEquals(List.of(oldSubstep), stepRow.tValueByName(SUBSTEPS));
		assertFalse(form.isDirty());

		RowSetEditSession substeps = nestedSession(form, steps, stepRow);
		TLObject substepBuffer = single(substeps.currentRows());
		assertSame(oldSubstep, ((TLObjectOverlay) substepBuffer).getBase());
		substepBuffer.tUpdateByName(NAME, RENAMED_SUBSTEP);
		assertNotNull(substeps.addRow(row -> row.tUpdateByName(NAME, NEW_SUBSTEP)));
		substeps.commit();

		assertTrue(form.isDirty());
		// Nothing is stored before the form is saved.
		assertEquals(List.of(oldSubstep), step.tValueByName(SUBSTEPS));
		assertEquals(OLD_SUBSTEP, oldSubstep.tValueByName(NAME));

		form.executeSave();

		List<?> savedSubsteps = (List<?>) step.tValueByName(SUBSTEPS);
		assertEquals(2, savedSubsteps.size());
		assertSame(oldSubstep, savedSubsteps.get(0));
		assertEquals(RENAMED_SUBSTEP, oldSubstep.tValueByName(NAME));
		TLObject created = (TLObject) savedSubsteps.get(1);
		assertFalse(created.tTransient());
		assertEquals(NEW_SUBSTEP, created.tValueByName(NAME));
	}

	/**
	 * A control displaying a session follows the changes of its rows, and disposing it keeps the
	 * session and its changes.
	 */
	public void testSessionOutlivesDisplay() {
		TLObject step = createStepWithSubstep();

		becomeUser(_responsible);
		FormControl form = newForm();
		CompositionControl steps = new CompositionControl(form, STEPS);
		steps.init();
		assertTrue(form.enterEditMode());
		RowSetEditSession substeps = nestedSession(form, steps, single(steps.session().currentRows()));

		CompositionControl display = new CompositionControl(substeps);
		display.init();
		assertEquals(1, display._shownRows);

		assertNotNull(display.addRow(row -> row.tUpdateByName(NAME, NEW_SUBSTEP)));
		assertEquals(2, display._shownRows);

		display.cleanupTree();
		assertTrue(substeps.isRunning());
		assertEquals(2, substeps.currentRows().size());

		substeps.commit();
		form.executeSave();
		assertEquals(2, ((List<?>) step.tValueByName(SUBSTEPS)).size());
	}

	/**
	 * Removing a row whose rows are deleted removes the rows that are parts of it as well, changed
	 * or not: they are deleted with their container, and the save neither writes them nor deletes
	 * them a second time.
	 */
	public void testRemovedContainerTakesItsPartsAlong() {
		TLObject step = createStepWithSubstep();
		TLObject substep = single(step.tValueByName(SUBSTEPS));

		becomeUser(_root);
		FormControl form = newForm();
		CompositionControl rows = stepsAndSubsteps(form, step, substep);
		List<TLObject> buffers = rows.session().currentRows();
		TLObject stepRow = buffers.get(0);
		TLObject substepRow = buffers.get(1);

		substepRow.tUpdateByName(NAME, RENAMED_SUBSTEP);
		rows.removeRow(stepRow);
		assertTrue(rows.session().currentRows().isEmpty());
		assertEquals(0, rows._shownRows);

		form.executeSave();

		assertFalse(form.isEditMode());
		assertFalse(step.tValid());
		assertFalse(substep.tValid());
	}

	/**
	 * Removing a container together with its parts, as removing all selected rows does, deletes
	 * each of them once.
	 */
	public void testRemoveContainerAndItsParts() {
		TLObject step = createStepWithSubstep();
		TLObject substep = single(step.tValueByName(SUBSTEPS));

		becomeUser(_root);
		FormControl form = newForm();
		CompositionControl rows = stepsAndSubsteps(form, step, substep);

		rows.removeRows(rows.session().currentRows());
		assertTrue(rows.session().currentRows().isEmpty());

		form.executeSave();

		assertFalse(form.isEditMode());
		assertFalse(step.tValid());
		assertFalse(substep.tValid());
	}

	/**
	 * A part whose container is kept is not taken along by the removal of another row.
	 */
	public void testRemovedPartKeepsContainer() {
		TLObject step = createStepWithSubstep();
		TLObject substep = single(step.tValueByName(SUBSTEPS));

		becomeUser(_root);
		FormControl form = newForm();
		CompositionControl rows = stepsAndSubsteps(form, step, substep);
		List<TLObject> buffers = rows.session().currentRows();

		rows.removeRow(buffers.get(1));
		assertEquals(List.of(buffers.get(0)), rows.session().currentRows());

		form.executeSave();

		assertTrue(step.tValid());
		assertFalse(substep.tValid());
		assertEquals(List.of(), step.tValueByName(SUBSTEPS));
	}

	/**
	 * A stored object adopted by a running session is edited through its buffer, and the change is
	 * stored with the form.
	 */
	public void testAdoptedRowSaved() {
		TLObject step = createStepWithSubstep();
		TLObject substep = single(step.tValueByName(SUBSTEPS));

		becomeUser(_root);
		FormControl form = newForm();
		CompositionControl rows = stepOnly(form, step);
		RowSetEditSession session = rows.session();

		TLObject substepRow = single(session.adoptRows(List.of(substep)));
		assertSame(substep, ((TLObjectOverlay) substepRow).getBase());
		assertEquals(2, session.currentRows().size());
		substepRow.tUpdateByName(NAME, RENAMED_SUBSTEP);
		assertTrue(form.isDirty());
		// Nothing is stored before the form is saved.
		assertEquals(OLD_SUBSTEP, substep.tValueByName(NAME));

		form.executeSave();

		assertFalse(form.isEditMode());
		assertTrue(substep.tValid());
		assertEquals(RENAMED_SUBSTEP, substep.tValueByName(NAME));
		assertEquals(List.of(substep), step.tValueByName(SUBSTEPS));
	}

	/**
	 * Adopting a row changes nothing: the session stays clean, its display is not rebuilt, and
	 * saving neither deletes nor changes the adopted row, although the binding deletes removed
	 * rows.
	 */
	public void testAdoptionIsNoChange() {
		TLObject step = createStepWithSubstep();
		TLObject substep = single(step.tValueByName(SUBSTEPS));

		becomeUser(_root);
		FormControl form = newForm();
		CompositionControl rows = stepOnly(form, step);
		RowSetEditSession session = rows.session();

		assertNotNull(single(session.adoptRows(List.of(substep))));
		assertFalse(session.isDirty());
		assertFalse(form.isDirty());
		assertEquals("Adoption must not rebuild the display.", 1, rows._shownRows);

		// A change of another row makes the save store something.
		session.currentRows().get(0).tUpdateByName(NAME, NEW_STEP + "!");
		form.executeSave();

		assertFalse(form.isEditMode());
		assertTrue(step.tValid());
		assertTrue(substep.tValid());
		assertEquals(OLD_SUBSTEP, substep.tValueByName(NAME));
		assertEquals(List.of(substep), step.tValueByName(SUBSTEPS));
	}

	/**
	 * Adopting an object that is a row already returns its row and adds nothing.
	 */
	public void testAdoptionIsIdempotent() {
		TLObject step = createStepWithSubstep();
		TLObject substep = single(step.tValueByName(SUBSTEPS));

		becomeUser(_root);
		FormControl form = newForm();
		RowSetEditSession session = stepOnly(form, step).session();
		TLObject stepRow = single(session.currentRows());

		assertNull(session.findRow(substep));
		TLObject substepRow = single(session.adoptRows(List.of(substep)));
		assertSame(substepRow, session.findRow(substep));
		assertEquals(List.of(substepRow), session.adoptRows(List.of(substep)));
		assertSame(substepRow, session.adoptRow(substep));
		assertEquals(List.of(stepRow, substepRow), session.adoptRows(List.of(step, substep)));
		assertEquals(List.of(stepRow, substepRow), session.currentRows());
		assertFalse(session.isDirty());
	}

	/**
	 * An adopted row removed from rows the binding deletes is deleted on save, and is not adopted
	 * again.
	 */
	public void testRemovedAdoptedRowDeleted() {
		TLObject step = createStepWithSubstep();
		TLObject substep = single(step.tValueByName(SUBSTEPS));

		becomeUser(_root);
		FormControl form = newForm();
		CompositionControl rows = stepOnly(form, step);
		RowSetEditSession session = rows.session();

		TLObject substepRow = single(session.adoptRows(List.of(substep)));
		rows.removeRow(substepRow);
		assertTrue(session.isDirty());
		assertNull(session.findRow(substep));
		assertEquals(List.of(), session.adoptRows(List.of(substep)));
		assertEquals(1, session.currentRows().size());

		form.executeSave();

		assertFalse(form.isEditMode());
		assertTrue(step.tValid());
		assertFalse(substep.tValid());
	}

	/**
	 * The parts of a removed row the binding deletes are not adopted: they are deleted with it.
	 */
	public void testPartOfRemovedRowNotAdopted() {
		TLObject step = createStepWithSubstep();
		TLObject substep = single(step.tValueByName(SUBSTEPS));

		becomeUser(_root);
		FormControl form = newForm();
		CompositionControl rows = stepOnly(form, step);
		RowSetEditSession session = rows.session();

		rows.removeRow(single(session.currentRows()));
		assertEquals(List.of(), session.adoptRows(List.of(substep)));
	}

	/**
	 * A violated constraint of an adopted row makes the rows invalid.
	 */
	public void testAdoptedRowValidated() {
		TLObject step = createStepWithSubstep();
		TLObject item = newPart(step, ITEMS, ITEM);

		becomeUser(_root);
		FormControl form = newForm();
		RowSetEditSession session = stepOnly(form, step).session();
		assertTrue(session.checkValid());

		TLObject itemRow = single(session.adoptRows(List.of(item)));
		assertTrue(session.checkValid());

		itemRow.tUpdateByName(NAME, null);
		assertFalse("The mandatory name of an adopted row is checked.", session.checkValid());

		itemRow.tUpdateByName(NAME, ITEM);
		assertTrue(session.checkValid());
	}

	/**
	 * A session that is not running adopts no rows.
	 */
	public void testAdoptionRequiresRunningSession() {
		TLObject step = createStepWithSubstep();

		becomeUser(_root);
		FormControl form = newForm();
		RowSetEditSession session = new RowSetEditSession(RowSetOwner.ofForm(form), stepOnlyBinding(step));
		assertFalse(session.isRunning());
		assertNull(session.findRow(step));
		try {
			session.adoptRows(List.of(step));
			fail("A session that is not running must refuse to adopt rows.");
		} catch (IllegalStateException ex) {
			// Expected.
		}
	}

	/**
	 * A table showing only the given step, deleting removed rows, in edit mode: further rows are
	 * adopted by the session.
	 */
	private CompositionControl stepOnly(FormControl form, TLObject step) {
		CompositionControl rows = new CompositionControl(form, stepOnlyBinding(step));
		rows.init();
		assertTrue(form.enterEditMode());
		assertEquals(1, rows.session().currentRows().size());
		assertEquals(1, rows._shownRows);
		return rows;
	}

	private static QueryRowSetBinding stepOnlyBinding(TLObject step) {
		return new QueryRowSetBinding(() -> List.of(step), type(STEP), List.of(), RemoveMode.DELETE);
	}

	/**
	 * A table of a step and its substep, deleting removed rows, in edit mode.
	 */
	private CompositionControl stepsAndSubsteps(FormControl form, TLObject step, TLObject substep) {
		CompositionControl rows = new CompositionControl(form,
			new QueryRowSetBinding(() -> List.of(step, substep), type(STEP), List.of(), RemoveMode.DELETE));
		rows.init();
		assertTrue(form.enterEditMode());
		assertEquals(2, rows.session().currentRows().size());
		return rows;
	}

	private TLObject createStepWithSubstep() {
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			TLObject step = DynamicModelService.getFactoryFor(MODULE).createObject(type(STEP));
			step.tUpdateByName(NAME, NEW_STEP);
			TLObject substep = DynamicModelService.getFactoryFor(MODULE).createObject(type(STEP));
			substep.tUpdateByName(NAME, OLD_SUBSTEP);
			step.tUpdateByName(SUBSTEPS, List.of(substep));
			_project.tUpdateByName(STEPS, List.of(step));
			tx.commit();
			return step;
		}
	}

	/**
	 * Starts a session editing the substeps of the given step row of the given steps, on top of the
	 * level the steps are edited in.
	 */
	private static RowSetEditSession nestedSession(FormControl form, CompositionControl steps, TLObject step) {
		AttributeRowSetBinding binding = new AttributeRowSetBinding(SUBSTEPS);
		assertTrue(binding.resolve(step));
		RowSetEditSession session =
			new RowSetEditSession(RowSetOwner.ofRow(form, step), binding, steps.session().level());
		session.start();
		assertTrue(session.isNested());
		assertTrue(session.isRunning());
		return session;
	}

	private static TLObject single(Object value) {
		List<?> list = (List<?>) value;
		assertEquals(1, list.size());
		return (TLObject) list.get(0);
	}

	private FormControl newForm() {
		return new FormControl(reactContext(), _project, "no model", NoTokenHandling.INSTANCE);
	}

	private static ReactContext reactContext() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	/**
	 * {@link AbstractCompositionControl} counting the rows it shows, without a presentation.
	 */
	private static class CompositionControl extends AbstractCompositionControl {

		int _shownRows = -1;

		CompositionControl(FormControl form, String attribute) {
			super(reactContext(), form, attribute, "TLPanel");
		}

		CompositionControl(FormControl form, RowSetBinding binding) {
			super(reactContext(), form, binding, "TLPanel");
		}

		CompositionControl(RowSetEditSession session) {
			super(reactContext(), session, "TLPanel");
		}

		@Override
		protected void buildContent(List<? extends TLObject> rows, boolean editMode) {
			_shownRows = rows.size();
		}

		@Override
		protected void refreshRows() {
			_shownRows = fieldModel().getCurrentList().size();
		}
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return suite(TestRowSetEditSession.class);
	}

}
