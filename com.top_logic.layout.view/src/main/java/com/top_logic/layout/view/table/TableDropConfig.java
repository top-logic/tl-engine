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

/**
 * Configuration of one {@code <drop>} of a {@link TableElement}: a drop made either on the table
 * as a whole or on a single row of it.
 *
 * <p>
 * The {@link #getTargetChannel() target channel}, the {@link #getTargetExecutability() target
 * executability} rules and the {@link #getRefuseIf() refusal function} get the row dropped on for a
 * drop on a {@link DropTargetMode#ROW row}; a drop on the {@link DropTargetMode#TABLE table} has no
 * target row.
 * </p>
 */
@TagName(DropConfig.TAG_NAME)
public interface TableDropConfig extends DropConfig {

	/** Configuration name for {@link #getTarget()}. */
	String TARGET = "target";

	/**
	 * Whether the table as a whole or a single row is the target of this drop.
	 */
	@Name(TARGET)
	DropTargetMode getTarget();

}
