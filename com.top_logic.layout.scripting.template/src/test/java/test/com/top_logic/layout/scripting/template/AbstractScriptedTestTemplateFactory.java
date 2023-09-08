/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.scripting.template;

import java.io.File;
import java.util.Map;

import test.com.top_logic.layout.scripting.AbstractScriptedTestFactory;
import test.com.top_logic.layout.scripting.ScriptedTest;
import test.com.top_logic.layout.scripting.ScriptedTest.ScriptedTestParameters;

import com.top_logic.basic.io.FileLocation;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.template.ScriptUtil;
import com.top_logic.layout.scripting.util.LazyActionProvider;

/**
 * Methods for creating {@link ScriptedTest}s from templates.
 * 
 * @see ScriptUtil
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class AbstractScriptedTestTemplateFactory extends AbstractScriptedTestFactory {

	/**
	 * Only subclasses may need to instantiate it. Everyone else should use the
	 * {@link ScriptedTestTemplateFactory#SINGLETON}.
	 */
	protected AbstractScriptedTestTemplateFactory() {
		// Reduce visibility
	}

	/**
	 * Convenience shortcut for
	 * {@link #buildTestFromTemplateFile(FileLocation, String, Map, ScriptedTestParameters)} with
	 * standard {@link ScriptedTestParameters}.
	 */
	public ScriptedTest buildTestFromTemplateFile(
			FileLocation file, String testname, Map<String, ?> templateParameters) {
		return buildTestFromTemplateFile(file, testname, templateParameters, new ScriptedTestParameters());
	}

	/**
	 * Builds a {@link ScriptedTest} with the given name by parsing the given file.
	 * <p>
	 * The {@link File} itself cannot be given to delay errors until the {@link ApplicationAction}
	 * is requested, i.e. until the test is executed. Otherwise, a mistake on {@link File}
	 * instantiation could kill the whole test tree creation.
	 * </p>
	 * 
	 * @param file
	 *        The location of the file. Must not be <code>null</code>.
	 * @param testname
	 *        The name of the test. Must not be <code>null</code>.
	 * @param templateParameters
	 *        The values for the template parameters. Must not be <code>null</code>.
	 * @param parameters
	 *        Parameters for the script execution, like which user should execute the script. Is
	 *        allowed to be <code>null</code>.
	 * @return Never <code>null</code>.
	 */
	public ScriptedTest buildTestFromTemplateFile(
			FileLocation file, String testname, Map<String, ?> templateParameters, ScriptedTestParameters parameters) {
		LazyActionProvider actionProvider = new TemplateScriptActionProvider(file, templateParameters);
		return new ScriptedTest(actionProvider, testname, parameters);
	}

}
