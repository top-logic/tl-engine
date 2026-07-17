/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.List;

import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Strategy defining the row-set semantics of a table edited within a form.
 *
 * <p>
 * A binding answers the four questions that distinguish editable tables from each other: which
 * objects are the rows, how a new row comes into existence (and which types are offered), what
 * removing a row means at commit time, and what is written back when the form is saved. The
 * cell-editing mechanics (row overlays, per-cell field models, validation) are independent of the
 * binding and implemented by {@link RowSetEditor}.
 * </p>
 *
 * <p>
 * Implementations: {@link AttributeRowSetBinding} derives everything from a reference attribute of
 * the form object (composition references delete removed rows, plain references only unlink them);
 * {@link QueryRowSetBinding} computes rows from an arbitrary query and takes its membership
 * semantics from explicit configuration.
 * </p>
 */
public interface RowSetBinding {

	/**
	 * How removing a row from the table affects the removed object when the form is saved.
	 */
	enum RemoveMode {

		/** Rows cannot be removed. */
		NONE,

		/** The removed object is only taken out of the row set; the object itself is kept. */
		UNLINK,

		/** The removed object is deleted. */
		DELETE;
	}

	/**
	 * Re-resolves this binding against the given form object.
	 *
	 * @param formObject
	 *        The form's current base object, or {@code null} if the form has none. Bindings that
	 *        are not anchored at an object (queries) may still resolve successfully.
	 * @return Whether the binding is available, i.e. {@link #readRows(TLObject)} can produce rows.
	 */
	boolean resolve(TLObject formObject);

	/**
	 * The type of the row objects, or {@code null} if it cannot be determined.
	 *
	 * <p>
	 * Used to resolve column labels and as the default create type.
	 * </p>
	 */
	TLClass getRowType();

	/**
	 * The attribute of the form object this binding is anchored at, or {@code null} for bindings
	 * without a bound attribute.
	 *
	 * <p>
	 * When non-{@code null}, the {@link RowSetEditor} re-evaluates constraints on this attribute
	 * whenever the row set changes.
	 * </p>
	 */
	TLStructuredTypePart getBoundPart();

	/**
	 * The current row objects, read from the given object.
	 *
	 * @param object
	 *        The object to read from (the form's base object or its overlay), or {@code null} for
	 *        bindings without a bound attribute.
	 */
	List<TLObject> readRows(TLObject object);

	/**
	 * The concrete types offered when creating a new row; empty if row creation is not offered.
	 */
	List<TLClass> getCreateTypes();

	/**
	 * The remove semantics of this binding.
	 */
	RemoveMode getRemoveMode();

	/**
	 * Publishes a membership change (row added or removed) of the running edit session to the form
	 * overlay, so that constraints on the {@link #getBoundPart() bound attribute} observe the
	 * current row set.
	 *
	 * <p>
	 * Bindings without a bound attribute do nothing here; membership changes are buffered in the
	 * edit session only.
	 * </p>
	 *
	 * @param form
	 *        The form whose overlay buffers the edit session.
	 * @param currentRows
	 *        The current row objects (row overlays and transient new objects).
	 */
	void updateMembership(FormControl form, List<TLObject> currentRows);

	/**
	 * Commits the row set within the given transaction.
	 *
	 * <p>
	 * Attribute bindings write the persisted row list back to the bound attribute of the form
	 * overlay. All bindings apply their remove semantics to the objects taken out of the row set
	 * during the edit session.
	 * </p>
	 *
	 * @param tx
	 *        The open transaction the commit takes part in.
	 * @param form
	 *        The form whose overlay buffers the edit session.
	 * @param persistedRows
	 *        The current rows with overlays and transient creations replaced by their persistent
	 *        objects.
	 * @param originalRows
	 *        The persistent rows at edit-session start.
	 */
	void commit(Transaction tx, FormControl form, List<TLObject> persistedRows, List<TLObject> originalRows);

}
