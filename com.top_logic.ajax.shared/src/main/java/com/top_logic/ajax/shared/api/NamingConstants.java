/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ajax.shared.api;

/**
 * Constants for referring to client-side GWT scripts from server-side controls.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NamingConstants {

	/**
	 * The namespace of the GWT code.
	 */
	public static final String SERVICE_NAMESPACE = "tl.service";

	/**
	 * The name of the JavaScript service object created from GWT code.
	 */
	public static final String SERVICE_NAME = "UIService";

	/** The name of the method that initializes a client-side twin of a server-side control. */
	public static final String INIT = "init";

	/**
	 * The name of the method that sends a command to a client-side twin of a server-side control.
	 */
	public static final String INVOKE = "invoke";

}
