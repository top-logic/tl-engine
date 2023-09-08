/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.security;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.meta.form.component.EditAttributedComponent;

/**
 * Component for security demonstrations. Shows only elements of type 'DemoSecurity.A'.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class EditAComponent extends EditAttributedComponent {

	public EditAComponent(InstantiationContext context, Config someAttrs) throws ConfigurationException {
		super(context, someAttrs);
	}

	@Override
	protected String getMetaElementName() {
		return "DemoSecurity.A";
	}

}
