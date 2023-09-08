/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.scripting.template;


/**
 * Singleton for {@link AbstractScriptedTestTemplateFactory}.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class ScriptedTestTemplateFactory extends AbstractScriptedTestTemplateFactory {

	/**
	 * Singleton {@link AbstractScriptedTestTemplateFactory} instance.
	 */
	public static final AbstractScriptedTestTemplateFactory SINGLETON = new AbstractScriptedTestTemplateFactory();

	private ScriptedTestTemplateFactory() {
		// Reduce visibility
	}

}
