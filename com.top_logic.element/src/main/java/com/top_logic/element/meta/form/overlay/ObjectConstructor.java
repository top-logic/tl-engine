/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.overlay;

import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.model.TLObject;

/**
 * Constructor function creating a new {@link TLObject} from form values.
 * 
 * @see AttributeUpdateContainer#createObject(com.top_logic.model.TLStructuredType, String,
 *      TLObject, ObjectConstructor)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ObjectConstructor {

	/**
	 * Instantiates the given transient object.
	 */
	TLObject newInstance(TLFormObject overlay);

}
