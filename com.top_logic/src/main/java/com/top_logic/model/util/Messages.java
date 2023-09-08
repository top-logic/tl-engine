/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.util;

import com.top_logic.basic.message.AbstractMessages;
import com.top_logic.model.TLModelPart;

/**
 * {@link AbstractMessages} for this package.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class Messages extends AbstractMessages {

	/** Commit message used to delete {@link TLModelPart}. */
	public static Template1 DELETED_MODEL_PART_RECURSIVELY__PART_NAME;
	
	static {
		// Call reflection initializer.
		init(Messages.class);
		
		// Call reflection initializer.
		initDefaults(Messages.class);
	}

}
