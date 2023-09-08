/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.genericimport;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Constants used for generic import assistent
 * 
 * @author     <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class I18NConstants extends I18NConstantsBase {

	public static ResPrefix IMPORT_CONFIG = legacyPrefix("genericImport.config");

    /** @see I18NConstantsBase */
    public static ResKey UPLOAD_NOFILE;
    
    public static ResKey ERROR_FOREIGN_KEY_NOT_MAPPED;
    
    static {
        initConstants(I18NConstants.class);
    }
}
