/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.script;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;

/**
 * {@link I18NConstants} definition for this package.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/** @en The Kafka service is not started. */
	public static ResKey ERROR_KAFKA_SERVICE_NOT_STARTED;

	/* initialize constants with their appropriate value */
	static {
		initConstants(I18NConstants.class);
	}
}
