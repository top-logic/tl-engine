/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services.simpleajax;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;

/**
 * Object representation of an action that is sent to a client in response to an
 * AJAX request.
 * 
 * <p>
 * An action result may either modify the DOM of the currently displayed page,
 * or execute an arbitrary JavaScript snipplet on the client machine. This is the
 * abstract base class for all client actions.
 * </p>
 * 
 * <p>
 * In their XML representation, a concrete client action is embedded in an
 * <code>ajax:action</code> element with a <code>xsi:type</code> attribute
 * that announces the concrete type of the action.
 * </p>
 * 
 * <p>
 * The XML format of this class is given by the following RelaxNG grammar (in
 * compact form):
 * </p>
 * 
 * <pre>
 * 
 * namespace xsi = http://www.w3.org/2001/XMLSchema-instance
 * namespace ajax = http://top-logic.com/base/services/ajax
 * 
 * element ajax:action {
 *    attribute context
 *       
 *    (
 *       attribute xsi:type { "ContentReplacement" }
 *       def{@link ContentReplacement}Contents
 *    )
 *    (
 *       attribute xsi:type { "ElementReplacement" }
 *       def{@link ElementReplacement}Contents
 *    )
 *    (
 *       attribute xsi:type { "RangeReplacement" }
 *       def{@link RangeReplacement}Contents
 *    )
 *    (
 *       attribute xsi:type { "FragmentInsertion" }
 *       def{@link FragmentInsertion}Contents
 *    )
 *    (
 *       attribute xsi:type { "JSSnipplet" }
 *       def{@link JSSnipplet}Contents
 *    )
 * }
 * </pre>
 * 
 * Concrete actions include the following:
 * 
 * @see ContentReplacement
 * @see ElementReplacement
 * @see RangeReplacement
 * @see FragmentInsertion
 * @see JSSnipplet
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ClientAction {

	/**
     * Convenience constant that can be used
     * if no actions on the client side are necessary in response of this
     * command.
     */
    public static final ClientAction[] NO_ACTIONS = new ClientAction[] {};

	
    public static final String XSI_NAMESPACE_DECL =
		AJAXConstants.XSI_XMLNS_ATTRIBUTE + "=\"" + AJAXConstants.XSI_NAMESPACE + "\"";
    
    /** 
     * Create a new {@link ClientAction}
     */
    protected ClientAction() {
    }

	/**
	 * The type of the concrete action. This value is provided by a concrete
	 * subclass. The result of this method is used in the <code>xsi:type</code>
	 * attribute in the XML serialized form of this class.
	 * 
	 * @see #writeFieldsAsXML(TagWriter)
	 * @see "simpleajax.js AJAXServiceClass.processAction()"
	 */
	protected abstract String getXSIType();

	/**
	 * Dispatches to {@link #writeAsXML(DisplayContext, TagWriter, String)} with
	 * <code>null</code> as context component, i.e. this action will be
	 * evaluated in the main layout.
	 * 
	 * @see #writeAsXML(DisplayContext, TagWriter, String)
	 */
	public final void writeAsXML(DisplayContext aContext, TagWriter writer) throws IOException {
		writeAsXML(aContext, writer, null);
	}

	/**
	 * Main entry point for XML serialization. This method writes the complete
	 * object with all referenced objects to the given {@link TagWriter}.
	 * 
	 * @param contextComponent
	 *        the {@link AJAXConstants#AJAX_CONTEXT_ATTRIBUTE} of this
	 *        {@link ClientAction}. Its the component in which this
	 *        {@link ClientAction} will be executed.
	 */
	public final void writeAsXML(DisplayContext aContext, TagWriter writer, String contextComponent) throws IOException {
		writer.beginBeginTag(AJAXConstants.AJAX_ACTION_ELEMENT);
		writer.writeAttribute(AJAXConstants.AJAX_CONTEXT_ATTRIBUTE, contextComponent);
		this.writeFieldsAsXML(writer);
		writer.endBeginTag();

		this.writeChildrenAsXML(aContext, writer);
	
		writer.endTag(AJAXConstants.AJAX_ACTION_ELEMENT);
	}

	/**
	 * Write all attributes to the given {@link TagWriter}.
	 */
	protected void writeFieldsAsXML(TagWriter out) throws IOException {
        // out.writeAttribute(AJAXConstants.XSI_XMLNS_ATTRIBUTE   , AJAXConstants.XSI_NAMESPACE);
	    out.writeContent(XSI_NAMESPACE_DECL);
		out.writeAttribute(AJAXConstants.XSI_TYPE_ATTRIBUTE    , getXSIType());
	}

	/**
	 * Write all referenced objects as child elements to the given
	 * {@link TagWriter}.
	 */
	protected abstract void writeChildrenAsXML(DisplayContext aContext, TagWriter writer) throws IOException;

}
