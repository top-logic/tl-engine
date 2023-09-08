/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.scripting;


/**
 * Singleton for {@link AbstractScriptedTestFactory}.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class ScriptedTestFactory extends AbstractScriptedTestFactory {

	/**
	 * Singleton {@link ScriptedTestFactory} instance.
	 */
	public static final ScriptedTestFactory SINGLETON = new ScriptedTestFactory();

	private ScriptedTestFactory() {
		// Reduce visibility
	}

}
