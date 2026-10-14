/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.view.dnd.DropConfig;
import com.top_logic.layout.view.element.TableElement;
import com.top_logic.layout.view.element.TreeTableElement;

/**
 * Configuration of one {@code <drop>} of a {@link TableElement} or a {@link TreeTableElement}: a
 * drop made on the table as a whole, on a single row of it, or between two rows.
 *
 * <p>
 * The {@link #getTargetChannel() target channel}, the {@link #getTargetExecutability() target
 * executability} rules and the {@link #getRefuseIf() refusal function} get the row dropped on for a
 * drop on a {@link DropTargetMode#ROW row}; a drop on the {@link DropTargetMode#TABLE table} has no
 * target row. An {@link DropTargetMode#ORDERED insertion} has the row it inserts before instead,
 * which the {@link #getBeforeChannel() before channel} and the refusal function get; in a tree
 * table also the row it inserts under, which the {@link #getParentChannel() parent channel} gets.
 * </p>
 */
@TagName(DropConfig.TAG_NAME)
public interface TableDropConfig extends DropConfig {

	/** Configuration name for {@link #getTarget()}. */
	String TARGET = "target";

	/**
	 * Whether the table as a whole, a single row, or a place between two rows is the target of this
	 * drop.
	 */
	@Name(TARGET)
	DropTargetMode getTarget();

}
