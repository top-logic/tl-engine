/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.form.validation;

import junit.framework.TestCase;

import com.top_logic.basic.StringID;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClassImpl;
import com.top_logic.element.meta.form.validation.FormValidationModel;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.layout.form.FormMember;
import com.top_logic.model.TLFormObjectBase;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TransientObject;

/**
 * Tests for {@link FormValidationModel}.
 */
public class TestFormValidationModel extends TestCase {

	/** Branch context of the trunk branch, the only branch mock objects live on. */
	private static final long TRUNK = 1L;

	/**
	 * Tests that isValid() returns true initially when no constraints exist.
	 */
	public void testEmptyModelIsValid() {
		FormValidationModel model = new FormValidationModel();
		assertTrue(model.isValid());
	}

	/**
	 * Tests that the overlay of an edited object is found, and that the overlay itself is reported
	 * as its own overlay.
	 *
	 * <p>
	 * An overlay shares the identity of the object it edits, so an equality-based lookup would
	 * answer the edited object itself instead of its overlay.
	 * </p>
	 */
	public void testOverlayLookup() {
		FormValidationModel model = new FormValidationModel();
		MockObject base = new MockObject(objectKey("o1"));
		MockOverlay overlay = new MockOverlay(base);
		model.addOverlay(overlay, base);

		assertSame(overlay, model.getExistingOverlay(base));
		assertSame(overlay, model.getExistingOverlay(overlay));
	}

	/**
	 * Tests that a created object without a persistent base is reported as its own overlay.
	 */
	public void testCreatedObjectIsItsOwnOverlay() {
		FormValidationModel model = new FormValidationModel();
		MockObject created = new MockObject(null);
		model.addOverlay(created, null);

		assertSame(created, model.getExistingOverlay(created));
	}

	/**
	 * Tests that removing an overlay does not remove the object it edits.
	 */
	public void testRemoveDoesNotMatchEditedObject() {
		FormValidationModel model = new FormValidationModel();
		MockObject base = new MockObject(objectKey("o1"));
		MockOverlay overlay = new MockOverlay(base);
		model.addOverlay(overlay, base);

		model.removeOverlay(base);
		assertSame("Removing the edited object must not drop its overlay.", overlay,
			model.getExistingOverlay(base));

		model.removeOverlay(overlay);
		assertNull(model.getExistingOverlay(base));
	}

	/**
	 * Creates an {@link ObjectKey} identifying a mock object.
	 */
	private static ObjectKey objectKey(String id) {
		return new DefaultObjectKey(TRUNK, Revision.CURRENT_REV, new MOClassImpl("MockType"),
			StringID.valueOf(id));
	}

	/**
	 * Minimal mock {@link TLObject} without a type, so that no model infrastructure is needed.
	 */
	private static class MockObject extends TransientObject {

		private final ObjectKey _id;

		/**
		 * Creates a {@link MockObject} identified by the given key, or without identity for
		 * <code>null</code>.
		 */
		MockObject(ObjectKey id) {
			_id = id;
		}

		@Override
		public ObjectKey tId() {
			return _id;
		}
	}

	/**
	 * Minimal mock editing buffer sharing the identity of the object it edits, as the productive
	 * overlay implementations do.
	 */
	private static class MockOverlay extends TransientObject implements TLFormObjectBase {

		private final TLObject _base;

		/**
		 * Creates a {@link MockOverlay} for the given object.
		 */
		MockOverlay(TLObject base) {
			_base = base;
		}

		@Override
		public ObjectKey tId() {
			return _base.tId();
		}

		@Override
		public boolean isCreate() {
			return false;
		}

		@Override
		public TLObject getEditedObject() {
			return _base;
		}

		@Override
		public String getDomain() {
			return null;
		}

		@Override
		public Object getFieldValue(TLStructuredTypePart attribute) {
			return _base.tValue(attribute);
		}

		@Override
		public FormMember getField(TLStructuredTypePart attribute) {
			return null;
		}

		@Override
		public Object getBaseValue(TLStructuredTypePart attribute) {
			return _base.tValue(attribute);
		}

		@Override
		public Object defaultValue(TLStructuredTypePart part) {
			return _base.tValue(part);
		}
	}
}
