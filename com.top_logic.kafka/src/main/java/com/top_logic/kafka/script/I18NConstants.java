/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.script;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;

/**
 * {@link I18NConstants} definition for this package.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/** @en The Kafka service is not started. */
	public static ResKey ERROR_KAFKA_SERVICE_NOT_STARTED;

	/** @en Tried to send a header that has not just a key and a value, but more entries: {0} */
	public static ResKey1 ERROR_HEADER_HAS_TOO_MANY_ENTRIES__HEADER;

	/** @en Tried to send a header without a key. */
	public static ResKey ERROR_HEADER_IS_EMPTY;

	/** @en Tried to send a header with an empty key. */
	public static ResKey ERROR_HEADER_KEY_IS_EMPTY;

	/* initialize constants with their appropriate value */
	static {
		initConstants(I18NConstants.class);
	}
}
