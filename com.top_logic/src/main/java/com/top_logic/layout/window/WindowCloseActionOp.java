/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.window;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.layout.scripting.action.ComponentAction;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.layout.scripting.runtime.action.ComponentActionOp;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.QualifiedComponentNameConstraint;

/**
 * {@link ComponentActionOp} for {@link WindowCloseAction}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class WindowCloseActionOp extends ComponentActionOp<WindowCloseActionOp.WindowCloseAction> {

	/**
	 * {@link ComponentAction} closing a window with given name.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface WindowCloseAction extends ComponentAction {

		@Override
		@ClassDefault(WindowCloseActionOp.class)
		Class<? extends AbstractApplicationActionOp<?>> getImplementationClass();

		/**
		 * Name of the {@link WindowComponent} to close.
		 */
		@Mandatory
		@Constraint(QualifiedComponentNameConstraint.class)
		ComponentName getCloseWindowName();

		/**
		 * Sets {@link #getCloseWindowName()}.
		 */
		void setCloseWindowName(ComponentName windowName);

	}

	/**
	 * Creates a new {@link WindowCloseActionOp}.
	 */
	public WindowCloseActionOp(InstantiationContext context, WindowCloseAction config) {
		super(context, config);
	}

	@Override
	protected Object process(ActionContext context, LayoutComponent component, Object argument) {
		WindowManager windowManager = component.getMainLayout().getWindowManager();
		ComponentName windowName = getConfig().getCloseWindowName();
		WindowComponent windowByName = windowManager.getWindowByName(windowName);
		if (windowByName == null) {
			throw ApplicationAssertions.fail(getConfig(), "No window with name '" + windowName + "' opened.");
		}
		windowByName.close();
		return argument;
	}

}

