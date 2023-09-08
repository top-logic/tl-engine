/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.demo.dnd.handlers;

import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.component.dnd.ComponentDropEvent;
import com.top_logic.layout.component.dnd.ComponentDropTarget;
import com.top_logic.layout.tree.dnd.TreeDropTarget;
import com.top_logic.layout.tree.model.TLTreeModelUtil;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link TreeDropTarget} that accepts drop in a tree whithout allowing to specify a position.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DemoComponentDrop implements ComponentDropTarget {

	/** Singleton {@link DemoComponentDrop} instance. */
	public static final DemoComponentDrop INSTANCE = new DemoComponentDrop();

	/**
	 * Creates a new {@link DemoComponentDrop}.
	 */
	protected DemoComponentDrop() {
		// singleton instance
	}

	@Override
	public void handleDrop(ComponentDropEvent event) {
		LayoutComponent target = event.getTarget();

		for (Object droppedObject : event.getData()) {
			Object source = TLTreeModelUtil.getInnerBusinessObject(droppedObject);

			InfoService.showInfo(I18NConstants.COMPONENT_DROP__DATA_COMPONENT.fill(source, target.getTitleKey()));
		}
	}

}
