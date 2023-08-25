/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.services.jms.script;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en JMS Service not started in {0}.
	 */
	public static ResKey1 ERROR_JMS_SERVICE_NOT_STARTED__EXPR;

	/**
	 * @en Error writing XML message.
	 */
	public static ResKey ERROR_WRITING_XML;

	/**
	 * @en No connection with name {0}: {1}.
	 */
	public static ResKey2 ERROR_NO_SUCH_CONNECTION__NAME_EXPR;

	static {
		initConstants(I18NConstants.class);
	}
}
