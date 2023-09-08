/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.layout.company;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.contact.business.CompanyContact;
import com.top_logic.contact.layout.AbstractEditContactComponent;


/**
 * Component to edit company contacts
 * 
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class EditCompanyContactComponent extends AbstractEditContactComponent {

	/**
	 * Configuration for {@link EditCompanyContactComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractEditContactComponent.Config {

		@Override
		@StringDefault(CompanyContactDeleteCommandHandler.COMMAND_ID)
		String getDeleteCommand();
		
		@Override
		@BooleanDefault(true)
		boolean getShowNoModel();
	}

	/**
	 * Creates a new {@link EditCompanyContactComponent}.
	 */
    public EditCompanyContactComponent(InstantiationContext context, Config someAttrs)
            throws ConfigurationException {
        super(context, someAttrs);
    }

    /** 
     * @see com.top_logic.element.meta.form.component.EditAttributedComponent#getMetaElementName()
     */
    @Override
	protected String getMetaElementName() {
        return CompanyContact.META_ELEMENT;
    }

    /**
     * @see com.top_logic.layout.form.component.EditComponent#supportsInternalModel(java.lang.Object)
     */
    @Override
	protected boolean supportsInternalModelNonNull(Object anObject) {
		return anObject instanceof CompanyContact;
    }
   
}
