/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.person;

import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.StringServices;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tool.export.ExcelCellRenderer.RenderContext;
import com.top_logic.tool.export.ExcelMapping;

/**
 * {@link ExcelMapping} for {@link Person}s.
 */
public class PersonExcelMapping implements ExcelMapping {

	/**
	 * Singleton {@link PersonExcelMapping} instance.
	 */
	public static final PersonExcelMapping INSTANCE = new PersonExcelMapping();

	private PersonExcelMapping() {
		// Singleton constructor.
	}

	@Override
	public Object apply(RenderContext context, Object obj) {
		if (obj instanceof Person account) {
			UserInterface user = account.getUser();

			String loginName = account.getName();
			String title = user == null ? null : user.getTitle();
			String lastName = user == null ? null : user.getName();
			String firstName = user == null ? null : user.getFirstName();

			if (!StringServices.isEmpty(title) && lastName != null) {
				lastName = title + " " + lastName;
			}

			return StringServices.isEmpty(lastName) ? loginName
				: StringServices.isEmpty(firstName) ? I18NConstants.ACCOUNT_LABEL__LAST_LOGIN.fill(lastName, loginName)
					: I18NConstants.ACCOUNT_LABEL__FIRST_LAST_LOGIN.fill(firstName, lastName, loginName);
		} else {
			return obj;
		}
	}

}
