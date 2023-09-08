/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

/**
 * Interface for allowing access to underlying implementations through (a chain)
 * of adapters.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface WrappedModel {

    /**
     * The original object wrapped by this adapter model.
     */ 
	Object getWrappedModel();
	
}

