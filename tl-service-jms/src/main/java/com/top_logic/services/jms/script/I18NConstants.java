/*
 * Copyright (c) 2019 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.services.jms.script;

import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en No connection with name {0}: {1}
	 */
	public static ResKey2 ERROR_NO_SUCH_CONNECTION__NAME_EXPR;

	static {
		initConstants(I18NConstants.class);
	}
}
