/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.structure.Expandable;
import com.top_logic.layout.structure.Expandable.ExpansionState;
import com.top_logic.layout.structure.LayoutControl;
import com.top_logic.mig.html.layout.LayoutUtils;

/**
 * {@link AbstractApplicationActionOp} for {@link CollapseToolbarAction}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CollapseToolbarActionOp
		extends AbstractApplicationActionOp<CollapseToolbarActionOp.CollapseToolbarAction> {

	/**
	 * {@link ApplicationAction} that represents collapsing or expanding of a toolbar.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface CollapseToolbarAction extends ApplicationAction {

		/**
		 * The owner of the toolbar to collapse or expand.
		 */
		ModelName getExpandable();

		/**
		 * @see #getExpandable()
		 */
		void setExpandable(ModelName value);

		/**
		 * The {@link ExpansionState} of the toolbar.
		 */
		ExpansionState getExpansionState();

		/**
		 * Setter for {@link #getExpansionState()}
		 */
		void setExpansionState(ExpansionState state);

	}

	/**
	 * Creates a new {@link CollapseToolbarActionOp}.
	 */
	public CollapseToolbarActionOp(InstantiationContext context, CollapseToolbarAction config) {
		super(context, config);
	}

	@Override
	protected Object processInternal(ActionContext context, Object argument) throws Throwable {
		DisplayContext displayContext = context.getDisplayContext();
		WindowScope windowScope = LayoutUtils.getWindowScope(displayContext);
		LayoutControl rootControl = (LayoutControl) windowScope;
		Expandable expandable = (Expandable) ModelResolver.locateModel(context, rootControl, config.getExpandable());
		expandable.setExpansionState(getConfig().getExpansionState());
		return argument;
	}

}
