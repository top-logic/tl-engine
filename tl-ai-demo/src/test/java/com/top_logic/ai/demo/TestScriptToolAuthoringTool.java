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
import com.top_logic.tl.ai.core.tools.authoring.ScriptToolAuthoringTool;
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

	public void testUpdateReplacesFields() {
		ScriptToolStore store = new ScriptToolStore(newRegistryConfig());
		store.create("calc", "Calc", "old", List.of(), "1", "READ", true);

		store.update("calc", null, "new description", null, "2", null, null);

		ScriptToolConfig updated = store.get("calc");
		assertNotNull(updated);
		assertEquals("2", com.top_logic.model.search.expr.config.ExprFormat.INSTANCE
			.getSpecification(updated.getImplementation()));
		assertEquals("label preserved", "Calc",
			com.top_logic.tl.ai.core.tools.registry.ToolDescriptions.englishPlainText(updated.getLabel()));
		assertEquals("parameters preserved (still empty)", 0, updated.getParameters().size());
		assertEquals("mode preserved", "READ", updated.getMode().name());
	}

	public void testUpdateMissingRejected() {
		ScriptToolStore store = new ScriptToolStore(newRegistryConfig());
		try {
			store.update("ghost", null, "x", null, null, null, null);
			fail("expected missing-tool rejection");
		} catch (IllegalArgumentException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("ghost"));
		}
	}

	public void testDelete() {
		ScriptToolStore store = new ScriptToolStore(newRegistryConfig());
		store.create("temp", "Temp", "d", List.of(), "1", "READ", true);
		store.delete("temp");
		assertNull("tool must be gone after delete", store.get("temp"));
	}

	public void testDeleteMissingRejected() {
		ScriptToolStore store = new ScriptToolStore(newRegistryConfig());
		try {
			store.delete("ghost");
			fail("expected missing-tool rejection");
		} catch (IllegalArgumentException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("ghost"));
		}
	}

	public void testToolCreateGetListDeleteJson() {
		ToolRegistryService.Config<?>[] holder = { newRegistryConfig() };
		ScriptToolAuthoringTool tool = new ScriptToolAuthoringTool(
			() -> holder[0],
			cfg -> holder[0] = cfg);

		String create = tool.createScriptTool("greet", "Greet", "Greets by name.",
			List.of(new ScriptToolStore.ParamSpec("name", "tl.core:String", "who", true, false)),
			"\"Hello \" + $name", "READ", Boolean.TRUE);
		assertTrue("create should succeed: " + create, create.contains("\"success\":true"));
		assertTrue("create should mention restart: " + create, create.toLowerCase().contains("restart"));

		String get = tool.getScriptTool("greet");
		assertTrue("get should expose the script body: " + get, get.contains("Hello"));
		assertTrue("get should report existence: " + get, get.contains("\"exists\":true"));

		String list = tool.listScriptTools();
		assertTrue("list should include the tool: " + list, list.contains("greet"));

		String delete = tool.deleteScriptTool("greet");
		assertTrue("delete should succeed: " + delete, delete.contains("\"success\":true"));
		assertTrue("tool gone after delete", tool.getScriptTool("greet").contains("\"exists\":false"));
	}

	public void testUpdateMissingListsExistingNames() {
		ScriptToolStore store = new ScriptToolStore(newRegistryConfig());
		store.create("summarize_ingredient_costs", "X", "d", java.util.List.of(), "1", "READ", true);
		try {
			store.update("wrong-name", null, "x", null, null, null, null);
			fail("expected missing-tool rejection");
		} catch (IllegalArgumentException ex) {
			assertTrue("message should list existing names: " + ex.getMessage(),
				ex.getMessage().contains("summarize_ingredient_costs"));
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
