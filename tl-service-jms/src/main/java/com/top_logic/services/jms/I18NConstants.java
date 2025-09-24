/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.services.jms;

import com.top_logic.basic.util.ResKey;
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

	/**
	 * @en Connection to the MQ system lost.
	 */
	public static ResKey ERROR_CONNECTION_LOST;

	/**
	 * @en Please make sure the MQ system is up and running before trying to send a message or
	 *     restarting the JMSService.
	 */
	public static ResKey ERROR_CONNECTION_LOST_DETAILS;

	/**
	 * @en Problem with the connection to the MQ system. Trying to reconnect...
	 */
	public static ResKey ERROR_NO_CONNECTION;

	/**
	 * @en Reconnect successful. Trying to resend message...
	 */
	public static ResKey INFO_RECONNECT_SUCCESS;

	/**
	 * @en Resend successful.
	 */
	public static ResKey INFO_RESEND_SUCCESS;

	/**
	 * @en Reconnect failed.
	 */
	public static ResKey ERROR_RECONNECT_FAILED;

	/**
	 * @en Error sending a message by producer {0}.
	 */
	public static ResKey1 ERROR_SENDING_MSG__NAME;

	/**
	 * @en Error receiving a message by consumer {0}.
	 */
	public static ResKey1 ERROR_RECEIVING_MSG__NAME;

	/**
	 * @en Processed JMS message.
	 */
	public static ResKey PROCESSED_JMS_MESSAGE;

	static {
		initConstants(I18NConstants.class);
	}
}
