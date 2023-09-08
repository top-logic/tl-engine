/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.dnd;

import com.top_logic.layout.dnd.DndData;
import com.top_logic.layout.dnd.DropEvent;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link DropEvent} signaling a drop operation on a generic {@link LayoutComponent}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ComponentDropEvent extends DropEvent {

	private final LayoutComponent _target;

	/**
	 * Creates a {@link ComponentDropEvent}.
	 *
	 * @param data
	 *        See {@link DropEvent#DropEvent(DndData)}.
	 * @param target
	 *        The {@link LayoutComponent} that is the target of the drop operation.
	 */
	public ComponentDropEvent(DndData data, LayoutComponent target) {
		super(data);
		_target = target;
	}

	@Override
	public LayoutComponent getTarget() {
		return _target;
	}

}
