/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.script;

import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.model.search.configured.ConfiguredScript;
import com.top_logic.model.search.configured.ConfiguredTLScriptFunctions;
import com.top_logic.model.search.configured.ScriptConfiguration;
import com.top_logic.model.search.configured.ScriptParameter;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.util.model.ModelService;

/**
 * Tests that {@code query} and {@code present} accept a call of a configured TL-Script function
 * that only reads, and name the function a refused script calls.
 */
public class TestReadOnlyConfiguredFunctions extends BasicTestCase {

	private static final String SUBTREE = "testReadOnlySubtree";

	private static final String RENAME = "testReadOnlyRename";

	private static final String RENAME_SUBTREE = "testReadOnlyRenameSubtree";

	private static final String NODE = "node";

	private Map<String, ConfiguredScript.Config> _originalScripts;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_originalScripts = new LinkedHashMap<>(serviceConfig().getScripts());

		Map<String, ConfiguredScript.Config> scripts = new LinkedHashMap<>(_originalScripts);
		add(scripts, SUBTREE, "$node.recursion(x -> $x.get(`test.objectedit:Folder#folders`))");
		add(scripts, RENAME, "$node.set(`test.objectedit:Folder#name`, 'renamed')");
		add(scripts, RENAME_SUBTREE, SUBTREE + "($node).foreach(x -> " + RENAME + "($x))");
		setScripts(scripts);
	}

	@Override
	protected void tearDown() throws Exception {
		setScripts(_originalScripts);
		_originalScripts = null;
		super.tearDown();
	}

	/** A configured function that only reads is a read. */
	public void testReadingConfiguredFunctionIsARead() {
		TLScriptExecutor.compileReadOnly(
			"all(`test.objectedit:Folder`).firstElement().map(f -> " + SUBTREE + "($f))"
				+ ".filter(n -> $n.get(`test.objectedit:Folder#name`) == 'x')");
	}

	/** A configured function that writes is refused by its name. */
	public void testWritingConfiguredFunctionIsNamed() {
		assertRefusedFor(RENAME, "all(`test.objectedit:Folder`).foreach(f -> " + RENAME + "($f))");
	}

	/** A configured function that writes through another one is refused by its own name. */
	public void testIndirectlyWritingConfiguredFunctionIsNamed() {
		assertRefusedFor(RENAME_SUBTREE,
			"all(`test.objectedit:Folder`).foreach(f -> " + RENAME_SUBTREE + "($f))");
	}

	/** A write directly in the expression is refused by the function that writes. */
	public void testDirectSetIsNamed() {
		assertRefusedFor(SideEffects.SET,
			"all(`test.objectedit:Folder`).foreach(f -> $f.set(`test.objectedit:Folder#name`, 'x'))");
	}

	/** A configured function changed from reading to writing is refused by the next check. */
	public void testChangedConfiguredFunctionIsRefused() throws Exception {
		String script = "all(`test.objectedit:Folder`).firstElement().map(f -> " + SUBTREE + "($f))";
		TLScriptExecutor.compileReadOnly(script);

		Map<String, ConfiguredScript.Config> scripts = new LinkedHashMap<>(serviceConfig().getScripts());
		add(scripts, SUBTREE, "$node.set(`test.objectedit:Folder#name`, 'changed')");
		setScripts(scripts);

		assertRefusedFor(SUBTREE, script);
	}

	private static void assertRefusedFor(String function, String script) {
		try {
			TLScriptExecutor.compileReadOnly(script);
			fail("Expected '" + script + "' to be refused as a read.");
		} catch (TLScriptExecutor.NotReadOnly ex) {
			assertEquals(function, ex.getFunction());
			assertContains("'" + function + "'", ex.getMessage());
		}
	}

	private static void add(Map<String, ConfiguredScript.Config> scripts, String name, String implementation)
			throws ConfigurationException {
		ConfiguredScript.Config script = TypedConfiguration.newConfigItem(ConfiguredScript.Config.class);
		script.setName(name);
		ScriptParameter parameter = TypedConfiguration.newConfigItem(ScriptParameter.class);
		parameter.setName(NODE);
		script.getParameters().add(parameter);
		script.setImplementation(ExprFormat.INSTANCE.getValue(ScriptConfiguration.IMPLEMENTATION, implementation));
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

	/** The model, the knowledge base and the configured functions the scripts compile against. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(new TestSuite(TestReadOnlyConfiguredFunctions.class),
				ModelService.Module.INSTANCE, CommandGroupRegistry.Module.INSTANCE,
				ConfiguredTLScriptFunctions.Module.INSTANCE));
	}
}
