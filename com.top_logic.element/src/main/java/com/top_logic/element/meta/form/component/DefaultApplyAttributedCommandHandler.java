/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.component;


import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Updates all attributes for the corresponding fields in the
 * {@link FormContext}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultApplyAttributedCommandHandler extends AbstractApplyAttributedCommandHandler {

	public static final String COMMAND_ID = "storeAttributes";

    public DefaultApplyAttributedCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

    @Override
	protected boolean storeChanges(LayoutComponent component, FormContext formContext, Object model) {
		return this.saveMetaAttributes(formContext);
	}
}
