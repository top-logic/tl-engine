/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.layout.company;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.contact.business.CompanyContact;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.element.meta.gui.CreateAttributedComponent;
import com.top_logic.element.structured.wrap.Mandator;
import com.top_logic.model.TLClass;


/**
 * This create component is mainly responsible for creating a form context
 * for Company contacts, which are slithly more complicated than Person contact.
 * (At least during creation).
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class CreateCompanyContactComponent extends CreateAttributedComponent {

	/**
	 * Configuration for the {@link CreateCompanyContactComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends CreateAttributedComponent.Config {

		@StringDefault(CompanyContactCreateHandler.COMMAND_ID)
		@Override
		String getCreateHandler();

	}

    public CreateCompanyContactComponent(InstantiationContext context, Config someAttrs)
            throws ConfigurationException {
        super(context, someAttrs);
    }

    @Override
	protected boolean supportsInternalModel(Object anObject) {
		return anObject == null || anObject instanceof Mandator;
    }
    
	@Override
	public TLClass getMetaElement() {
        return ContactFactory.getInstance().getMetaElement(CompanyContact.META_ELEMENT);
    }
}
