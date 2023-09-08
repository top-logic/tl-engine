/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.scripting.action.SelectSelectableAction;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Just for backward compatibility of old recordings. Use {@link SelectActionOp}
 * instead.
 */
@Deprecated
public class SelectSelectableActionOp extends ComponentActionOp<SelectSelectableAction> {

	public SelectSelectableActionOp(InstantiationContext context, SelectSelectableAction config) {
		super(context, config);
	}

	@Override
	protected Object process(ActionContext context, LayoutComponent component, Object argument) {
		Selectable selectable = (Selectable) component;
		Object selection = context.resolve(config.getSelection(), component);
		selectable.setSelected(selection);

		// We don't check if setting the selection succeeded, as we don't know whether setting the
		// selection succeeded when this action was recorded.

		return argument;
	}

}
