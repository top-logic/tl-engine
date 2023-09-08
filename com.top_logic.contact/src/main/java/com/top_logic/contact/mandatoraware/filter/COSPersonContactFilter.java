/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.filter;

import com.top_logic.contact.business.PersonContact;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.model.TLObject;

/**
 * Accept the value if it is a PersonContact
 * 
 * @author     <a href="mailto:kbu@top-logic.com">kbu</a>
 */
@Deprecated
public class COSPersonContactFilter extends MandatorAwareContactFilter {

	/**
	 * Accept the value if it is a PersonContact
	 */
	@Override
	protected boolean checkBasics(TLObject anAttributed, EditContext editContext,
			Object anObject) {
		return (anObject instanceof PersonContact);
	}

}
