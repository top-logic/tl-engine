/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.util.Resources;

/**
 * The AllowedCharactersOnlyConstraint allows only or forbids inserting the specified
 * characters.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class AllowedCharactersOnlyConstraint extends AbstractStringConstraint {

    /** The characters which are allowed or forbidden. */
    private final String characters;

    /**
     * Defines whether the specified characters are allowed (<code>false</code>) or
     * forbidden (<code>true</code>)
     */
    private final boolean forbidden;

    /**
     * Defines whether case should be ignored while checking the constraint.
     */
    private final boolean ignoreCase;



    /**
     * Creates a AllowedCharactersOnlyConstraint with the specified allowed characters,
     * ignoring case.
     *
     * @param allowedChars
     *        the characters which are allowed to enter
     */
    public AllowedCharactersOnlyConstraint(String allowedChars) {
        this(allowedChars, false, true);
    }

    /**
     * Creates a AllowedCharactersOnlyConstraint with the specified allowed or forbidden
     * characters, ignoring case.
     *
     * @param characters
     *        the characters which are allowed or forbidden to enter
     * @param forbidden
     *        if <code>false</code>, only the specified characters are allowed to enter; if
     *        <code>true</code>, all but the specified characters are allowed to enter
     */
    public AllowedCharactersOnlyConstraint(String characters, boolean forbidden) {
        this(characters, forbidden, true);
    }

    /**
     * Creates a AllowedCharactersOnlyConstraint with the specified allowed or forbidden
     * characters.
     *
     * @param characters
     *        the characters which are allowed or forbidden to enter
     * @param forbidden
     *        if <code>false</code>, only the specified characters are allowed to enter; if
     *        <code>true</code>, all but the specified characters are allowed to enter
     * @param ignoreCase
     *        if <code>true</code>, case is ignored while checking the constraint
     */
    public AllowedCharactersOnlyConstraint(String characters, boolean forbidden, boolean ignoreCase) {
        this.characters = characters == null ? "" : (ignoreCase ? characters.toLowerCase() : characters);
        this.forbidden = forbidden;
        this.ignoreCase = ignoreCase;
    }



    @Override
	protected boolean checkString(String aValue) throws CheckException {
        if (aValue == null) return true;
        if (ignoreCase) aValue = aValue.toLowerCase();
        for (int i = 0, length = aValue.length(); i < length; i++) {
            int index = characters.indexOf(aValue.charAt(i));
            if ( (forbidden && index >= 0) || (!forbidden && index < 0) ) {
				throw new CheckException(Resources.getInstance().getString(I18NConstants.INVALID_CHARACTERS));
            }
        }
        return true;
    }

}
