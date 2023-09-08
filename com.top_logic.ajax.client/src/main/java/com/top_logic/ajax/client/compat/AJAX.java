/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ajax.client.compat;

import jsinterop.annotations.JsType;

/**
 * The beginning of a Java interface for the code in the "simpleajax.js" file.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@JsType(isNative = true, namespace = "services", name = "ajax")
public class AJAX {

	public static native void execute(String command, AJAXArguments args);

	public static native void execute(String command, AJAXArguments args, boolean useWaitPane);

}
