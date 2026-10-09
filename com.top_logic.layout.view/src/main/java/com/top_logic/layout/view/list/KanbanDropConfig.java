/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.list;

import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.view.dnd.DropConfig;

/**
 * Configuration of one {@code <drop>} of a {@link KanbanBoardElement}: a drop made on a column of
 * the board.
 *
 * <p>
 * The column value is the target of the drop: the {@link #getTargetChannel() target channel}, the
 * {@link #getTargetExecutability() target executability} rules and the {@link #getRefuseIf()
 * refusal function} get it.
 * </p>
 */
@TagName(DropConfig.TAG_NAME)
public interface KanbanDropConfig extends DropConfig {

	// The drop of a board column, a column being the only target a board has.

}
