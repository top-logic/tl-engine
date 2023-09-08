/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services.simpleajax;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;

/**
 * Calls a JavaScript function.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class JSFunctionCall extends DOMAction {

	public static final String FUNCTION_CALL_XSI_TYPE = "FunctionCall";

	/**
	 * @see #getFunctionReference()
	 */
	private final String functionReference;
	
	/**
	 * @see #getFunctionName()
	 */
	private final String functionName;
	
	/**
	 * @see #getArguments()
	 */
	private final Object[] arguments;

	/**
	 * Creates a new JavaScript function call.
	 * 
	 * @param elementID
	 *        The ID of the DOM element passed as first argument.
	 * @param functionReference
	 *        see {@link #getFunctionReference()}
	 * @param functionName
	 *        see {@link #getFunctionName()}
	 * @param arguments
	 *        see {@link #getArguments()}
	 */
	public JSFunctionCall(String elementID, String functionReference, String functionName, Object... arguments) {
		super(elementID);

		this.functionReference = functionReference;
		this.functionName = functionName;
		this.arguments = arguments;
	}

	@Override
	protected String getXSIType() {
		return FUNCTION_CALL_XSI_TYPE;
	}
	
	/**
	 * A reference to a JavaScript object defining a function with name
	 * {@link #getFunctionName()}.
	 */
	public String getFunctionReference() {
		return functionReference;
	}
	
	/**
	 * The name of the function that is invoked.
	 */
	public String getFunctionName() {
		return functionName;
	}
	
	/**
	 * Further arguments of the function.
	 */
	public Object[] getArguments() {
		return arguments;
	}

	@Override
	protected void writeChildrenAsXML(DisplayContext context, TagWriter writer) throws IOException {
		super.writeChildrenAsXML(context, writer);
		
		writer.beginBeginTag(AJAXConstants.AJAX_FUNCTION);
		writer.writeAttribute(AJAXConstants.REF_ATTRIBUTE, functionReference);
		writer.writeAttribute(AJAXConstants.NAME_ATTRIBUTE, functionName);
		writer.endEmptyTag();
		
		writer.beginTag(AJAXConstants.AJAX_ARGUMENTS);
		new XMLValueEncoder(writer).encode(arguments);
		writer.endTag(AJAXConstants.AJAX_ARGUMENTS);
	}

	/**
	 * Creates a {@link JSFunctionCall} that sets a new style string at the
	 * element with the given id.
	 * 
	 * @param id
	 *        The client-side element ID, whose style should be updated.
	 * @param newStyle
	 *        The new style string to set.
	 * @return The action that updates the style.
	 */
	public static ClientAction setStyle(String id, String newStyle) {
		return new JSFunctionCall(id, "BAL", "setStyle", newStyle);
	}
}
