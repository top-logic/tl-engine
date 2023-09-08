/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.monitor;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.LabelProvider;
import com.top_logic.util.Resources;

/**
 *          com.top_logic.knowledge.monitor.UserMonitorTableRenderer$UserNameLabelProvider
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class UserNameLabelProvider implements LabelProvider {

	/** Singleton {@link UserNameLabelProvider} instance. */
	public static final UserNameLabelProvider INSTANCE = new UserNameLabelProvider();

	private UserNameLabelProvider() {
		// singleton instance
	}

	@Override
	public String getLabel(Object object) {
		Person thePerson = PersonManager.getManager().getPersonByName((String) object);
		if (thePerson != null) {
			String theName = thePerson.getName();
			try {
				return theName + " (" + thePerson.getFullName() + ")";
			} catch (Exception ex) {
				return theName + " (???)";
			}
		}
		return object + " (" + Resources.getInstance().getString(I18NConstants.DELETED_USER) + ")";
	}

}

