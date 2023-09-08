/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

/**
 * Builder for an {@link Command} instance.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface CommandBuilder {

	/**
	 * Creates the final {@link Command} by linking it to its {@link CommandModel}.
	 * 
	 * @param model
	 *        The UI aspects.
	 * @return The final {@link Command}.
	 */
	Command build(CommandModel model);

}