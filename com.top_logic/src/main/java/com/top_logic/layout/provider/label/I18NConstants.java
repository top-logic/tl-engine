/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider.label;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.layout.I18NConstantsBase;

/**
 * {@link I18NConstants} for this package.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/** Label key for the current {@link Revision}. */
	public static ResKey CURRENT_REVISION_LABEL;

	/** Label key for a persistent {@link Revision}. */
	public static ResKey1 REVISION_LABEL__COMMIT_NUMBER;
	
	static {
		initConstants(I18NConstants.class);
	}
}
