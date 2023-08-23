/*
 * Copyright (c) 2019 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.services.jms;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Error establishing connection to {0}.
	 */
	public static ResKey1 ERROR_ESTABLISH_CONNECTION__NAME;

	static {
		initConstants(I18NConstants.class);
	}
}
