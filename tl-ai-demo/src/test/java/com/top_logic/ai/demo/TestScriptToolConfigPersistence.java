/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.io.File;
import java.nio.file.Files;
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
import com.top_logic.tl.ai.core.tools.authoring.ScriptToolConfigPersistence;
import com.top_logic.tl.ai.core.tools.authoring.ScriptToolStore;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.util.model.ModelService;

/**
 * Tests for {@link ScriptToolConfigPersistence} overlay serialization.
 */
public class TestScriptToolConfigPersistence extends BasicTestCase {

	/** */
	public void testOverlayIsPrettyPrintedAndRoundTrips() throws Exception {
		ToolRegistryService.Config<?> config =
			TypedConfiguration.newConfigItem(ToolRegistryService.Config.class);
		ScriptToolStore store = new ScriptToolStore(config);
		String body = "{\n  x = $name;\n  $x\n}";
		store.create("prettyTool", "Pretty", "A tool.",
			List.of(new ScriptToolStore.ParamSpec("name", "tl.core:String", "the name", true, false, null)),
			body, "READ", true);

		File tmp = File.createTempFile("tl-script-tool-overlay", ".config.xml");
		tmp.deleteOnExit();
		ScriptToolConfigPersistence.persistTo(tmp, config);

		String content = Files.readString(tmp.toPath());
		// Pretty-printed: not a single line.
		assertTrue("overlay should be multi-line (pretty-printed):\n" + content,
			content.split("\n").length > 3);
		// Contains the serialized tool, factory, and param.
		assertTrue("should contain the custom factory", content.contains("name=\"custom\""));
		assertTrue("should contain the tool", content.contains("prettyTool"));
		assertTrue("should contain the parameter", content.contains("name=\"name\""));
		// TL-Script body must survive pretty-printing (CDATA content must not be mangled).
		assertTrue("TL-Script body must survive pretty-printing:\n" + content,
			content.contains("x = $name"));
	}

	/** */
	public static Test suite() {
		BasicRuntimeModule<?>[] modules = {
			SearchBuilder.Module.INSTANCE,
			ModelService.Module.INSTANCE,
			LabelProviderService.Module.INSTANCE,
		};
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(new TestSuite(TestScriptToolConfigPersistence.class), modules));
	}
}
