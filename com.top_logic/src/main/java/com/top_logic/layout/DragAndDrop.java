/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Drop.DropException;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Container managing potential drop targets.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DragAndDrop<T> {

	private final FrameScope _scope;

	private final Map<String, Drop<? super T>> _targets = new HashMap<>();

	/**
	 * Creates a {@link DragAndDrop}.
	 * 
	 * @param scope
	 *        The {@link FrameScope} in which all drop targets will be rendered. Required for early
	 *        initializing IDs.
	 */
	public DragAndDrop(FrameScope scope) {
		_scope = scope;
	}

	/**
	 * Adds the given {@link Drop} as target for drag'n drop.
	 * 
	 * @param target
	 *        the target to add. must not be <code>null</code>;
	 */
	public void addDropTarget(Drop<? super T> target) {
		DNDUtil.ensureId(_scope, target);
		_targets.put(target.getID(), target);
	}

	/**
	 * Removes the given {@link Drop} as target.
	 * 
	 * @param target
	 *        the target to remove
	 * @see #addDropTarget(Drop)
	 */
	public void removeDropTarget(Drop<? super T> target) {
		_targets.remove(target.getID());
	}

	/**
	 * Creates the {@link HTMLConstants#TL_DRAG_N_DROP} attribute for {@link Drag} sources.
	 * 
	 * @see DNDUtil#writeDNDInfo(TagWriter, FrameScope, java.util.Collection)
	 */
	public void writeDNDInfo(TagWriter out) throws IOException {
		DNDUtil.writeDNDInfo(out, _scope, _targets.values());
	}

	/**
	 * Dispatches {@link Drop#notifyDrop(Object, Object)} to the {@link Drop} instance with the
	 * given ID.
	 * 
	 * @param dropId
	 *        The client-side ID of the drop target.
	 * @param value
	 *        The application-level value to drop.
	 * @param dropInfo
	 *        Additional client-side info sent to the drop target.
	 * @return The {@link Drop} instance that was notified, or <code>null</code> if no drop target
	 *         was found with the given ID.
	 * @throws DropException
	 *         See {@link Drop#notifyDrop(Object, Object)}.
	 */
	public Drop<? super T> processDrag(String dropId, T value, Object dropInfo) throws DropException {
		Drop<? super T> drop = getDrop(dropId);
		if (drop != null) {
			drop.notifyDrop(value, dropInfo);
		}
		return drop;
	}

	/**
	 * Look up the {@link Drop} with the given client-side ID.
	 */
	public Drop<? super T> getDrop(String dropId) {
		return _targets.get(dropId);
	}

}
