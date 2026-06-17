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
	 */
	record Raw(Object payload) implements CellContent {
		// Pure value type.
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
