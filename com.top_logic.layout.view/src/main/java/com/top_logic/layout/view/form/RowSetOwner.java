/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.model.TLObject;

/**
 * The object holding a row set edited in a {@link RowSetEditSession}, together with the form whose
 * edit session the row set takes part in.
 *
 * <p>
 * The owner is either the object of the form itself ({@link #ofForm(FormControl)}) - a composition
 * table within a form - or a row of a row set the form edits ({@link #ofRow(FormControl, TLObject)})
 * - the parts of a composition of a table row. Either way, the row set is saved and discarded with
 * the form.
 * </p>
 */
public interface RowSetOwner {

	/**
	 * The form whose edit session the row set takes part in: the form validates, saves, and
	 * cancels the row set together with everything else it edits.
	 */
	FormControl form();

	/**
	 * The editing buffer of the owner: the object the row set is read from and written to while
	 * the edit session runs, or {@code null} if the owner is not being edited.
	 *
	 * <p>
	 * A {@link TLObjectOverlay} for a persistent owner, the owner itself for a transient one.
	 * </p>
	 */
	TLObject object();

	/**
	 * The object the {@link #object() editing buffer} stands for: the object new rows are created
	 * within, and which receives the row set when the form is saved. {@code null} if the owner is
	 * not being edited.
	 */
	default TLObject base() {
		TLObject object = object();
		return object instanceof TLObjectOverlay overlay ? overlay.getBase() : object;
	}

	/**
	 * Whether the owner is a transient object, so its rows stay transient as well and become
	 * persistent together with the owner.
	 */
	default boolean isTransient() {
		TLObject base = base();
		return base != null && base.tTransient();
	}

	/**
	 * The owner of a row set held by the object of the given form.
	 *
	 * @param form
	 *        The form editing the owner. Its {@link FormControl#getOverlay() overlay} is the editing
	 *        buffer of the owner.
	 */
	static RowSetOwner ofForm(FormControl form) {
		return new RowSetOwner() {
			@Override
			public FormControl form() {
				return form;
			}

			@Override
			public TLObject object() {
				return form.getOverlay();
			}
		};
	}

	/**
	 * The owner of a row set held by a row the given form edits, the row of a composition table
	 * for instance.
	 *
	 * @param form
	 *        The form whose edit session the row takes part in.
	 * @param row
	 *        The editing buffer of the row: its {@link TLObjectOverlay}, or the row itself if it is
	 *        a transient object created in the edit session.
	 */
	static RowSetOwner ofRow(FormControl form, TLObject row) {
		return new RowSetOwner() {
			@Override
			public FormControl form() {
				return form;
			}

			@Override
			public TLObject object() {
				return row;
			}
		};
	}

}
