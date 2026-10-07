/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.view.form.BufferSave;
import com.top_logic.layout.view.form.EditLevel;
import com.top_logic.layout.view.form.TLObjectOverlay;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests editing nested compositions in {@link EditLevel}s, a nested level per dialog, and storing
 * the buffers of the root level with {@link BufferSave}.
 *
 * <p>
 * The fixture: the project holds the steps {@link #_s1} and {@link #_s2}; {@link #_s1} holds the
 * substeps {@link #_s11} and {@link #_s12}; {@link #_s11} holds the substep {@link #_s111}.
 * </p>
 */
public class TestEditLevel extends AbstractModelAccessTest {

	private static final String RENAMED = "renamed";

	private static final String CREATED = "created";

	private TLObject _s1;

	private TLObject _s2;

	private TLObject _s11;

	private TLObject _s12;

	private TLObject _s111;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			_s111 = step("s111");
			_s11 = step("s11", _s111);
			_s12 = step("s12");
			_s1 = step("s1", _s11, _s12);
			_s2 = step("s2");
			_project.tUpdateByName(STEPS, List.of(_s1, _s2));
			tx.commit();
		}
		becomeUser(_responsible);
	}

	private static TLObject step(String name, TLObject... substeps) {
		TLObject result = DynamicModelService.getFactoryFor(MODULE).createObject(type(STEP));
		result.tUpdateByName(NAME, name);
		result.tUpdateByName(SUBSTEPS, List.of(substeps));
		return result;
	}

	/**
	 * Changes on three levels of nested dialogs - a part removed, a part's attribute changed, a
	 * part created below it - are stored with the root, and only the touched objects are written.
	 */
	public void testNestedEditStoresTouchedObjects() {
		EditLevel root = new EditLevel();
		TLObject project = root.buffer(_project);

		EditLevel steps = root.nested();
		TLObject s1 = steps.buffer(_s1);

		EditLevel substeps = steps.nested();
		TLObject s11 = substeps.buffer(_s11);
		s11.tUpdateByName(NAME, RENAMED);

		EditLevel subsubsteps = substeps.nested();
		TLObject created = subsubsteps.create(type(STEP), s11);
		created.tUpdateByName(NAME, CREATED);
		subsubsteps.commit(s11, part(SUBSTEPS), List.of(_s111, created));

		// The second substep is removed.
		substeps.commit(s1, part(SUBSTEPS), List.of(s11));
		steps.commit(project, stepsPart(), List.of(s1, _s2));

		// Nothing is stored before the root is saved.
		assertEquals("s11", _s11.tValueByName(NAME));
		assertEquals(List.of(_s11, _s12), _s1.tValueByName(SUBSTEPS));

		long s2Update = lastUpdate(_s2);
		long s111Update = lastUpdate(_s111);
		save(project);

		assertEquals(RENAMED, _s11.tValueByName(NAME));
		assertEquals(List.of(_s11), _s1.tValueByName(SUBSTEPS));
		assertFalse("A removed part is deleted.", _s12.tValid());
		List<?> stored = (List<?>) _s11.tValueByName(SUBSTEPS);
		assertEquals(2, stored.size());
		assertSame(_s111, stored.get(0));
		TLObject storedCreated = (TLObject) stored.get(1);
		assertFalse(storedCreated.tTransient());
		assertEquals(CREATED, storedCreated.tValueByName(NAME));
		assertEquals(List.of(_s1, _s2), _project.tValueByName(STEPS));

		assertEquals("Untouched objects are not written.", s2Update, lastUpdate(_s2));
		assertEquals("Untouched objects are not written.", s111Update, lastUpdate(_s111));
	}

	/**
	 * Cancelling a nested dialog drops its changes, whatever its own nested dialogs confirmed.
	 */
	public void testCancelDropsNestedChanges() {
		EditLevel root = new EditLevel();
		TLObject project = root.buffer(_project);

		EditLevel steps = root.nested();
		TLObject s1 = steps.buffer(_s1);
		EditLevel substeps = steps.nested();
		TLObject s11 = substeps.buffer(_s11);
		s11.tUpdateByName(NAME, RENAMED);
		EditLevel subsubsteps = substeps.nested();
		subsubsteps.commit(s11, part(SUBSTEPS), List.of());
		// The substeps dialog is cancelled: s1 is not changed.
		assertFalse(((TLObjectOverlay) s1).isChanged(part(SUBSTEPS)));
		steps.commit(project, stepsPart(), List.of(s1, _s2));

		// The steps dialog is cancelled in a second round.
		EditLevel steps2 = root.nested();
		TLObject s2 = steps2.buffer(_s2);
		s2.tUpdateByName(NAME, RENAMED);

		assertFalse(((TLObjectOverlay) project).isDirty());

		save(project);
		assertEquals("s11", _s11.tValueByName(NAME));
		assertEquals(List.of(_s111), _s11.tValueByName(SUBSTEPS));
		assertEquals("s2", _s2.tValueByName(NAME));
		assertTrue(_s12.tValid());
	}

	/**
	 * A part removed in a cancelled dialog is still there, with the changes confirmed for it before.
	 */
	public void testRemoveAndCancelKeepsPart() {
		EditLevel root = new EditLevel();
		TLObject project = root.buffer(_project);

		// Confirmed: the second substep is renamed.
		EditLevel steps = root.nested();
		TLObject s1 = steps.buffer(_s1);
		EditLevel substeps = steps.nested();
		TLObject s12 = substeps.buffer(_s12);
		s12.tUpdateByName(NAME, RENAMED);
		substeps.commit(s1, part(SUBSTEPS), List.of(_s11, s12));
		steps.commit(project, stepsPart(), List.of(s1, _s2));

		// Cancelled: the second substep is removed in a confirmed nested dialog.
		EditLevel steps2 = root.nested();
		TLObject s1Again = steps2.buffer(s1);
		EditLevel substeps2 = steps2.nested();
		substeps2.commit(s1Again, part(SUBSTEPS), List.of(_s11));
		assertEquals(List.of(_s11), s1Again.tValueByName(SUBSTEPS));

		save(project);
		assertTrue(_s12.tValid());
		assertEquals(RENAMED, _s12.tValueByName(NAME));
		assertEquals(List.of(_s11, _s12), _s1.tValueByName(SUBSTEPS));
	}

	/**
	 * Reopening a dialog on a confirmed buffer edits on top of it and transfers the changes into
	 * it on OK, without stacking overlays; a cancelled reopening leaves the confirmed buffer and
	 * the buffers below it as they were.
	 */
	public void testReopenedDialog() {
		EditLevel root = new EditLevel();
		TLObject project = root.buffer(_project);

		EditLevel steps = root.nested();
		TLObject s1 = steps.buffer(_s1);
		EditLevel substeps = steps.nested();
		TLObject s11 = substeps.buffer(_s11);
		s11.tUpdateByName(NAME, "first");
		substeps.commit(s1, part(SUBSTEPS), List.of(s11, _s12));
		steps.commit(project, stepsPart(), List.of(s1, _s2));

		// Reopened and confirmed: the change goes into the buffer of the root level.
		EditLevel steps2 = root.nested();
		TLObject s1Again = steps2.buffer(s1);
		assertNotSame(s1, s1Again);
		s1Again.tUpdateByName(NAME, RENAMED);
		steps2.commit(project, stepsPart(), List.of(s1Again, _s2));
		List<?> stepList = (List<?>) project.tValueByName(STEPS);
		assertSame("The confirmed buffer stands in the list, not an overlay of it.", s1, stepList.get(0));
		assertEquals(RENAMED, s1.tValueByName(NAME));

		// Reopened with a nested change of a confirmed buffer, and cancelled.
		EditLevel steps3 = root.nested();
		TLObject s1Third = steps3.buffer(s1);
		EditLevel substeps3 = steps3.nested();
		TLObject s11Again = substeps3.buffer(s11);
		s11Again.tUpdateByName(NAME, "second");
		substeps3.commit(s1Third, part(SUBSTEPS), List.of(s11Again, _s12));
		// steps3 is cancelled.
		assertEquals("first", s11.tValueByName(NAME));

		save(project);
		assertEquals(RENAMED, _s1.tValueByName(NAME));
		assertEquals("first", _s11.tValueByName(NAME));
	}

	/**
	 * A reference that is no composition to a new object refers to the stored object after saving.
	 */
	public void testReferenceToNewObject() {
		EditLevel root = new EditLevel();
		TLObject project = root.buffer(_project);

		EditLevel steps = root.nested();
		TLObject created = steps.create(type(STEP), project);
		created.tUpdateByName(NAME, CREATED);
		TLObject s2 = steps.buffer(_s2);
		s2.tUpdateByName(PREDECESSOR, created);
		steps.commit(project, stepsPart(), List.of(_s1, s2, created));

		save(project);
		List<?> stored = (List<?>) _project.tValueByName(STEPS);
		assertEquals(3, stored.size());
		TLObject storedCreated = (TLObject) stored.get(2);
		assertFalse(storedCreated.tTransient());
		assertSame(storedCreated, _s2.tValueByName(PREDECESSOR));
	}

	/**
	 * A new object the user may not create is refused, and nothing is stored.
	 */
	public void testCreationRefused() {
		becomeUser(_roleless);
		EditLevel root = new EditLevel();
		TLObject project = root.buffer(_project);
		EditLevel steps = root.nested();
		TLObject created = steps.create(type(STEP), project);
		steps.commit(project, stepsPart(), List.of(_s1, _s2, created));

		assertRefused(com.top_logic.element.model.copy.I18NConstants.ERROR_PERSIST_PERMISSION_DENIED__TYPE,
			() -> save(project));
		assertEquals(List.of(_s1, _s2), _project.tValueByName(STEPS));
	}

	/**
	 * A change of an attribute the user may not write is refused, and nothing is stored.
	 */
	public void testWriteRefused() {
		EditLevel root = new EditLevel();
		TLObject project = root.buffer(_project);
		EditLevel steps = root.nested();
		TLObject s1 = steps.buffer(_s1);
		s1.tUpdateByName(NAME, RENAMED);
		TLObject s2 = steps.buffer(_s2);
		s2.tUpdateByName(SECRET, "forbidden");
		steps.commit(project, stepsPart(), List.of(s1, s2));

		try {
			save(project);
			fail("Writing the secret of a step must be refused.");
		} catch (TopLogicException ex) {
			// Expected.
		}
		assertEquals("s1", _s1.tValueByName(NAME));
		assertEquals(RENAMED, s1.tValueByName(NAME));
	}

	private static void save(TLObject buffer) {
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			BufferSave.save(buffer);
			tx.commit();
		}
	}

	private static long lastUpdate(TLObject object) {
		return object.tHandle().getLastUpdate();
	}

	private static TLStructuredTypePart part(String name) {
		return type(STEP).getPartOrFail(name);
	}

	private static TLStructuredTypePart stepsPart() {
		return type(PROJECT).getPartOrFail(STEPS);
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return suite(TestEditLevel.class);
	}

}
