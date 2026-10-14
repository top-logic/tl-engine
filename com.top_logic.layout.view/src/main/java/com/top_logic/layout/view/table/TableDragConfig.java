/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.List;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.layout.view.dnd.DragConfig;
import com.top_logic.layout.view.element.TableElement;
import com.top_logic.layout.view.element.TreeTableElement;

/**
 * Configuration of the {@code <drag>} of a {@link TableElement} or a {@link TreeTableElement}: that
 * its rows may be dragged, the {@link #getKind() kind} of such a drag, and when they may be dragged.
 *
 * <p>
 * The {@link #getExecutability() executability} rules decide for the table as a whole over the
 * value of the {@link #getInput() input} channel, and are followed live: while they refuse, no row
 * can be dragged. The {@link #getRowExecutability() row executability} rules decide for each row
 * separately, with the row as their input.
 * </p>
 */
public interface TableDragConfig extends DragConfig {

	/** Configuration name for {@link #getRowExecutability()}. */
	String ROW_EXECUTABILITY = "row-executability";

	/**
	 * Rules deciding which rows may be dragged, each row being the input they decide over.
	 *
	 * <p>
	 * A row the rules refuse offers no drag, and a drag of a selection including such a row is
	 * refused as a whole. Empty (default) lets every row be dragged while the table-wide
	 * {@link #getExecutability() executability} allows dragging at all.
	 * </p>
	 */
	@Name(ROW_EXECUTABILITY)
	@EntryTag(RULE)
	List<PolymorphicConfiguration<? extends ViewExecutabilityRule>> getRowExecutability();

}
