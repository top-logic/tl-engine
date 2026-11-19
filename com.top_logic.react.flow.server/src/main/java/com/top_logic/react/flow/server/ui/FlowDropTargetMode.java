/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.server.ui;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.layout.view.dnd.DropArguments;
import com.top_logic.layout.view.dnd.DropBinding;
import com.top_logic.layout.view.dnd.DropConfig;
import com.top_logic.layout.view.dnd.DropSignature;

/**
 * What a declared drop of a flow diagram targets: the diagram as a whole, or a single node of it.
 *
 * <p>
 * A node is the innermost box under the pointer that is a drop region or has a user object; its user
 * object is the object the drop is made onto. Beside the dropped objects, the drop's
 * {@value DropConfig#REFUSE_IF} and action chain see the point the drop is made at as
 * {@value DropArguments#X} and {@value DropArguments#Y} (renamed with {@value DropConfig#X_AS} and
 * {@value DropConfig#Y_AS}), in diagram units, independent of zoom and scrolling.
 * </p>
 *
 * @see FlowDropConfig
 * @see DropBinding
 */
public enum FlowDropTargetMode implements ExternallyNamed {

	/**
	 * The diagram as a whole is the target.
	 *
	 * <p>
	 * A drop on the free area of the diagram applies - and on a drop region without a user object.
	 * Where the diagram declares a {@code node} drop for the drag as well, a drop on a node goes to
	 * that drop only; without one, a drop anywhere on the diagram applies. The reference
	 * {@value DropArguments#TARGET} (renamed with {@value DropConfig#TARGET_AS}) holds no value; the
	 * point is relative to the origin of the diagram.
	 * </p>
	 */
	DIAGRAM("diagram", DropSignature.CONTROL),

	/**
	 * A single node is the target.
	 *
	 * <p>
	 * A drop applies to the user object of the node it was made on; a drop beside the nodes applies
	 * to nothing. The object is the reference {@value DropArguments#TARGET} (renamed with
	 * {@value DropConfig#TARGET_AS}), which the optional {@value DropConfig#TARGET_CHANNEL} receives
	 * as well; the point is relative to the upper left corner of the node. The node under the pointer
	 * is highlighted while such a drag moves over the diagram.
	 * </p>
	 */
	NODE("node", DropSignature.ONTO);

	private final String _externalName;

	private final DropSignature _signature;

	private FlowDropTargetMode(String externalName, DropSignature signature) {
		_externalName = externalName;
		_signature = signature;
	}

	/**
	 * The {@link DropSignature} a {@link DropBinding} applies a drop of this target with: on the
	 * diagram as a whole, or onto a node.
	 */
	public DropSignature signature() {
		return _signature;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

}
