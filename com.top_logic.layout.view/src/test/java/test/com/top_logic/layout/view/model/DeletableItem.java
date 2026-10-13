/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.model;

import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TransientTLObjectImpl;

/**
 * Object without a type that a test can delete: after {@link #markDeleted()} it is no longer
 * {@link TLObject#tValid() valid}, as a persistent object deleted in another session.
 */
public class DeletableItem extends TransientTLObjectImpl {

	private final String _name;

	private boolean _deleted;

	/**
	 * Creates a {@link DeletableItem}.
	 *
	 * @param name
	 *        The name the item is printed with.
	 */
	public DeletableItem(String name) {
		super(null, null);
		_name = name;
	}

	/**
	 * Makes this object one that was deleted.
	 */
	public void markDeleted() {
		_deleted = true;
	}

	@Override
	public boolean tValid() {
		return !_deleted;
	}

	@Override
	public String toString() {
		return _name;
	}
}
