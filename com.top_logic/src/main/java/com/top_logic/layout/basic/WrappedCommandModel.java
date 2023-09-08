/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

/**
 * {@link CommandModel} that delegates the execution aspects to another {@link Command}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface WrappedCommandModel extends CommandModel {

	/**
	 * The delegate for the execution aspect.
	 */
	Command unwrap();

}
