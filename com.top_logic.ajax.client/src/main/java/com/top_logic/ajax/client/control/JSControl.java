/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ajax.client.control;

import com.google.gwt.dom.client.Element;

/**
 * Client-side representative of an active UI element.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface JSControl {

	/**
	 * The ID of the DOM {@link Element} that this {@link JSControl} controls.
	 */
	String getId();

	/**
	 * Hook that is called directly after creating a {@link JSControl}.
	 */
	void init(Object[] args);

	/**
	 * Invokes a command on this control from the server.
	 * 
	 * @param command
	 *        The name of the command to execute.
	 * @param args
	 *        Arguments to the command.
	 */
	default void invoke(String command, Object[] args) {
		throw new IllegalArgumentException("Command '" + command + "' not supported.");
	}

}
