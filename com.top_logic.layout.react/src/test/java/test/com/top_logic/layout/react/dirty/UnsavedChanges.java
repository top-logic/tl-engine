/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.dirty;

import com.top_logic.layout.react.dirty.DirtyChannel;
import com.top_logic.layout.react.dirty.StateHandler;

/**
 * A {@link StateHandler} standing for the unsaved input of a form a test puts into a
 * {@link DirtyChannel}.
 *
 * <p>
 * Dirty from the moment it is created, and clean once it is {@link #executeSave() saved} or
 * {@link #executeDiscard() discarded} - which is what resolving the unsaved changes does to the
 * form it stands for.
 * </p>
 */
public class UnsavedChanges implements StateHandler {

	/** The description a test handler reports. */
	private static final String DESCRIPTION = "unsaved changes";

	private boolean _dirty = true;

	@Override
	public boolean isDirty() {
		return _dirty;
	}

	@Override
	public boolean hasErrors() {
		return false;
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
		return DESCRIPTION;
	}
}
