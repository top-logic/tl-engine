/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.dnd;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.view.command.ExecutabilityConfig;
import com.top_logic.model.util.TLModelPartRef;

/**
 * Configuration of the {@code <drag>} of an element: that its items may be dragged, what they are
 * announced as, and when they may be dragged.
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

	/** Configuration name for {@link #getType()}. */
	String TYPE = "type";

	/**
	 * The type the dragged items are announced as, which a {@link DropConfig#getAccept() drop}
	 * accepts them by.
	 *
	 * <p>
	 * Unset (default), the element derives the type from what it displays; an element that cannot
	 * say what its items are reports this as a configuration error.
	 * </p>
	 */
	@Name(TYPE)
	TLModelPartRef getType();

}
