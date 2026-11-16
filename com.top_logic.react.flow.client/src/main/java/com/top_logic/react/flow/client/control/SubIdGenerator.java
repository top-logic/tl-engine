/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.client.control;

import com.top_logic.react.flow.data.Widget;
import com.top_logic.react.flow.svg.SvgWriter;

import de.haumacher.msgbuf.graph.AbstractSharedGraphNode;

/**
 * Generator of locally unique IDs based on some context ID.
 *
 * <p>
 * Besides the sequential IDs of {@link #createId()}, it builds the IDs of elements a widget draws
 * for a purpose from the context ID and the node ID of the widget, see
 * {@link #ownedId(Widget, String)}.
 * </p>
 */
public class SubIdGenerator {

	private final String _baseId;

	private int _next = 1;

	/**
	 * Creates a {@link SubIdGenerator}.
	 */
	public SubIdGenerator(String baseId) {
		_baseId = baseId;
	}

	/**
	 * Creates the next ID.
	 */
	public String createId() {
		return _baseId + "-" + (_next++);
	}

	/**
	 * The ID of the element the given owner draws for the given purpose, built from the context
	 * ID and the {@link AbstractSharedGraphNode#id() node ID} of the owner.
	 *
	 * <p>
	 * The ID stays the same when the owner is drawn again, so that it can be computed again to
	 * find the element, and differs from all IDs of {@link #createId()}.
	 * </p>
	 *
	 * @param owner
	 *        The widget drawing the element.
	 * @param purpose
	 *        The role of the element within the owner, see
	 *        {@link SvgWriter#uniqueId(Widget, String)}.
	 * @return The ID, or <code>null</code> if the owner has no node ID.
	 */
	public String ownedId(Widget owner, String purpose) {
		int node = owner instanceof AbstractSharedGraphNode ? ((AbstractSharedGraphNode) owner).id() : 0;
		if (node == 0) {
			return null;
		}
		return _baseId + "-n" + node + "-" + purpose;
	}

}
