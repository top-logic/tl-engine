/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import com.top_logic.basic.LongID;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClassImpl;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.impl.TransientTLObjectImpl;
import com.top_logic.model.listen.ModelScope;

/**
 * Object with an identity that is not transient, which is what a form observes in its
 * {@link ModelScope}.
 *
 * <p>
 * The values are held in memory. {@link #delete()} makes the object invalid, as a deletion
 * committed to the knowledge base does.
 * </p>
 */
public class IdentifiedObject extends TransientTLObjectImpl {

	private static final MOClassImpl TABLE = new MOClassImpl("test");

	private static long _nextId = 1;

	private final ObjectKey _id =
		new DefaultObjectKey(1, Revision.CURRENT_REV, TABLE, LongID.valueOf(_nextId++));

	private boolean _deleted;

	/**
	 * Creates an {@link IdentifiedObject}.
	 *
	 * @param type
	 *        The type of the object.
	 */
	public IdentifiedObject(TLStructuredType type) {
		super(type, null);
	}

	@Override
	public ObjectKey tId() {
		return _id;
	}

	@Override
	public boolean tTransient() {
		return false;
	}

	@Override
	public boolean tValid() {
		return !_deleted && super.tValid();
	}

	/**
	 * Makes this object {@link #tValid() invalid}.
	 */
	public void delete() {
		_deleted = true;
	}
}
