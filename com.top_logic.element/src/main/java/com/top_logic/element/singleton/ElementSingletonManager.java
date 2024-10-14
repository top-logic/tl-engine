/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.singleton;

import com.top_logic.basic.StringServices;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;

/**
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class ElementSingletonManager {

    public static final String SINGLETON_PREFIX_STRUCTURE_ROOT = "structureRoot@";

    /**
	 * Get the single unique object registered under the given id
	 *
	 * @param singletonReference
	 *        a singleton reference
	 * @return NULL in case no object is registered for the given reference
	 */
	public static TLObject getSingleton(String singletonReference) {
		if (StringServices.isEmpty(singletonReference)) {
            return null;
        }
		if (singletonReference.startsWith(SINGLETON_PREFIX_STRUCTURE_ROOT)) {
			String moduleName = singletonReference.substring(SINGLETON_PREFIX_STRUCTURE_ROOT.length());
			final TLModule module = DynamicModelService.getApplicationModel().getModule(moduleName);
			return module == null ? null : module.getSingleton(TLModule.DEFAULT_SINGLETON_NAME);
        }
        return null;
    }

}

