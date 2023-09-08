/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services.simpleajax;

import java.io.IOException;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.AbstractDisplayValue;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DynamicText;
import com.top_logic.layout.LinkGenerator;
import com.top_logic.layout.LinkGenerator.Handle;
import com.top_logic.layout.basic.Command;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Executes a given snipplet of JavaScript in the context of the client's page.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class JSSnipplet extends ClientAction {
	
	public static final String JS_SNIPPLET_XSI_TYPE = "JSSnipplet";

	private DynamicText fragment;
	private String code;
	
	/**
	 * Evaluate the given code in the context of the client's page. 
	 */
	public JSSnipplet(String code) {
		this.code = code;
	}

	public JSSnipplet(DynamicText fragment) {
    	this.fragment = fragment;
    }
    
    @Override
	protected String getXSIType() {
		return JS_SNIPPLET_XSI_TYPE;
	}

	@Override
	protected void writeChildrenAsXML(DisplayContext context, TagWriter writer) throws IOException {
		writer.beginTag(AJAXConstants.AJAX_CODE_ELEMENT);
		int currentDepth = writer.getDepth();
		try {
			if (fragment != null) {
				fragment.append(context, writer);
			}
			else {
				writer.writeText(code);
			}
		} catch (Throwable throwable) {
			writer.endAll(currentDepth);
			Logger.error("Could not create js snippet (Cause: " + throwable.getMessage(), throwable, this);
		}
		writer.endTag(AJAXConstants.AJAX_CODE_ELEMENT);
	}

	/**
	 * Creates a one-time client-side call to the given {@link Command}.
	 * 
	 * @param command
	 *        The command to schedule execution for on the client.
	 * @return The {@link ClientAction} that schedules the given command for execution.
	 */
	public static JSSnipplet createServerCallback(final Command command) {
		return createServerCallback(0L, command);
	}

	/**
	 * Creates a one-time client-side call to the given {@link Command}.
	 * 
	 * @param timeout
	 *        The time in milliseconds to wait on the client before sending the new request. A value
	 *        of <code>0</code> means no timeout at all.
	 * @param command
	 *        The command to schedule execution for on the client.
	 * @return The {@link ClientAction} that schedules the given command for execution.
	 */
	public static JSSnipplet createServerCallback(final long timeout, final Command command) {
		return new JSSnipplet(new AbstractDisplayValue() {
			@Override
			public void append(DisplayContext renderContext, Appendable out) throws IOException {
				boolean hasTimeout = timeout > 0;
				if (hasTimeout) {
					out.append("window.setTimeout(function() {");
				}
				appendCallback(out, renderContext, command);
				if (hasTimeout) {
					out.append("}, ");
					StringServices.append(out, timeout);
					out.append(");");
				}
			}
		});
	}

	static void appendCallback(Appendable out, DisplayContext renderContext, final Command command) throws IOException {
		out.append("services.ajax.executeOnCompletion(function() {");
		appendOneTimeLink(out, renderContext, command);
		out.append("});");
	}
	static void appendOneTimeLink(Appendable out, DisplayContext renderContext, final Command command)
			throws IOException {
		final Handle[] handle = new Handle[1];
		handle[0] = LinkGenerator.renderLink(renderContext, out, new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext executionContext) {
				handle[0].dispose();
				return command.executeCommand(executionContext);
			}
		});
	}

	/**
	 * Factory method for creating a {@link ClientAction} that shows an alert
	 * message in the requesting browser.
	 * 
	 * @param message
	 *     The internationalized message to display in the alert.
	 * @return The created {@link ClientAction}. 
	 */
	public static JSSnipplet createAlert(String message) {
		return new JSSnipplet("alert(" + createJSString(message) + ")");
	}

	/**
	 * Factory method for creating a snipplet that reloads its context window.
	 */
	public static JSSnipplet createPageReload() {
		// Note: Must not use a constant, because JSSnipplet is not immutable.
		return new JSSnipplet("window.location.reload();");
	}

	private static String createJSString(String message) {
		return '"' + StringServices.escape(message) + '"';
	}

}
