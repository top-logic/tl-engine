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
