/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.layout;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.element.meta.form.component.EditAttributedComponent;

/**
 * Abstract superclass for edit component concerning contacts. Person Contacts and Company
 * contacts use mostly the similar behavior.
 *
 * @author <a href=mailto:jco@top-logic.com>jco</a>
 */
public abstract class AbstractEditContactComponent extends EditAttributedComponent {

	/**
	 * Configuration of the {@link AbstractEditContactComponent}
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends EditAttributedComponent.Config {

		@Override
		@StringDefault(ContactApplyHandler.COMMAND_ID)
		String getApplyCommand();

	}

    public AbstractEditContactComponent(InstantiationContext context, Config someAttrs) throws ConfigurationException {
        super(context, someAttrs);
    }

}
