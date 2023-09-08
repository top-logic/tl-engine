/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services.simpleajax;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundCommand;

/**
 * Constants defining the top-level communication protocol between client-side
 * handlers of AJAX-enabled components and server-side command handling.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface AJAXConstants {

	/**
	 * Namespace of XML schema instances.
	 */
	public static final String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";

	/**
	 * {@link #XSI_NAMESPACE} defining attribute.
	 */
	public static final String XSI_XMLNS_ATTRIBUTE = "xmlns:xsi";
	
	/**
	 * {@link #XSI_NAMESPACE} attribute declaring the concrete type of an
	 * element.
	 */
	public static final String XSI_TYPE_ATTRIBUTE = "xsi:type";

	/**
	 * Namespace of the <i>TopLogic</i> AJAX protocol.
	 * 
	 * @see "simpleajax.js AJAX_NS"
     * @see "simpleajax.xsd"
	 */
	public static final String AJAX_NAMESPACE = "http://top-logic.com/base/service/ajax";
	
	/**
	 * {@link #AJAX_NAMESPACE} defining attribute.
	 */
	public static final String AJAX_XMLNS_ATTRIBUTE = "xmlns:ajax";

	/**
	 * Attribute on an {@link #AJAX_ACTION_ELEMENT} and {@link #AJAX_ACTIONS_ELEMENT} that
	 * determines the {@link ClientAction#writeAsXML(DisplayContext, TagWriter, String) context}, in which a
	 * {@link ClientAction} should be executed.
	 * <p>
	 * If an {@link #AJAX_ACTION_ELEMENT} does not provide its own context, the context of the
	 * corresponding {@link #AJAX_ACTIONS_ELEMENT} is used.
	 * </p>
	 */
	public static final String AJAX_CONTEXT_ATTRIBUTE = "context";
	
	/**
	 * Collection of {@link #AJAX_ACTIONS_ELEMENT}s.
	 */
	public static final String AJAX_ACTIONS_ELEMENT = "ajax:actions";
	
	/**
	 * Polymorphic container for any transmitted {@link ClientAction}.
	 * 
	 * <p>
	 * The concrete type of action is identified by {@link #XSI_TYPE_ATTRIBUTE}.
	 * The corresponding type is defined by {@link ClientAction#getXSIType()} in
	 * a concrete sub-class of {@link ClientAction}.
	 * </p>
	 */
	public static final String AJAX_ACTION_ELEMENT = "ajax:action";

	/**
	 * Representation of a {@link JSSnipplet}.
	 */
	public static final String AJAX_CODE_ELEMENT = "ajax:code";
	
	/**
	 * Representation of the update transmitted in a {@link DOMModification}.
	 */
	public static final String AJAX_FRAGMENT_ELEMENT = "ajax:fragment";
	
	/**
	 * Pointer to the first (or only) element affected by a {@link DOMAction}.
	 */
	public static final String AJAX_ID_ELEMENT = "ajax:id";
	
	/**
	 * Pointer to the last element affected by a {@link DOMAction}.
	 */
	public static final String AJAX_STOP_ID_ELEMENT = "ajax:stop-id";
	
	/**
	 * Denotes the {@link FragmentInsertion#getPosition() position} of a
	 * {@link FragmentInsertion}.
	 * 
	 * @see #AJAX_POSITION_ATTRIBUTE
	 * @see #AJAX_POSITION_BEFORE_VALUE
	 * @see #AJAX_POSITION_AFTER_VALUE
	 * @see #AJAX_POSITION_START_VALUE
	 * @see #AJAX_POSITION_END_VALUE
	 */
	public static final String AJAX_POSITION_ATTRIBUTE = "position";
	
	/**
	 * Possible value of {@link #AJAX_POSITION_ATTRIBUTE}.
	 * 
	 * <p>
	 * Denotes that a fragment should be inserted immediately before the
	 * referenced element.
	 * </p>
	 * 
	 * @see #AJAX_POSITION_ATTRIBUTE
	 * @see #AJAX_POSITION_BEFORE_VALUE
	 * @see #AJAX_POSITION_AFTER_VALUE
	 * @see #AJAX_POSITION_START_VALUE
	 * @see #AJAX_POSITION_END_VALUE
	 * @see "DOMNode.insertAdjacentHTML(position,HTML_string)"
	 */
	public static final String AJAX_POSITION_BEFORE_VALUE = "beforebegin";
	
	/**
	 * Possible value of {@link #AJAX_POSITION_ATTRIBUTE}.
	 * 
	 * <p>
	 * Denotes that a fragment should be inserted immediately after the
	 * referenced element.
	 * </p>
	 * 
	 * @see #AJAX_POSITION_ATTRIBUTE
	 * @see #AJAX_POSITION_BEFORE_VALUE
	 * @see #AJAX_POSITION_AFTER_VALUE
	 * @see #AJAX_POSITION_START_VALUE
	 * @see #AJAX_POSITION_END_VALUE
	 * @see "DOMNode.insertAdjacentHTML(position,HTML_string)"
	 */
	public static final String AJAX_POSITION_AFTER_VALUE = "afterend";

	/**
	 * Possible value of {@link #AJAX_POSITION_ATTRIBUTE}.
	 * 
	 * <p>
	 * Denotes that a fragment should be inserted at the start of the content of
	 * the referenced element.
	 * </p>
	 * 
	 * @see #AJAX_POSITION_ATTRIBUTE
	 * @see #AJAX_POSITION_BEFORE_VALUE
	 * @see #AJAX_POSITION_AFTER_VALUE
	 * @see #AJAX_POSITION_START_VALUE
	 * @see #AJAX_POSITION_END_VALUE
	 * @see "DOMNode.insertAdjacentHTML(position,HTML_string)"
	 */
	public static final String AJAX_POSITION_START_VALUE = "afterbegin";

	/**
	 * Possible value of {@link #AJAX_POSITION_ATTRIBUTE}.
	 * 
	 * <p>
	 * Denotes that a fragment should be inserted at the end of the content of
	 * the referenced element.
	 * </p>
	 * 
	 * @see #AJAX_POSITION_ATTRIBUTE
	 * @see #AJAX_POSITION_BEFORE_VALUE
	 * @see #AJAX_POSITION_AFTER_VALUE
	 * @see #AJAX_POSITION_START_VALUE
	 * @see #AJAX_POSITION_END_VALUE
	 * @see "DOMNode.insertAdjacentHTML(position,HTML_string)"
	 */
	public static final String AJAX_POSITION_END_VALUE = "beforeend";

	/**
	 * Denotes the {@link BoundCommand#getID()} of the transmitted command.
	 */
	public static final String COMMAND_ELEMENT = "command";
	
	/**
	 * Encodes the AJAX sequence number (see
	 * {@link RequestLock#enterWriter(Integer)}) of a command.
	 */
	public static final String SEQUENCE_ELEMENT = "sequence";
	
	/**
	 * Encodes an AJAX sequence number acknowledgement message.
	 */
	public static final String ACK_ELEMENT = "ack";

	/**
	 * Header field that transports the current submit number of the component that generated the
	 * request.
	 */
	public static final String SUBMIT_ELEMENT = "submit";
	
	/**
	 * Encodes a {@link #SEQUENCE_ELEMENT sequence number} that is transmitted
	 * to the client-side.
	 */
	public static final String TX_ATTRIBUTE = "tx";
	
	/**
	 * Encodes a {@link #SEQUENCE_ELEMENT sequence number} that is received
	 * to the client-side.
	 */
	public static final String RX_ATTRIBUTE = "rx";
	
	/**
	 * Identifies a dropped command due to an {@link AJAXOutOfSequenceException}.
	 */
	public static final String DROPPED_ATTRIBUTE = "dropped";
	
	/**
	 * Value of {@link #DROPPED_ATTRIBUTE}.
	 */
	public static final String DROPPED_ATTRIBUTE_VALUE = "dropped";
	
	/**
	 * Container for the {@link #SOURCE_ID_ATTRIBUTE} and
	 * {@link #TARGET_ID_ATTRIBUTE} of the source and target components of an
	 * AJAX command.
	 */
	public static final String TARGET_COMPONENT_ELEMENT = "component";

	/**
	 * Encodes the {@link LayoutComponent#getName() name} of the source component of the request.
	 */
	public static final String SOURCE_ID_ATTRIBUTE = "source";

	/**
	 * Encodes the {@link LayoutComponent#getName() name} of the target component of a request.
	 */
	public static final String TARGET_ID_ATTRIBUTE = "target";
	
	/**
	 * Container for a {@link #NAME_ELEMENT}/{@link #VALUE_ELEMENT} pair.
	 */
	public static final String ARGUMENT_ELEMENT = "argument";

	/**
	 * The name of an additional argument to an AJAX command.
	 */
	public static final String VALUE_ELEMENT = "value";
	
	/**
	 * Container for the value of an additional argument to an
	 * AJAX command.
	 */
	public static final String NAME_ELEMENT = "name";
	
	/** name of the element that represents the informations for one execute */
	public static final String EXECUTE_ELEMENT = "execute";

	/**
	 * Element transmitting the request sender information (component ID and window name).
	 */
	public static final String SOURCE_ELEMENT = "source";

	/**
	 * Attribute of {@link #SOURCE_ELEMENT} that transmitts the window name of the sender.
	 */
	public static final String WINDOW_ATTRIBUTE = "window";

	/**
	 * Encodes a property of a {@link PropertyUpdate} action.
	 */
	public static final String AJAX_PROPERTY = "ajax:property";

	/**
	 * Encodes the function reference of a {@link JSFunctionCall} action.
	 */
	public static final String AJAX_FUNCTION = "ajax:function";

	/**
	 * Encodes the arguments of a {@link JSFunctionCall} action.
	 */
	public static final String AJAX_ARGUMENTS = "ajax:arguments";
	
	/**
	 * Encodes the name of an {@link #AJAX_PROPERTY} or {@link #AJAX_FUNCTION}.
	 */
	public static final String NAME_ATTRIBUTE = "name";
	
	/**
	 * Encodes the reference on which an {@link #AJAX_FUNCTION} is invoked.
	 */
	public static final String REF_ATTRIBUTE = "ref";

	/**
	 * Encodes the reference, which triggered the AJAX request
	 */
	public static final String REQUEST_SOURCE_REFERENCE_ATTRIBUTE = "requestsourcereference";

	/**
	 * Encodes the value of an {@link #AJAX_PROPERTY}, or a header property such as
	 * {@link #SUBMIT_ELEMENT}.
	 */
	public static final String VALUE_ATTRIBUTE = "value";

}
