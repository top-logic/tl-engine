/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Command action on a {@link LayoutComponent}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ComponentAction extends ApplicationAction {

	/**
	 * Name of the component this action is executed on.
	 * 
	 * @return The {@link LayoutComponent#getName()} of this actions model.
	 */
	ComponentName getComponentName();
	
	/**
	 * Sets the {@link #getComponentName()} property.
	 */
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
	
}
