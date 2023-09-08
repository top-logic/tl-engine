/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.wrap;

import java.util.Locale;

import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.gui.GroupResourceProvider;
import com.top_logic.layout.ResPrefix;
import com.top_logic.util.Resources;

/**
 * Utilities for {@link GroupResourceProvider}.
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class GroupResourceHelper {
    
    public static final String I18N_PREFIX_GROUP             = "group";
    public static final String I18N_INFIX_NAME               = "name";
    public static final String I18N_INFIX_DESCRIPTION        = "description";
    public static final String I18N_SEPARATOR                = ".";

    /** i18n group name prefix */
	public static final ResPrefix I18N_PREFIX_GROUP_NAME = I18NConstants.NAME;

    /** i18n group description prefix */
	public static final ResPrefix I18N_PREFIX_GROUP_DESCRIPTION = I18NConstants.DESCRIPTION;
    
    /**
	 * This method searches the resource bundels for the i18n display name
	 * 
	 * @param locale
	 *        e.g. "de", "en"
	 * @return the i18n display name or the missing resource key in braces
	 */
	public static String findI18NName(Group aGroup, Locale locale) {
		return Resources.getInstance(locale).getString(getResKey(aGroup));
    }
    
    /**
	 * This method searches the resource bundels for the i18n display name
	 * 
	 * @param locale
	 *        e.g. "de", "en"
	 * @return the i18n display name or the missing resource key in braces
	 */
	public static String findI18NDescription(Group aGroup, Locale locale) {
		return Resources.getInstance(locale).getString(getDescriptionResKey(aGroup));
    }
    
    /**
     * group.name.$groupID$ 
     */
	public static ResKey getResKey(Group aGroup) {
        if (aGroup != null) {
			String name = aGroup.getName();
			return ResKey.fallback(I18N_PREFIX_GROUP_NAME.key(name), ResKey.text(name));
        }
		return I18N_PREFIX_GROUP_NAME.key("null");
    }
    
    /**
     * group.name.$groupID$ 
     */
	public static ResKey getDescriptionResKey(Group aGroup) {
        if (aGroup != null) {
			return ResKey.fallback(I18N_PREFIX_GROUP_DESCRIPTION.key(aGroup.getName()), ResKey.text(""));
        }
		return I18N_PREFIX_GROUP_DESCRIPTION.key("null");
    }

}

