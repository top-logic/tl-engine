/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import com.top_logic.util.TopLogicServlet;

/**
 * The servlet mapped to {@code /chat/*}, which serves no endpoint.
 *
 * <p>
 * Everything the chat's browser component asks of the server is a command, a data request or an
 * upload of the chat controls ({@link com.top_logic.ai.chat.view.ChatControl}), and the chat's
 * frames come on the window's event stream ({@link ChatSSEHandler}). A request to this servlet is
 * answered with the container's "method not allowed".
 * </p>
 */
public class ChatServlet extends TopLogicServlet {

	// No endpoint.

}
