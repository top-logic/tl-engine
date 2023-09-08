/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ajax.client.service;

import static com.top_logic.ajax.shared.api.NamingConstants.*;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;

import com.top_logic.ajax.client.boot.GWTCompatibility;
import com.top_logic.ajax.client.control.JSControl;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

/**
 * Client-side control code.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@JsType(namespace = SERVICE_NAMESPACE, name = SERVICE_NAME)
public class UIService {

	private static final String DIAGRAM_JS_GRAPH_TYPE = "diagramJSgraph";

	private static final String UMLJS_CONTAINER_SUFFIX = "-umljs-container";

	private static final String TL_CONTROL_PROPERTY = "tlControl";

	private static final Map<String, JSControlFactory> _factories = new HashMap<>();

	/**
	 * Registers a {@link JSControlFactory} for a well-known "control type".
	 *
	 * @param type
	 *        The well-known "control type" that is requested by the server.
	 * @param factory
	 *        A {@link JSControlFactory} creating {@link JSControl}s for the given control type.
	 */
	public static void registerFactory(String type, JSControlFactory factory) {
		_factories.put(type, factory);

		GWTCompatibility.onLoad(type);
	}

	/**
	 * Initializer for a {@link JSControl}.
	 * 
	 * <p>
	 * A call to this method is invoked by the server-side control when creating it's client-side
	 * twin.
	 * </p>
	 * 
	 * @param type
	 *        The well-known type of {@link JSControl} control to create.
	 * @param controlId
	 *        The id of the control writing the graph. Is not allowed to be null.
	 * @param args
	 *        Custom arguments for initializing the given control type.
	 */
	@JsMethod(name = INIT)
	public static void init(String type, String controlId, Object... args) {
		if (DIAGRAM_JS_GRAPH_TYPE.equals(type)) {
			if (Document.get().getElementById(controlId + UMLJS_CONTAINER_SUFFIX) != null) {
				return;
			}
		}

		JSControl control = create(type, controlId);
		control.init(args);
		install(control);
	}

	private static JSControl create(String type, String controlId) {
		JSControlFactory factory = _factories.get(type);
		if (factory == null) {
			throw new IllegalArgumentException("Control type '" + type + "' not supported.");
		}
		return factory.createControl(controlId);
	}

	private static void install(JSControl control) {
		Document.get().getElementById(control.getId()).setPropertyObject(TL_CONTROL_PROPERTY, control);
	}

	/**
	 * Invocation of a custom method on a client-side {@link JSControl}.
	 * 
	 * <p>
	 * An invocation of this method is triggered by an update of the server-side twin of the
	 * targeted {@link JSControl}.
	 * </p>
	 * 
	 * @param controlElement
	 *        The DOM element which is the representation of the targeted {@link JSControl}, see
	 *        {@link JSControl#getId()}.
	 * @param command
	 *        The custom command name.
	 * @param args
	 *        Custom arguments for the given command.
	 */
	@JsMethod(name = INVOKE)
	public static void invoke(Element controlElement, String command, Object... args) {
		control(controlElement).invoke(command, args);
	}

	private static JSControl control(Element controlElement) {
		JSControl control = (JSControl) controlElement.getPropertyObject(TL_CONTROL_PROPERTY);
		if (control == null) {
			throw new IllegalArgumentException("No control on element '" + controlElement.getId() + "'.");
		}
		return control;
	}


}
