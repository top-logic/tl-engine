/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.layout.react.control.TextPart;
import test.com.top_logic.layout.view.command.ArgumentsProbe;
import test.com.top_logic.layout.view.command.TestButtonCommand;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.basic.DummyDisplayContext;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.UploadHandler;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.upload.UploadSupport;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.command.CommandCliqueService;
import com.top_logic.layout.view.command.UploadCommand;
import com.top_logic.layout.view.element.ButtonElement;

/**
 * Tests that a {@code <button>} whose action is an {@link UploadCommand} runs the command: the
 * button accepts the files the user picks and hands each of them to the command's action chain.
 */
public class TestButtonUploadAction extends TestCase {

	/** Name of the uploaded file. */
	private static final String FILE_NAME = "report.txt";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ArgumentsProbe.reset();
	}

	@Override
	protected void tearDown() throws Exception {
		ArgumentsProbe.reset();
		super.tearDown();
	}

	/**
	 * Tests that a file uploaded to the button of an {@link UploadCommand} runs the command's action
	 * chain with that file.
	 */
	public void testUploadRunsActionChain() throws ConfigurationException {
		IReactControl control = control(
			"<action class='" + UploadCommand.class.getName() + "'>"
				+ "<action class='" + ArgumentsProbe.class.getName() + "'/>"
				+ "</action>");

		assertTrue("The button of an upload command accepts uploads: " + control,
			control instanceof UploadHandler);

		((UploadHandler) control).handleUpload(new DummyDisplayContext(),
			List.of(new TextPart(UploadSupport.FILE_PART, FILE_NAME, "content")));

		assertEquals("The action chain runs once for the uploaded file.", 1, ArgumentsProbe.SEEN.size());
		Object file = ArgumentsProbe.SEEN.get(0).value();
		assertTrue("The action chain runs with the uploaded file: " + file, file instanceof BinaryData);
		assertEquals(FILE_NAME, ((BinaryData) file).getName());
	}

	/**
	 * Tests that the button of a command running on a click alone accepts no uploads.
	 */
	public void testClickCommandIsNoUploadButton() throws ConfigurationException {
		IReactControl control = control("<action class='" + TestButtonCommand.TestCommand.class.getName() + "'/>");

		assertFalse("The button of a click command accepts no uploads: " + control,
			control instanceof UploadHandler);
	}

	/** The control of a {@code <button>} with the given action. */
	private static IReactControl control(String action) throws ConfigurationException {
		UIElement button = element("<button>" + action + "</button>");
		return button.createControl(new DefaultViewContext(new DefaultReactContext("", "test",
			new SSEUpdateQueue(), new ReactWindowRegistry("test"))));
	}

	private static UIElement element(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestButtonUploadAction.class);
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"button", TypedConfiguration.getConfigurationDescriptor(ButtonElement.Config.class));
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(CharacterContents.newContent(xml, "test-button.view.xml"));
		ButtonElement.Config config = (ButtonElement.Config) reader.read();
		context.checkErrors();

		UIElement result = context.getInstance(config);
		context.checkErrors();
		return result;
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module, which resolves the element tags, and the
	 * services the command model of a button needs for its label, its clique and its upload.
	 */
	public static Test suite() throws ModuleException {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestButtonUploadAction.class, TypeIndex.Module.INSTANCE,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE,
				CommandCliqueService.Module.INSTANCE));
	}

}
