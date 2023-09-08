/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.scripting.runtime.action.NamedTabSwitchOp;

/**
 * Action that switches a tab in a {@link TabComponent}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface NamedTabSwitch extends ComponentAction {

	@Override
	@ClassDefault(NamedTabSwitchOp.class)
	Class<NamedTabSwitchOp> getImplementationClass();

	/**
	 * The technical name of the tab to select.
	 */
	String getCardName();
	
	/** @see #getCardName() */
	void setCardName(String value);

	/**
	 * Optional label of the tab to select as displayed at the GUI.
	 * 
	 * <p>
	 * This property is optional and only for documentary purpose. The value of this property must
	 * not be used for the action implementation.
	 * </p>
	 */
	String getCardComment();

	/** @see #getCardComment() */
	void setCardComment(String name);

}
