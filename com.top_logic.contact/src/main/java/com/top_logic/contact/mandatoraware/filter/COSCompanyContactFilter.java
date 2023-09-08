/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.filter;

import com.top_logic.contact.business.CompanyContact;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.model.TLObject;

/**
 * Check for CompanyContact
 * 
 * @author     <a href="mailto:kbu@top-logic.com">kbu</a>
 */
@Deprecated
public class COSCompanyContactFilter extends MandatorAwareContactFilter {

    /**
	 * Check for CompanyContact and client property
	 */
    @Override
	protected boolean checkBasics(TLObject anAttributed, EditContext attributeUpdate,
			Object anObject) {
        return (anObject instanceof CompanyContact);
    }

}
