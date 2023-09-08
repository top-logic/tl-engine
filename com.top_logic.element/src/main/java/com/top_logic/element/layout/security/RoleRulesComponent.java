/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.security;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.model.TLClass;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.form.model.FormContext;

/**
 * @author     <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class RoleRulesComponent extends EditComponent {

	/**
	 * Configuration of the {@link RoleRulesComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends EditComponent.Config {

		@Override
		@NullDefault
		String getApplyCommand();

	}

	public RoleRulesComponent(InstantiationContext context, Config someAttrs) throws ConfigurationException {
        super(context, someAttrs);
    }

    @Override
	protected boolean supportsInternalModel(Object anObject) {
        return anObject instanceof TLClass;
    }

    @Override
	public FormContext createFormContext() {
        
        FormContext theFC = new FormContext("form", this.getResPrefix()); 
        
        return theFC;
    }

}
