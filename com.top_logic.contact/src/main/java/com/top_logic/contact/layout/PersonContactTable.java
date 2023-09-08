/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.layout;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.util.TLContext;

/**
 * The PersonContactTable selects the own contact as default / initial selection.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class PersonContactTable extends TableComponent {

    /**
     * CBR Creates a new instance of this class.
     */
    public PersonContactTable(InstantiationContext context, Config aAttr) throws ConfigurationException {
        super(context, aAttr);
    }

	/**
	 * Select the {@link PersonContact} of the current user as default.
	 */
	@Override
	protected Object getDefaultSelection(List<?> displayedRows) {
		PersonContact currentPerson = PersonContact.getPersonContact(TLContext.getContext().getCurrentPersonWrapper());

		if (displayedRows.contains(currentPerson)) {
			return currentPerson;
		} else {
			return super.getDefaultSelection(displayedRows);
		}
	}

}
