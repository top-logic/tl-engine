/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.client.diagramjs.event;

import jsinterop.annotations.JsFunction;

/**
 * Basic event handler for events dispatched from within diagramjs.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
@JsFunction
public interface EventHandler {

	/**
	 * The actual event handler.
	 */
	public void call(Event event);
}