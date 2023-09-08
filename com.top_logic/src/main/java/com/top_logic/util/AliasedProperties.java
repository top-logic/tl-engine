/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

// First import java...
import java.util.Properties;

import com.top_logic.basic.AliasManager;

// then import javax...
// then other libraries like org...
// then our project specific packages, like com.top_logic.mig..., com.top_logic...

/**
 * Deviates from the parent in that all (String) values that
 * are added are run through
 * {@link com.top_logic.basic.AliasManager}.
 * <br/>
 * Note that this does not apply to any default properties
 * (if specified).
 * 
 * @author    <a href="mailto:mer@top-logic.com">Michael Eriksson</a>
 */
public class AliasedProperties extends Properties {

     @Override
	public synchronized Object put(Object key, Object value) {
         Object theAliasedValue = value;

         if (theAliasedValue instanceof String) {
            theAliasedValue = AliasManager.getInstance ()
                                          .replace ((String) theAliasedValue);
         }

         return super.put (key, theAliasedValue);
     }
}

