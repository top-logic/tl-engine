/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ajax.server.util;

import static com.top_logic.ajax.shared.api.NamingConstants.*;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Utilities for creating JS controls on the client.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class JSControlUtil {

	private static final String SERVICES_AJAX_EXECUTE_AFTER_RENDERING_METHOD = "services.ajax.executeAfterRendering";

	private static final String SERVICES_GWT_EXECUTE_AFTER_LOAD_METHOD = "services.gwt.executeAfterLoad";

	private static final String WINDOW = "window";

	private static final String FUNCTION_BEGIN_DECLARATION = "function() {";

	private static final String FUNCTION_END_DECLARATION = "}";

	private static final String NAMESPACE_SEPARATOR = ".";

	private static final String METHOD_ARGUMENT_SEPARATOR = ",";

	private static final String METHOD_END_ARGUMENTS = ");";

	private static final String METHOD_BEGIN_ARGUMENTS = "(";

	/**
	 * Calls the init method from UIService which uses the registered factory method for the given
	 * controlType to create the client-side control.
	 *
	 * Note: The window load event fires before the GWT scripts are loaded and completely
	 * initialized. Therefore calls to GWT defined code must be delayed until its EntryPoint
	 * callback has been invoked.
	 *
	 * @param out
	 *        The {@link TagWriter} creating the page output.
	 * @param type
	 *        The client-side control type to create.
	 */
	public static void writeCreateJSControlScript(TagWriter out, String type, String id, Object... args)
			throws IOException {
		out.beginScript();

		if (out.getStack().contains(HTMLConstants.HTML)) {
			writeExecuteAfterLoad(out, type, id, args);
		} else {
			writeExecuteAfterRendering(out, type, id, args);
		}

		out.endScript();
	}

	private static void writeExecuteAfterRendering(TagWriter out, String type, String id, Object... args)
			throws IOException {
		out.append(SERVICES_AJAX_EXECUTE_AFTER_RENDERING_METHOD);
		out.append(METHOD_BEGIN_ARGUMENTS);

		out.write(WINDOW);
		out.write(METHOD_ARGUMENT_SEPARATOR);
		writeInit(out, type, id, args);

		out.append(METHOD_END_ARGUMENTS);
	}

	private static void writeExecuteAfterLoad(TagWriter out, String type, String id, Object... args)
			throws IOException {
		out.append(SERVICES_GWT_EXECUTE_AFTER_LOAD_METHOD);
		out.append(METHOD_BEGIN_ARGUMENTS);

		out.writeJsString(type);
		out.write(METHOD_ARGUMENT_SEPARATOR);
		out.write(WINDOW);
		out.write(METHOD_ARGUMENT_SEPARATOR);
		writeInit(out, type, id, args);

		out.append(METHOD_END_ARGUMENTS);
	}

	private static void writeInit(TagWriter out, String type, String id, Object... args) throws IOException {
		out.append(FUNCTION_BEGIN_DECLARATION);
		
		writeInitName(out);
		writeInitArguments(out, type, id, args);

		out.append(FUNCTION_END_DECLARATION);
	}

	private static void writeInitArguments(TagWriter out, String type, String id, Object... args) throws IOException {
		out.write(METHOD_BEGIN_ARGUMENTS);
		out.writeJsLiteral(type);
		out.write(METHOD_ARGUMENT_SEPARATOR);
		out.writeJsLiteral(id);
		for (Object argument : args) {
			out.write(METHOD_ARGUMENT_SEPARATOR);
			out.writeJsLiteral(argument);
		}
		out.write(METHOD_END_ARGUMENTS);
	}

	private static void writeInitName(TagWriter out) throws IOException {
		out.write(SERVICE_NAMESPACE);
		out.write(NAMESPACE_SEPARATOR);
		out.write(SERVICE_NAME);
		out.write(NAMESPACE_SEPARATOR);
		out.write(INIT);
	}

}
