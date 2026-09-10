/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.dnd;

import java.util.List;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.layout.react.control.ReactCommand;

/**
 * Typed arguments of the {@code drop} command a client sends when a drag is released over a
 * {@link DropTarget}: which control the drag started in, which of its rows were dragged, and which
 * row of the receiving control the drop was made on.
 *
 * <p>
 * The arguments name nothing but client-side identities. The dragged objects are resolved by the
 * {@link DragSourceControl} the source id designates, the target object by the receiving control
 * itself, so no wire value can designate an object neither control displays.
 * </p>
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans. A recorded drop is rewritten to the
 * replay-stable {@link DropObjectsArguments}, which names the objects instead; this rendering
 * applies to a drop that could not be rewritten.
 * </p>
 */
@Label("Drop on '{targetKey}'")
public interface DropArguments extends ReactCommand {

	/** @see #getSource() */
	String SOURCE = "source";

	/** @see #getKeys() */
	String KEYS = "keys";

	/** @see #isSelection() */
	String SELECTION = "selection";

	/** @see #getTargetKey() */
	String TARGET_KEY = "targetKey";

	/** @see #getPosition() */
	String POSITION = "position";

	/**
	 * The id of the control the drag started in.
	 *
	 * @see DragSourceControl
	 */
	@Name(SOURCE)
	@Mandatory
	String getSource();

	/**
	 * The client-side keys of the dragged rows within the source control, comma-separated.
	 *
	 * @implNote Resolved through {@link DragSourceControl#dragObjects(List)}.
	 */
	@Name(KEYS)
	@Format(CommaSeparatedStrings.class)
	List<String> getKeys();

	/**
	 * Whether the drag carries the source control's whole selection rather than the rows the keys
	 * name.
	 *
	 * <p>
	 * Set when the dragged row was one of the selected ones. The selection is then read from the
	 * source control, which — unlike the client — knows the rows outside the rendered window.
	 * </p>
	 *
	 * @implNote Read through {@link DragSourceControl#dragSelection()}.
	 */
	@Name(SELECTION)
	boolean isSelection();

	/**
	 * The client-side key of the row the drop was made on, absent when it was made on the control as
	 * a whole.
	 */
	@Name(TARGET_KEY)
	@Nullable
	String getTargetKey();

	/**
	 * Where the drop happened relative to the target row.
	 *
	 * @implNote One of the names {@link DropPosition#wireName()} transmits a position under.
	 */
	@Name(POSITION)
	String getPosition();

}
