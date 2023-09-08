/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services.simpleajax;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * I18N constants for generic AJAX error messages.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/** @see I18NConstantsBase */
	public static ResKey REQUEST_TIMEOUT_RELOAD_REQUIRED;

	@JavaScriptResKey
	public static ResKey INCONSISTENT_SERVER_REPIES;

	@JavaScriptResKey
	public static ResKey PLEASE_WAIT_PAGE_BEING_LOADED;
	public static ResKey ILLEGAL_REQUEST_IGNORED;
	public static ResKey SESSION_TIMED_OUT_REDIRECT_LOGIN;

	@JavaScriptResKey
	public static ResKey SERVER_CLIENT_COMMUNICATION_CRASHED;
	public static ResKey MAX_NUMBER_REQUESTS_RECEIVED;

	/**
	 * Used on the client to report an invalid XML response.
	 */
	@JavaScriptResKey
	public static ResKey INVALID_XML_RESPONSE;

	/**
	 * Used on the client to report a communication link failure.
	 */
	@JavaScriptResKey
	public static ResKey NETWORK_ERROR;
	
	/**
	 * Message displayed while retrying transmission.
	 */
	@JavaScriptResKey
	public static ResKey NETWORK_ERROR_TRYING_RECONNECT;

	/**
	 * Message displayed while retrying transmission.
	 */
	@JavaScriptResKey
	public static ResKey NETWORK_ERROR_RECONNECT_FAILED;

	@JavaScriptResKey
	public static ResKey SETTION_TIMEOUT_FAILURE;

	@JavaScriptResKey
	public static ResKey SETTION_TIMEOUT_LOGIN;

	/**
	 * Button to retry reconnect.
	 */
	@JavaScriptResKey
	public static ResKey NETWORK_ERROR_RETRY;

	public static ResKey RENDERING_ERROR;

	static {
		initConstants(I18NConstants.class);
	}
}
