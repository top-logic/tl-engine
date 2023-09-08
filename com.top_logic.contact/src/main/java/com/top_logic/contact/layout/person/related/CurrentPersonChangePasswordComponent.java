/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.layout.person.related;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.gui.layout.person.ChangePasswordComponent;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;

/**
 * {@link ChangePasswordComponent} for current user only, so that security checks are not necessary.
 * 
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class CurrentPersonChangePasswordComponent extends ChangePasswordComponent {

	/**
	 * Creates a new {@link CurrentPersonChangePasswordComponent}.
	 */
	public CurrentPersonChangePasswordComponent(InstantiationContext context, Config atts) throws ConfigurationException {
		super(context, atts);
	}

	@Override
	public boolean allow(BoundCommandGroup aCmdGroup, BoundObject anObject) {
		if (EditCurrentPersonComponent.READWRITE_SET.contains(aCmdGroup)) {
			return true;
		}
		return super.allow(aCmdGroup, anObject);
	}

}
