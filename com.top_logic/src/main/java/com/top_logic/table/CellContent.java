/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

import com.top_logic.layout.form.model.FieldModel;

/**
 * UI-neutral description of a cell's content produced by a {@link CellRenderer}.
 *
 * <p>
 * Kept toolkit-agnostic on purpose: the model tier describes <em>what</em> to show; a UI
 * adapter (e.g. the React tier) turns a {@link CellContent} into a concrete control. The
 * {@link Raw} variant is an escape hatch carrying a UI-tier-specific payload for cells
 * that need bespoke rendering.
 * </p>
 */
public sealed interface CellContent {

	/**
	 * Plain text content.
	 *
	 * @param text
	 *        The text to display.
	 */
	record Text(String text) implements CellContent {
		// Pure value type.
	}

	/**
	 * Labeled content with an optional tooltip, icon and CSS class.
	 *
	 * @param text
	 *        The display label.
	 * @param tooltip
	 *        Optional tooltip text, or {@code null}.
	 * @param icon
	 *        Optional icon reference (e.g. a theme image name), or {@code null}.
	 * @param cssClass
	 *        Optional CSS class, or {@code null}.
	 */
	record Labeled(String text, String tooltip, String icon, String cssClass) implements CellContent {
		// Pure value type.
	}

	/**
	 * An editable cell backed by a {@link FieldModel} (inline editing).
	 *
	 * @param field
	 *        The field model holding the editable value.
	 */
	record Editable(FieldModel field) implements CellContent {
		// Pure value type.
	}

	/**
	 * An empty cell.
	 */
	record Empty() implements CellContent {
		// Pure value type.
	}

	/**
	 * Escape hatch carrying a UI-tier-specific payload for bespoke rendering.
	 *
	 * @param payload
	 *        The UI-tier-specific rendering payload.
	 * @param tooltip
	 *        Optional tooltip text, or {@code null}.
	 */
	record Raw(Object payload, String tooltip) implements CellContent {

		/**
		 * Creates a {@link Raw} cell without a {@link #tooltip()}.
		 *
		 * @param payload
		 *        The UI-tier-specific rendering payload.
		 */
		public Raw(Object payload) {
			this(payload, null);
		}

	}

	/**
	 * A tooltip distinct from the cell's text, or {@code null}.
	 *
	 * <p>
	 * What the cell says over and above what it displays: the text a cell whose value is an icon or
	 * a button reads as, the full text of a value the cell abbreviates. A cell that has nothing to
	 * add answers {@code null} - the displayed text is then what the UI tier offers where it does
	 * not fit.
	 * </p>
	 */
	default String tooltip() {
		return null;
	}

	/** Shared {@link Empty} instance. */
	CellContent EMPTY = new Empty();

	/**
	 * Plain {@link Text} content.
	 */
	static CellContent text(String text) {
		return new Text(text);
	}

	/**
	 * Labeled content with only a display label.
	 */
	static CellContent label(String text) {
		return new Labeled(text, null, null, null);
	}

	/**
	 * The shared {@link #EMPTY empty} content.
	 */
	static CellContent empty() {
		return EMPTY;
	}

}
