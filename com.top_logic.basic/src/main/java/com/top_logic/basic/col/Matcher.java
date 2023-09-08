/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

/**
 * A Matcher is used for a LabelProvider in combination with a SelectField that
 * supports autocompletion. It is necessary if other information than just the
 * normal lable should be used, e.g. the login id for Persons.
 * 
 * An example can be found in
 * com.top_logic.contact.layout.PersonContactLabelProvider.
 * 
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public interface Matcher {
	
	/**
	 * The {@link Matcher} that does not match any objects.
	 */
    Matcher NONE = new Matcher() {

		@Override
		public boolean match(String searchText, Object anObject) {
	        return false;
        }
    	
    };

    /**
     * Checks weather the given object matches the given search text. 
     * 
     * @param searchText a String in lower case
     * @param anObject   an Object to check
     * 
     * @return true when match was sucessful.
     */
	public boolean match(String searchText, Object anObject);

}
