/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.layout.kafka;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;

/**
 * {@link I18NConstants} definition for this package.
 * 
 * @author <a href=mailto:wta@top-logic.com>wta</a>
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey MESSAGE_DETAIL_TITLE;

	public static ResKey MESSAGE_DETAIL_TOPIC;

	public static ResKey MESSAGE_DETAIL_TIMESTAMP;

	public static ResKey MESSAGE_DETAIL_KEY;

	public static ResKey MESSAGE_DETAIL_VALUE;

	/* initialize constants with their appropriate value */
	static {
		initConstants(I18NConstants.class);
	}
}