/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.scripting;

import java.io.File;

import test.com.top_logic.layout.scripting.ScriptedTest.ScriptedTestParameters;

import com.top_logic.basic.io.FileLocation;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.util.LazyActionProvider;

/**
 * Methods for creating {@link ScriptedTest}s.
 * <p>
 * There is a <code>ScriptedTestTemplateFactory</code> in the project
 * 'com.top_logic.layout.scripting.template' which contains methods for creating
 * {@link ScriptedTest}s from templates.
 * </p>
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class AbstractScriptedTestFactory {

	/**
	 * Only subclasses may need to instantiate it. Everyone else should use the
	 * {@link ScriptedTestFactory#SINGLETON}.
	 */
	protected AbstractScriptedTestFactory() {
		// See JavaDoc above.
	}

	/**
	 * Convenience shortcut for
	 * {@link #buildTestFromFile(FileLocation, String, ScriptedTestParameters)} with standard
	 * {@link ScriptedTestParameters}.
	 */
	public ScriptedTest buildTestFromFile(FileLocation file, String testname) {
		return buildTestFromFile(file, testname, new ScriptedTestParameters());
	}

	/**
	 * Builds a {@link ScriptedTest} with the given name by parsing the given file.
	 * <p>
	 * The {@link File} itself cannot be given to delay errors until the {@link ApplicationAction}
	 * is requested, i.e. until the test is executed. Otherwise, a mistake on {@link File}
	 * instantiation could kill the whole test tree creation.
	 * </p>
	 * 
	 * @see XmlScriptedTestUtil
	 * 
	 * @param file
	 *        The location of the file. Must not be <code>null</code>.
	 * @param testname
	 *        The name of the test. Must not be <code>null</code>.
	 * @param parameters
	 *        Parameters for the script execution, like which user should execute the script. Must
	 *        not be <code>null</code>.
	 * @return Never <code>null</code>.
	 */
	public ScriptedTest buildTestFromFile(FileLocation file, String testname, ScriptedTestParameters parameters) {
		LazyActionProvider actionProvider = XmlScriptedTestUtil.newActionProvider(file);
		return new ScriptedTest(actionProvider, testname, parameters);
	}

}
