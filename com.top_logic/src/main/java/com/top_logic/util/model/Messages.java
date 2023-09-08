/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.model;

import com.top_logic.basic.message.AbstractMessages;
import com.top_logic.model.TLModel;

/**
 * Messages for this package.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Messages extends AbstractMessages {

	/** Transaction name for creating {@link TLModel}. */
	public static Template0 CREATING_TL_MODEL;

	/** Transaction name for initial classification creation by {@link ModelService}. */
	public static Template0 CREATING_CLASSIFICATIONS;

	static {
		init(Messages.class);
	}
}
