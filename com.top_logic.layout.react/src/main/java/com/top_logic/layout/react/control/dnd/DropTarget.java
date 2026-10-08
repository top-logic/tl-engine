/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.dnd;

import java.util.Set;

import com.top_logic.layout.react.I18NConstants;

/**
 * What a control does with objects dropped on it, and what it accepts a drop of.
 *
 * <p>
 * A target consists of drop operations, each of a {@link DropMode}: on the control as a whole, onto
 * an item, or inserting among the items. The {@link #dropModes() modes} of the operations reach
 * the client, which splits an item into the {@link DropZone zones} they need; the control resolves
 * the item and the zone a drop is made in into the {@link DropLocation} of each mode, and the target
 * {@link #check(DropRequest) decides} which operation, if any, accepts the drop at the location of
 * its mode.
 * </p>
 *
 * <p>
 * Acceptance is decided in two stages. The {@link #acceptedKinds() accepted kinds} reach the
 * client, which uses them while a drag moves over the control to tell the user whether a drop is
 * possible at all; the same kinds are checked again against the
 * {@link DragSourceControl#dragKind() source's kind} when the drop arrives, so a client announcing a
 * drop the control never offered is refused. Whatever else makes a particular drop impossible — the
 * objects, the location — is decided by {@link #check(DropRequest)}, which has the resolved objects
 * at hand.
 * </p>
 *
 * <p>
 * The check is asked twice: while a drag hovers a target, the client probes it so the user sees
 * whether the target under the pointer takes the dragged objects - and where, or why not; and once
 * more when the drop arrives, before {@link #onDrop(DropEvent)} is called with the location the
 * check accepted the drop at - a refused drop never reaches {@link #onDrop(DropEvent)}.
 * </p>
 *
 * <p>
 * A target whose {@link #acceptedKinds()} or {@link #dropModes()} answer changes over its life
 * tells the control displaying it to announce the change to the client again (for a table:
 * {@link com.top_logic.layout.react.control.table.TableViewControl#refreshDropTarget()}).
 * </p>
 *
 * @see DragSourceControl
 */
public interface DropTarget {

	/**
	 * The {@link DragSourceControl#dragKind() kinds} of the drags this target accepts.
	 */
	AcceptedKinds acceptedKinds();

	/**
	 * The modes of the operations this target currently offers.
	 *
	 * <p>
	 * The control announces them to the client, which splits an item into the zones the modes need:
	 * an insertion between two items needs the upper and the lower part of an item told apart, a
	 * drop onto an item its middle, a drop on the control as a whole no item at all.
	 * </p>
	 *
	 * @return The modes, in the order {@link #check(DropRequest)} tries them by default. A drop on
	 *         the control as a whole by default.
	 */
	default Set<DropMode> dropModes() {
		return Set.of(DropMode.CONTROL);
	}

	/**
	 * Whether this target accepts the given drop, and at which location.
	 *
	 * <p>
	 * A target with several operations tries them in its order: the first operation that accepts
	 * the drag's kind, finds a location for its mode in the request, and accepts the drop there
	 * wins. Asked for every place the pointer moves over during a drag, and again before
	 * {@link #onDrop(DropEvent)}, so it must not modify anything and must decide the same way for
	 * the same request.
	 * </p>
	 *
	 * @param request
	 *        The drop in question: the dragged objects and the location of the drop for each mode.
	 * @return The {@link DropVerdict#accepted(DropLocation) acceptance} at the location of the
	 *         winning operation, or a {@link DropVerdict#refused(com.top_logic.basic.util.ResKey)
	 *         refusal} naming the reason the user is shown. By default, the drop is accepted at the
	 *         location of the first of the {@link #dropModes()} the request has one for.
	 */
	default DropVerdict check(DropRequest request) {
		for (DropMode mode : dropModes()) {
			DropLocation location = request.location(mode);
			if (location != null) {
				return DropVerdict.accepted(location);
			}
		}
		return DropVerdict.refused(I18NConstants.ERROR_DROP_NOT_ACCEPTED);
	}

	/**
	 * Applies a drop.
	 *
	 * <p>
	 * Called only for a drop {@link #check(DropRequest)} accepts, at the location of the accepting
	 * verdict. A target with several operations applies the drop through the operation that
	 * accepts {@link DropRequest#of(DropEvent) a request of exactly this location}.
	 * </p>
	 *
	 * @param event
	 *        The dragged objects and the location the drop is applied at.
	 */
	void onDrop(DropEvent event);

	/**
	 * Applies a drop, and runs the given follow-up once the drop is applied.
	 *
	 * <p>
	 * A target whose application of a drop may come to an end only later - after the user answered
	 * a question, say - runs the follow-up then, and not at all where the application is abandoned.
	 * By default, the drop is applied by {@link #onDrop(DropEvent)} and the follow-up runs right
	 * after it.
	 * </p>
	 *
	 * @param event
	 *        The dragged objects and the location the drop is applied at.
	 * @param onApplied
	 *        Runs once the drop is applied.
	 */
	default void onDrop(DropEvent event, Runnable onApplied) {
		onDrop(event);
		onApplied.run();
	}

}
