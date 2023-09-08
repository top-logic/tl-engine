/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.user;

import com.top_logic.base.security.attributes.PersonAttributes;
import com.top_logic.dob.filt.DOStringAttributeComparator;
import com.top_logic.util.TLContext;

/**
 * Compare two UserInterfaces using the full name, i.e.
 * sort by last name, then by first name.
 *
 * @author    <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public class UserFullNameComparator extends DOStringAttributeComparator
                                    implements PersonAttributes {
	
    /**
	 * Default constructor. Sorts ascending.
	 */
	public UserFullNameComparator() {
		this (ASCENDING);
	}
    
	/**
	 * Constructor with sorting direction parameter.
	 * 
	 * @param	asc		if true sort ascendingly, descendingly otherwise.
	 */
	public UserFullNameComparator (boolean asc) {
        super (new String[] {SUR_NAME, GIVEN_NAME, USER_NAME},
			new boolean[] { asc, asc, asc }, TLContext.getLocale());
	}
}
