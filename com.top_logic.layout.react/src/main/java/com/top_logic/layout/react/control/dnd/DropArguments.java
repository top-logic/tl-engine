/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.dnd;

import java.util.List;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.json.JsonBinding;
import com.top_logic.basic.config.json.SimpleJsonListBinding;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.identity.DisplayedObjectId;

/**
 * Typed arguments of the {@code drop} command a client sends when a drag is released over a
 * {@link DropTarget}: which control the drag started in, which of its items were dragged, and which
 * item of the receiving control the drop was made on, in which of its {@link DropZone zones}.
 *
 * <p>
 * The arguments name the objects by the ids the controls display them under. The dragged objects
 * are resolved by the {@link DragSourceControl} the {@link #getSource() source} designates, the
 * target object by the receiving control itself, so no wire value can designate an object neither
 * control displays.
 * </p>
 *
 * <p>
 * Both kinds of ids are {@link DisplayedObjectId}s: a recorded drop names the source control by its
 * address, the dragged objects by their keys in the source control and the target by its key in
 * the receiving control, and a replay resolves them back to the ids in the replaying session.
 * </p>
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Drop {keys} on '{targetKey}'")
public interface DropArguments extends ReactCommand {

	/** @see #getSource() */
	String SOURCE = "source";

	/** @see #getKeys() */
	String KEYS = "keys";

	/** @see #isSelection() */
	String SELECTION = "selection";

	/** @see #getTargetKey() */
	String TARGET_KEY = "targetKey";

	/** @see #getZone() */
	String ZONE = "zone";

	/**
	 * The control the drag started in: its control id, as the browser sends it, or its semantic
	 * address, as an agent observes it and a recorded step carries it.
	 *
	 * @see DragSourceControl
	 * @see com.top_logic.layout.react.control.identity.ControlReferences#controlOf(String)
	 */
	@Name(SOURCE)
	@Mandatory
	String getSource();

	/**
	 * The ids of the dragged items in the {@link #getSource() source control}: the row ids of a
	 * table, the node ids of a tree, the card keys of a board.
	 *
	 * @implNote Resolved through {@link DragSourceControl#dragObjects(List)}.
	 */
	@Name(KEYS)
	@DisplayedObjectId(control = SOURCE)
	@JsonBinding(SimpleJsonListBinding.class)
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
	 * The id of the item of the receiving control the drop was made on - a row of a table, a node
	 * of a tree, a card or a column of a board -, absent when it was made beside the items.
	 */
	@Name(TARGET_KEY)
	@Nullable
	@DisplayedObjectId
	String getTargetKey();

	/**
	 * Where within the target row the drop was made, absent for a drop beside the rows.
	 *
	 * @implNote One of the names {@link DropZone#wireName()} transmits a zone under.
	 */
	@Name(ZONE)
	String getZone();

}
