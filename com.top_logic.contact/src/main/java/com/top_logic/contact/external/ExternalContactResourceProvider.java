/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.external;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.mig.html.DefaultResourceProvider;
import com.top_logic.util.Resources;

/**
 * Get labels for ExternalContacts
 * 
 * @author    <a href=mailto:kbu@top-logic.com>kbu</a>
 */
public class ExternalContactResourceProvider extends DefaultResourceProvider {
	
	public static final ExternalContactResourceProvider EXTERNAL_CONTACT_INSTANCE = new ExternalContactResourceProvider();

	/** 
	 * @see com.top_logic.layout.LabelProvider#getLabel(java.lang.Object)
	 */
	@Override
	public String getLabel(Object anObject) {
		ExternalContact theContact = (ExternalContact) anObject;

		return theContact.getFamilyName() + ", " + theContact.getFirstName() + " (" + StringServices.nonNull(theContact.getDivision()) + ")";
	}

	/** 
	 * @see com.top_logic.layout.ResourceProvider#getTooltip(java.lang.Object)
	 */
	@Override
	public String getTooltip(Object anObject) {
		ExternalContact theContact = (ExternalContact) anObject;
        String          theText    = TagUtil.encodeXML(this.getLabel(theContact));
        String          theMail    = TagUtil.encodeXML(theContact.getEMail());
        String          thePhone   = TagUtil.encodeXML(theContact.getPhone());

		return Resources.getInstance().getString(
			com.top_logic.contact.layout.I18NConstants.PERSON_TOOLTIP__NAME_MAIL_PHONE.fill(theText, theMail,
				thePhone));
	}
}
