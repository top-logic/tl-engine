/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.FileBasedBinaryContent;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.tools.authoring.ScriptToolAuthoringTool;
import com.top_logic.tl.ai.core.tools.authoring.ScriptToolConfigPersistence;
import com.top_logic.tl.ai.core.tools.authoring.ScriptToolStore;
import com.top_logic.tl.ai.core.tools.authoring.ScriptToolStore.ParamSpec;
import com.top_logic.tl.ai.core.tools.registry.ScriptToolFactory;
import com.top_logic.tl.ai.core.tools.registry.ScriptToolFactory.ScriptToolConfig;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.util.TLContextManager;
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
			List.of(new ScriptToolStore.ParamSpec("name", "String", null, "the name", true, false, null, null)),
			body, "READ", true, null);

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

	/**
	 * A tool the agent authors with a text, an object and a list parameter is stored, reads back from
	 * the stored overlay and, called with arguments, gives the expected result.
	 */
	public void testAuthoredParametersRoundTrip() throws Exception {
		ToolRegistryService.Config<?> config = TypedConfiguration.newConfigItem(ToolRegistryService.Config.class);
		new ScriptToolStore(config).create("describe", "Describe", "Describes a room.",
			List.of(
				new ParamSpec("title", "String", null, "The heading.", true, false, null, null),
				new ParamSpec("room", null, ScriptToolFactoryTest.ROOM, "The room to describe.", true, false, null,
					null),
				new ParamSpec("tags", null, null, "Keywords.", false, true, null, null)),
			"$title + \": \" + $room.get(`demo.events:Named#name`) + \" \" + $tags.size()",
			"READ", true, null);

		File tmp = File.createTempFile("tl-script-tool-overlay", ".config.xml");
		tmp.deleteOnExit();
		ScriptToolConfigPersistence.persistTo(tmp, config);

		ScriptToolConfig readBack = readTool(tmp, ScriptToolStore.DEFAULT_FACTORY, "describe");
		assertEquals(List.of("title", "room", "tags"), List.copyOf(readBack.getParameters().getProperties().keySet()));

		// What the agent reads back of the stored declaration.
		ToolRegistryService.Config<?> stored = TypedConfiguration.newConfigItem(ToolRegistryService.Config.class);
		ScriptToolStore storedTools = new ScriptToolStore(stored);
		storedTools.create("describe", null, "d", null, "1", "READ", true, null);
		storedTools.get("describe").setParameters(readBack.getParameters());
		String detail = new ScriptToolAuthoringTool(() -> stored, c -> {
			// Nothing to persist.
		}).getScriptTool("describe", null);
		assertTrue(detail, detail.contains("\"objectType\":\"" + ScriptToolFactoryTest.ROOM + "\""));
		assertTrue(detail, detail.contains("\"multiple\":true"));
		assertTrue(detail, detail.contains("The room to describe."));

		TLObject room = ScriptToolFactoryTest.createRoom("lab");
		try {
			Tool tool = ScriptToolFactoryTest.singleTool(readBack);
			ToolResult result = tool.execute(
				Map.of("title", "Room", "room", ScriptToolFactoryTest.id(room), "tags", List.of("a", "b")),
				ToolContext.EMPTY);
			assertEquals("Room: lab 2", MarkedResults.disclosed(result.text()));
		} finally {
			ScriptToolFactoryTest.delete(room);
		}
	}

	private static ScriptToolConfig readTool(File file, String factory, String name) throws Exception {
		ConfigurationReader reader = new ConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			Collections.singletonMap("application",
				TypedConfiguration.getConfigurationDescriptor(ApplicationConfig.Config.class)));
		reader.setSource(FileBasedBinaryContent.createBinaryContent(file));
		ApplicationConfig.Config app = (ApplicationConfig.Config) reader.read();
		ToolRegistryService.Config<?> registry =
			(ToolRegistryService.Config<?>) app.getServices().get(ToolRegistryService.class).getInstance();
		ScriptToolFactory.Config factoryConfig =
			(ScriptToolFactory.Config) registry.getToolFactories().get(factory).getImplementation();
		return factoryConfig.getTools().get(name);
	}

	/**
	 * Runs each test as the root account, see {@link ScriptToolFactoryTest#runTest()}.
	 */
	@Override
	protected void runTest() throws Throwable {
		TLContextManager.<Void, Throwable, RuntimeException> inPersonContext(PersonManager.getManager().getRoot(),
			() -> {
				super.runTest();
				return null;
			});
	}

	/** */
	public static Test suite() {
		BasicRuntimeModule<?>[] modules = {
			SearchBuilder.Module.INSTANCE,
			ModelService.Module.INSTANCE,
			LabelProviderService.Module.INSTANCE,
			PersonManager.Module.INSTANCE,
		};
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(new TestSuite(TestScriptToolConfigPersistence.class), modules));
	}
}
