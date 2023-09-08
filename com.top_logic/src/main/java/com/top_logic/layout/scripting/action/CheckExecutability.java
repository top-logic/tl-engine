/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

/**
 * Checks whether trying to execute the command fails with the given
 * {@link CheckExecutability#getReasonKey() reason key}. If no reason key is set, it is checked that
 * the command is executable.
 * 
 * @author <a href="mailto:Jan.Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public interface CheckExecutability extends CommandAction, ReasonKeyAssertion {

	// Pure sum interface

}
