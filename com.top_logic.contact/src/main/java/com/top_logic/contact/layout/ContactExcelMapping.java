/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.layout;

import com.top_logic.contact.business.PersonContact;
import com.top_logic.knowledge.gui.layout.person.PersonExcelMapping;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tool.export.ExcelCellRenderer.RenderContext;
import com.top_logic.tool.export.ExcelMapping;

/**
 * {@link ExcelMapping} for {@link PersonContact}s that have an account.
 */
public class ContactExcelMapping implements ExcelMapping {

	@Override
	public Object apply(RenderContext context, Object obj) {
		if (obj instanceof PersonContact contact) {
			Person account = contact.getPerson();
			if (account != null) {
				return PersonExcelMapping.INSTANCE.apply(context, account);
			}
		}
		return obj;
	}

}
