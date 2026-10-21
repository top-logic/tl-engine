/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react.websocket;

import jakarta.websocket.OnMessage;
import jakarta.websocket.server.ServerEndpoint;

import com.top_logic.util.WebSocketEndpointListener;

/**
 * Websocket endpoint of the React demo answering each text message with the message itself.
 *
 * <p>
 * The endpoint demonstrates an application websocket endpoint. It is served at
 * {@value #PATH} below the context path of the application and is registered by the
 * {@link WebSocketEndpointListener} through the context parameter
 * {@value WebSocketEndpointListener#ENDPOINTS_PARAM} of the application's {@code web.xml}. It
 * requires no login.
 * </p>
 */
@ServerEndpoint(EchoEndpoint.PATH)
public class EchoEndpoint {

	/**
	 * Path of the endpoint relative to the context path of the application.
	 */
	public static final String PATH = "/ws/echo";

	/**
	 * Answers a text message with the message itself.
	 */
	@OnMessage
	public String onMessage(String message) {
		return message;
	}

}
