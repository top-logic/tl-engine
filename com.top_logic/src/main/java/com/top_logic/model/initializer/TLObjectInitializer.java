/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.initializer;

import com.top_logic.model.TLObject;
import com.top_logic.model.annotate.TLObjectInitializers;

/**
 * Initializer to run when a new {@link TLObject} is created.
 * 
 * @see TLObjectInitializers
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TLObjectInitializer {

	/**
	 * Initializes the newly created object.
	 * 
	 * @param context
	 *        The context in which the new object was needed, e.g. when the object is a new child of
	 *        an structured element, the context is the parent. May be <code>null</code>.
	 * @param object
	 *        The newly created element to initialize.
	 * @param initForUI
	 *        Whether the new object was created for the UI.
	 */
	void initializeObject(Object context, TLObject object, boolean initForUI);
}
