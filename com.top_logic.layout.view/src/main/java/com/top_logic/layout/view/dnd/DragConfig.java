/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.dnd;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.layout.view.command.ExecutabilityConfig;

/**
 * Configuration of the {@code <drag>} of an element: that its items may be dragged, the kind of
 * such a drag, and when they may be dragged.
 *
 * <p>
 * The {@link #getExecutability() executability} rules decide for the element as a whole over the
 * value of the {@link #getInput() input} channel, and are followed live: while they refuse, no item
 * can be dragged. Which single item may be dragged is decided by rules the element declares for its
 * kind of item.
 * </p>
 *
 * @see DragSourceBinding
 */
public interface DragConfig extends ExecutabilityConfig {

	/** Configuration name for {@link #getKind()}. */
	String KIND = "kind";

	/**
	 * The kind of a drag of the items, which a {@link DropConfig#getAccept() drop} accepts it by.
	 *
	 * <p>
	 * A free name chosen by the application, such as {@code ticket}, compared literally: a drop
	 * listing it in its {@link DropConfig#getAccept() accepted kinds} takes the drag. Unset (default),
	 * the drag has no kind and is taken only by a drop accepting every drag.
	 * </p>
	 */
	@Name(KIND)
	@Nullable
	String getKind();

}
