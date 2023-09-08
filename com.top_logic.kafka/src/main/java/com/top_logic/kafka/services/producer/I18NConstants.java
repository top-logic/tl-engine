/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.services.producer;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;

/**
 * {@link I18NConstants} definition for this package.
 * 
 * @author <a href=mailto:wta@top-logic.com>wta</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * The {@link ResKey} to be used for L10N of error messages upon producer creation failure.
	 */
	public static ResKey PRODUCER_INSTANTIATION_ERROR;

	/* initialize constants with their appropriate value */
	static {
		initConstants(I18NConstants.class);
	}
}
