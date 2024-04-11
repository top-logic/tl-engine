/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.component.dnd.ComponentDropEvent;
import com.top_logic.layout.component.dnd.ComponentDropTarget;
import com.top_logic.layout.dnd.DnD;
import com.top_logic.layout.dnd.DndData;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ControlCommand} implements a generic drop on a component assuming the
 * {@link Control}'s model is a {@link LayoutComponent}.
 */
public class ComponentDropAction extends ControlCommand {

	private static final String COMMAND_ID = "dndDrop";

	/**
	 * Singleton {@link ComponentDropAction} instance.
	 */
	public static final ComponentDropAction INSTANCE = new ComponentDropAction();

	private ComponentDropAction() {
		super(COMMAND_ID);
	}

	@Override
	public ResKey getI18NKey() {
		return I18NConstants.COMPONENT_DROP;
	}

	@Override
	protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
		LayoutComponent component = (LayoutComponent) control.getModel();
		ComponentDropTarget dropTarget = component.getConfig().getDropTarget();
		if (!dropTarget.dropEnabled(component)) {
			throw new TopLogicException(com.top_logic.layout.dnd.I18NConstants.DROP_NOT_POSSIBLE);
		}

		DndData data = DnD.getDndData(commandContext, arguments);
		if (data != null) {
			dropTarget.handleDrop(new ComponentDropEvent(data, component));
		}

		return HandlerResult.DEFAULT_RESULT;
	}
}