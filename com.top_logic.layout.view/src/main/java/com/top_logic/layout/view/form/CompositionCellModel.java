/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * The field of a composition of a row edited in a table: its value is the list of the parts of the
 * row, edited by a {@link RowSetEditSession} of its own.
 *
 * <p>
 * The session edits the parts within the edit session of the form editing the row: it starts with
 * this field and takes part in the form's save until the field is {@link #dispose() disposed},
 * which happens when the edit of the row ends. Its changes reach the row as the value of the
 * composition.
 * </p>
 *
 * @see CompositionEditing
 */
public class CompositionCellModel extends AttributeFieldModel {

	private final RowSetEditSession _session;

	/**
	 * Creates a {@link CompositionCellModel} and starts the edit session of the parts.
	 *
	 * @param row
	 *        The editing buffer of the row: its overlay, or the row itself if it is new.
	 * @param part
	 *        The composition of the row.
	 * @param form
	 *        The form whose edit session the row takes part in.
	 * @param editable
	 *        Whether the parts may be changed; otherwise they are displayed only.
	 */
	public CompositionCellModel(TLObject row, TLStructuredTypePart part, FormControl form, boolean editable) {
		super(row, part);
		AttributeRowSetBinding binding = new AttributeRowSetBinding(part.getName());
		binding.resolve(row);
		_session = new RowSetEditSession(RowSetOwner.ofRow(form, row), binding);
		_session.setEditable(editable);
		_session.start();

		// The session holds the parts as overlays: that is what the field starts with.
		refreshFromObject();
		setDefaultValue(getValue());
	}

	/**
	 * The edit session of the parts.
	 */
	public RowSetEditSession session() {
		return _session;
	}

	/**
	 * Whether the parts may be changed: the field must be editable, and so must be the
	 * {@link RowSetEditSession#isEditable() session} of the parts.
	 */
	@Override
	public boolean isEditable() {
		return super.isEditable() && _session.isEditable();
	}

	/**
	 * Ends the edit session of the parts, which then takes no part in saving the form any more.
	 */
	@Override
	public void dispose() {
		_session.end();
	}

}
