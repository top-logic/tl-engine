/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import static com.top_logic.knowledge.service.KBUtils.*;
import static com.top_logic.model.search.expr.I18NConstants.*;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.layout.view.form.TLObjectOverlay;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Tests that {@link TLObjectOverlay#apply()} writes a changed attribute only with the right to
 * modify it.
 */
public class TestTLObjectOverlayWriteRights extends AbstractModelAccessTest {

	/**
	 * A user without any role on the object may not write it; nothing is written.
	 */
	public void testRefusedWithoutWriteRight() {
		TLStructuredTypePart name = part(PROJECT, NAME);
		TLObjectOverlay overlay = new TLObjectOverlay(_project);
		overlay.tUpdate(name, "changed");

		becomeUser(_roleless);
		inTransaction(() -> assertRefused(WRITE_PERMISSION_DENIED__OBJECT_ATTRIBUTE, overlay::apply));

		assertEquals("project", _project.tValue(name));
		assertTrue("A refused overlay keeps its changes.", overlay.isDirty());
	}

	/**
	 * A user allowed to write the object but not one of its changed attributes may write none of
	 * the changes.
	 */
	public void testRefusedAttributeWritesNothing() {
		TLStructuredTypePart name = part(PROJECT, NAME);
		TLStructuredTypePart secret = part(PROJECT, SECRET);
		TLObjectOverlay overlay = new TLObjectOverlay(_project);
		overlay.tUpdate(name, "changed");
		overlay.tUpdate(secret, "revealed");

		becomeUser(_responsible);
		inTransaction(() -> assertRefused(WRITE_PERMISSION_DENIED__OBJECT_ATTRIBUTE, overlay::apply));

		assertEquals("A writable attribute is not written when another one is refused.", "project",
			_project.tValue(name));
		assertNull(_project.tValue(secret));
		assertTrue(overlay.isChanged(name));
		assertTrue(overlay.isChanged(secret));
	}

	/**
	 * A user with the right to write the object writes its changes.
	 */
	public void testWrittenWithWriteRight() {
		TLStructuredTypePart name = part(PROJECT, NAME);
		TLObjectOverlay overlay = new TLObjectOverlay(_project);
		overlay.tUpdate(name, "changed");

		becomeUser(_responsible);
		inTransaction(overlay::apply);

		assertEquals("changed", _project.tValue(name));
		assertFalse(overlay.isDirty());
	}

	/**
	 * The super-user writes even an attribute no role may write.
	 */
	public void testWrittenByRoot() {
		TLStructuredTypePart secret = part(PROJECT, SECRET);
		TLObjectOverlay overlay = new TLObjectOverlay(_project);
		overlay.tUpdate(secret, "revealed");

		becomeUser(_root);
		inTransaction(overlay::apply);

		assertEquals("revealed", _project.tValue(secret));
	}

	/**
	 * An unchanged value needs no write right, even if the user may not write the attribute.
	 */
	public void testUnchangedValueNeedsNoRight() {
		TLStructuredTypePart name = part(PROJECT, NAME);
		TLObjectOverlay overlay = new TLObjectOverlay(_project);
		overlay.tUpdate(name, "project");

		becomeUser(_roleless);
		inTransaction(overlay::apply);

		assertEquals("project", _project.tValue(name));
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return suite(TestTLObjectOverlayWriteRights.class);
	}

}
