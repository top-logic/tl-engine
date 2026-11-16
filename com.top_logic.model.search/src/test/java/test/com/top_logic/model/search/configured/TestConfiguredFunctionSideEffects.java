/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.configured;

import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.model.search.configured.ConfiguredScript;
import com.top_logic.model.search.configured.ConfiguredTLScriptFunctions;
import com.top_logic.model.search.configured.ScriptParameter;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.parser.ParseException;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;

/**
 * Test of the side effect analysis of configured TL-Script functions, see
 * {@link ConfiguredTLScriptFunctions#isSideEffectFree(String)}.
 */
@SuppressWarnings("javadoc")
public class TestConfiguredFunctionSideEffects extends AbstractSearchExpressionTest {

	private static final String PARAM = "n";

	private static final String WRITE = "$n.set(`TestSearchExpression:A#str`, 'x')";

	private static final String READING = "testSEFReading";

	private static final String WRITING = "testSEFWriting";

	private static final String CALLS_WRITING = "testSEFCallsWriting";

	private static final String SELF_RECURSIVE = "testSEFSelfRecursive";

	private static final String MUTUAL_F = "testSEFMutualF";

	private static final String MUTUAL_G = "testSEFMutualG";

	private static final String CYCLE_F = "testSEFCycleF";

	private static final String CYCLE_G = "testSEFCycleG";

	private static final String CALENDAR = "testSEFCalendar";

	private static final String CHANGING = "testSEFChanging";

	private Map<String, ConfiguredScript.Config> _originalScripts;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_originalScripts = new LinkedHashMap<>(serviceConfig().getScripts());

		Map<String, ConfiguredScript.Config> scripts = new LinkedHashMap<>(_originalScripts);
		add(scripts, READING, "list($n).size()");
		add(scripts, WRITING, WRITE);
		add(scripts, CALLS_WRITING, "list(" + WRITING + "($n))");
		add(scripts, SELF_RECURSIVE, "if($n == null, null, " + SELF_RECURSIVE + "(null))");
		add(scripts, MUTUAL_F, MUTUAL_G + "($n)");
		add(scripts, MUTUAL_G, "if($n == null, null, " + MUTUAL_F + "(null))");
		add(scripts, CYCLE_F, CYCLE_G + "($n)");
		add(scripts, CYCLE_G, "{" + WRITE + "; " + CYCLE_F + "($n);}");
		add(scripts, CALENDAR, "toUserCalendar($n)");
		add(scripts, CHANGING, "list($n)");
		setScripts(scripts);
	}

	@Override
	protected void tearDown() throws Exception {
		setScripts(_originalScripts);
		_originalScripts = null;
		super.tearDown();
	}

	/** (a) */
	public void testReadingIsFree() throws Exception {
		assertFree(READING, true);
	}

	/** (b) */
	public void testWritingIsNotFree() throws Exception {
		assertFree(WRITING, false);
	}

	/** (c) */
	public void testCallerOfWritingIsNotFree() throws Exception {
		assertFree(CALLS_WRITING, false);
	}

	/** (d) */
	public void testSelfRecursiveReadingIsFree() throws Exception {
		assertFree(SELF_RECURSIVE, true);
	}

	/** (e) */
	public void testMutuallyRecursiveReadingIsFree() throws Exception {
		assertFree(MUTUAL_F, true);
		assertFree(MUTUAL_G, true);
	}

	/** (f) */
	public void testCycleWithWritingIsNotFree() throws Exception {
		assertFree(CYCLE_F, false);
		assertFree(CYCLE_G, false);
	}

	/** (g) */
	public void testChangedConfigurationTakesEffect() throws Exception {
		assertFree(CHANGING, true);

		Map<String, ConfiguredScript.Config> scripts = new LinkedHashMap<>(serviceConfig().getScripts());
		add(scripts, CHANGING, WRITE);
		setScripts(scripts);

		assertFree(CHANGING, false);
	}

	/** (h) */
	public void testFunctionMarkedWithSideEffectIsNotFree() throws Exception {
		assertFree(CALENDAR, false);
	}

	public void testUnknownFunctionIsNotFree() {
		assertFalse(functions().isSideEffectFree("testSEFUndefined"));
	}

	private static void assertFree(String function, boolean expected) throws Exception {
		assertEquals("Side effect freedom of '" + function + "'.", expected,
			functions().isSideEffectFree(function));

		// The call of the function in a script.
		SearchExpression call = search(function + "(null)");
		assertTrue(call instanceof GenericMethod);
		assertEquals("Side effect freedom of a call of '" + function + "'.", expected,
			((GenericMethod) call).isSideEffectFree());
	}

	private static ConfiguredTLScriptFunctions<?> functions() {
		return ConfiguredTLScriptFunctions.Module.INSTANCE.getImplementationInstance();
	}

	private static void add(Map<String, ConfiguredScript.Config> scripts, String name, String implementation)
			throws ParseException {
		ConfiguredScript.Config script = TypedConfiguration.newConfigItem(ConfiguredScript.Config.class);
		script.setName(name);
		ScriptParameter parameter = TypedConfiguration.newConfigItem(ScriptParameter.class);
		parameter.setName(PARAM);
		script.getParameters().add(parameter);
		script.setImplementation(parse(implementation));
		scripts.put(name, script);
	}

	private static void setScripts(Map<String, ConfiguredScript.Config> scripts) throws Exception {
		ConfiguredTLScriptFunctions.Config<?> config = serviceConfig();
		PropertyDescriptor property = config.descriptor().getProperty(ConfiguredTLScriptFunctions.Config.SCRIPTS);
		config.update(property, scripts);
		ModuleUtil.INSTANCE.restart(ConfiguredTLScriptFunctions.Module.INSTANCE);
	}

	private static ConfiguredTLScriptFunctions.Config<?> serviceConfig() throws Exception {
		return (ConfiguredTLScriptFunctions.Config<?>) ApplicationConfig.getInstance()
			.getServiceConfiguration(ConfiguredTLScriptFunctions.class);
	}

	public static Test suite() {
		return suite(TestConfiguredFunctionSideEffects.class,
			CommandGroupRegistry.Module.INSTANCE,
			ConfiguredTLScriptFunctions.Module.INSTANCE);
	}

}
