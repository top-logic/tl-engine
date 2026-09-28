/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.dnd;

import java.util.Collection;

/**
 * What a control does with objects dropped on it, and what it accepts a drop of.
 *
 * <p>
 * Acceptance is decided in two stages. The {@link #acceptedTypes() accepted type tags} reach the
 * client, which uses them while a drag moves over the control to tell the user whether a drop is
 * possible at all; the same tags are checked again against the
 * {@link DragSourceControl#dragType() source's tag} when the drop arrives, so a client announcing a
 * drop the control never offered is refused. Whatever else makes a particular drop impossible — the
 * objects, the target row, the position — is decided by {@link #check(DropEvent)}, which has the
 * resolved objects at hand.
 * </p>
 *
 * <p>
 * The check is asked twice: while a drag hovers a target, the client probes it so the user sees
 * whether the target under the pointer takes the dragged objects, and why not; and once more when
 * the drop arrives, before {@link #onDrop(DropEvent)} is called — a refused drop never reaches
 * {@link #onDrop(DropEvent)}.
 * </p>
 *
 * <p>
 * A target whose {@link #acceptedTypes()} or {@link #dropOnRows()} answer changes over its life
 * tells the control displaying it to announce the change to the client again (for a table:
 * {@link com.top_logic.layout.react.control.table.TableViewControl#refreshDropTarget()}).
 * </p>
 *
 * @see DragSourceControl
 */
public interface DropTarget {

	/**
	 * The {@link DragSourceControl#dragType() type tags} of the drags this target accepts.
	 */
	Collection<String> acceptedTypes();

	/**
	 * Whether a single row is a drop target of its own, so that a drop names the row it was made on.
	 *
	 * <p>
	 * A target that assigns the dragged objects to the row they were dropped on (or inserts them
	 * next to it) needs this; one that only adds them to the control as a whole does not, and
	 * receives every drop with a {@code null} {@link DropEvent#target() target}.
	 * </p>
	 */
	default boolean dropOnRows() {
		return false;
	}

	/**
	 * Whether this target accepts the given drop.
	 *
	 * <p>
	 * Asked for every target the pointer moves over during a drag, and again before
	 * {@link #onDrop(DropEvent)}. It must therefore not modify anything.
	 * </p>
	 *
	 * @param event
	 *        The drop in question: the dragged objects, the row it would be made on and the position
	 *        relative to it.
	 * @return {@link DropVerdict#ACCEPTED}, or a {@link DropVerdict#refused(com.top_logic.basic.util.ResKey)
	 *         refusal} naming the reason the user is shown. Accepts every drop by default.
	 */
	default DropVerdict check(DropEvent event) {
		return DropVerdict.ACCEPTED;
	}

	/**
	 * Applies a drop.
	 *
	 * <p>
	 * Called only for a drop {@link #check(DropEvent)} accepts.
	 * </p>
	 *
	 * @param event
	 *        The dragged objects, the row they were dropped on and the position relative to it.
	 */
	void onDrop(DropEvent event);

}
