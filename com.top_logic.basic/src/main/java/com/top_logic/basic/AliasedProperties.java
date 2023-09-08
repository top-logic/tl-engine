/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.util.Properties;

/**
 * Properties that feed all values to the AliasManager. 
 * 
 * All (String) values that are added are run through the 
 * {@link com.top_logic.basic.AliasManager}.
 * <br/>
 * Note that this does not apply to any default properties
 * (if specified).
 * 
 * @author    <a href="mailto:mer@top-logic.com">Michael Eriksson</a>
 */
public class AliasedProperties extends Properties {

    /** Fetch the AliasManager only once */
    protected AliasManager aliasManager = AliasManager.getInstance ();

     @Override
	public synchronized Object put(Object key, Object value) {

         if (value instanceof String) {
             value = aliasManager.replace ((String) value);
         }

         return super.put (key, value);
     }
}

