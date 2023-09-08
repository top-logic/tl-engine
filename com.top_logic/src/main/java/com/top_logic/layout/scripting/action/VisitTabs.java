/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import java.util.List;

import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.scripting.runtime.action.VisitTabsOp;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutContainer;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.mig.html.layout.QualifiedComponentNameConstraint;

/**
 * Configuration of {@link VisitTabsOp}.
 * 
 * <p>
 * {@link ApplicationAction} that visits all of the {@link LayoutContainer}s tabs and subtabs. <br/>
 * The given {@link LayoutContainer} should either be a {@link TabComponent} or the
 * {@link MainLayout}.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface VisitTabs extends DynamicAction {

	/**
	 * Name of the component this action is executed on.
	 * 
	 * @return The {@link LayoutComponent#getName()} of this actions model.
	 */
	@Constraint(QualifiedComponentNameConstraint.class)
	ComponentName getComponentName();

	/** @see #getComponentName() */
	void setComponentName(ComponentName value);

	/**
	 * E.g. the name of the class that is supposed to implements the component.
	 * 
	 * <p>
	 * This property is optional and only for debugging purpose. The value of this property must not
	 * be used for the action implementation.
	 * </p>
	 */
	String getComponentImplementationComment();

	/** @see #getComponentImplementationComment() */
	void setComponentImplementationComment(String value);

	/**
	 * Return a collection of components that must not be visited.
	 */
	@ListBinding(tag = "ignore-component", attribute = "name")
	List<ComponentName> getIgnoreComponents();

}
