/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * The field of a composition of a row edited in a table: its value is the list of the parts the
 * buffer of the row holds.
 *
 * <p>
 * The parts are edited in a dialog, on a level of its own on top of the {@link #getLevel() level}
 * the row is edited in, see {@link CompositionEditing}. A dialog that is open when the edit of the
 * row ends - the field is {@link #dispose() disposed} - is closed.
 * </p>
 */
public class CompositionCellModel extends AttributeFieldModel {

	private final FormControl _form;

	private final EditLevel _level;

	private final boolean _partsEditable;

	private final List<Runnable> _disposeActions = new ArrayList<>();

	/**
	 * Creates a {@link CompositionCellModel}.
	 *
	 * @param row
	 *        The buffer of the row.
	 * @param part
	 *        The composition of the row.
	 * @param form
	 *        The form the row is edited in.
	 * @param level
	 *        The level holding the buffer of the row.
	 * @param editable
	 *        Whether the parts may be changed; otherwise they are displayed only.
	 */
	public CompositionCellModel(TLObject row, TLStructuredTypePart part, FormControl form, EditLevel level,
			boolean editable) {
		super(row, part);
		_form = form;
		_level = level;
		_partsEditable = editable;
	}

	/**
	 * The form the row is edited in.
	 */
	public FormControl getForm() {
		return _form;
	}

	/**
	 * The level holding the buffer of the row, the level the dialog editing the parts edits on top
	 * of.
	 */
	public EditLevel getLevel() {
		return _level;
	}

	/**
	 * Whether the parts may be changed: the field must be editable, and the user must be allowed to
	 * change the composition.
	 */
	@Override
	public boolean isEditable() {
		return super.isEditable() && _partsEditable;
	}

	/**
	 * Runs the given action when the field is {@link #dispose() disposed}.
	 */
	public void addDisposeAction(Runnable action) {
		_disposeActions.add(action);
	}

	/**
	 * Removes an action added by {@link #addDisposeAction(Runnable)}.
	 */
	public void removeDisposeAction(Runnable action) {
		_disposeActions.remove(action);
	}

	/**
	 * Runs the actions of what still depends on the field, closing an open dialog for instance.
	 */
	@Override
	public void dispose() {
		for (Runnable action : new ArrayList<>(_disposeActions)) {
			action.run();
		}
		_disposeActions.clear();
	}

}
