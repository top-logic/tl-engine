/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.tl.ai.core.tools.authoring.ScriptToolStore;
import com.top_logic.tl.ai.core.tools.authoring.ScriptToolStore.ParamSpec;
import com.top_logic.tl.ai.core.tools.registry.ScriptToolFactory.ScriptToolConfig;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.util.model.ModelService;

/**
 * Tests for {@link ScriptToolStore} and {@code ScriptToolAuthoringTool}.
 */
public class TestScriptToolAuthoringTool extends BasicTestCase {

	private static ToolRegistryService.Config<?> newRegistryConfig() {
		return TypedConfiguration.newConfigItem(ToolRegistryService.Config.class);
	}

	public void testCreateAndReadBack() {
		ScriptToolStore store = new ScriptToolStore(newRegistryConfig());

		store.create("greet", "Greet", "Greets a person by name.",
			List.of(new ParamSpec("name", "tl.core:String", "Who to greet", true, false)),
			"\"Hello \" + $name", "READ", true);

		ScriptToolConfig created = store.get("greet");
		assertNotNull("tool must exist after create", created);
		assertEquals("greet", created.getName());
		assertEquals(1, created.getParameters().size());
		assertEquals("name", created.getParameters().get(0).getName());

		List<ScriptToolConfig> all = store.list();
		assertEquals(1, all.size());
	}

	public void testDuplicateNameRejected() {
		ScriptToolStore store = new ScriptToolStore(newRegistryConfig());
		store.create("dup", "Dup", "d", List.of(), "1", "READ", true);
		try {
			store.create("dup", "Dup", "d", List.of(), "1", "READ", true);
			fail("expected duplicate-name rejection");
		} catch (IllegalArgumentException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("already exists"));
		}
	}

	public void testBadParameterTypeRejected() {
		ScriptToolStore store = new ScriptToolStore(newRegistryConfig());
		try {
			store.create("bad", "Bad", "d",
				List.of(new ParamSpec("x", "no.such:Type", "", true, false)),
				"$x", "READ", true);
			fail("expected unresolvable-type rejection");
		} catch (IllegalArgumentException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("no.such:Type"));
		}
	}

	public void testUnparseableScriptRejected() {
		ScriptToolStore store = new ScriptToolStore(newRegistryConfig());
		try {
			store.create("broken", "Broken", "d", List.of(), "this is ( not valid", "READ", true);
			fail("expected script-parse rejection");
		} catch (IllegalArgumentException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().toLowerCase().contains("script"));
		}
	}

	public static Test suite() {
		BasicRuntimeModule<?>[] modules = {
			SearchBuilder.Module.INSTANCE,
			ModelService.Module.INSTANCE,
			LabelProviderService.Module.INSTANCE,
		};
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(new TestSuite(TestScriptToolAuthoringTool.class), modules));
	}
}
