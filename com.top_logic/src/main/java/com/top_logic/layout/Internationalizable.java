/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.basic.util.ResKey;
import com.top_logic.mig.html.I18NResourceProvider;

/**
 * An internationalizable object provides a {@link ResKey} for its display in human readable form.
 * 
 * @see I18NResourceProvider
 * 
 * @author <a href="mailto:tdi@top-logic.com">tdi</a>
 */
public interface Internationalizable {

    /**
	 * {@link ResKey} key for displaying this instance.
	 * 
	 * @param optionalPrefix
	 *        Optional {@link ResPrefix} to build the key from. May be <code>null</code>. In that
	 *        case, global keys must be built. Even if the value is non-<code>null</code>, the
	 *        implementation is not required to use the given prefix.
	 */
    public ResKey getI18NKey(ResPrefix optionalPrefix);
    
}

