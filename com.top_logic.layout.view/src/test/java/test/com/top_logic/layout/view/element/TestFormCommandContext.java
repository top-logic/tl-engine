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

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.layout.react.control.TextPart;
import test.com.top_logic.layout.view.command.ArgumentsProbe;

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
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.UploadHandler;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.button.ReactUploadButtonControl;
import com.top_logic.layout.react.control.upload.UploadSupport;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.command.CommandCliqueService;
import com.top_logic.layout.view.command.CommandScope;
import com.top_logic.layout.view.element.FormElement;
import com.top_logic.layout.view.form.FormModel;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;

/**
 * Tests the commands a {@code <form>} lists in its {@link FormElement.Config#getCommands()
 * commands}: the form offers them to the command scope of the enclosing element, whose toolbar
 * shows their buttons, and they run in the form, whichever context the toolbar's button runs
 * them with.
 */
public class TestFormCommandContext extends TestCase {

	/** Name of the channel holding the object the form displays. */
	private static final String INPUT = "account";

	/** Name of the upload command the form lists. */
	private static final String UPLOAD = "upload";

	/** Name of the click command the form lists. */
	private static final String APPLY = "apply";

	/** Name of the uploaded file. */
	private static final String FILE_NAME = "report.txt";

	/** The commands the form offers to the enclosing element. */
	private CommandScope _scope;

	/** The context of the enclosing element, in which its toolbar creates the buttons. */
	private ReactContext _panelContext;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ArgumentsProbe.reset();
		_scope = new CommandScope(List.of());
		_panelContext = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")) {
			@Override
			public ModelScope getModelScope() {
				// The test displays no persistent object, so there is nothing to observe.
				return null;
			}
		};
	}

	@Override
	protected void tearDown() throws Exception {
		ArgumentsProbe.reset();
		super.tearDown();
	}

	/**
	 * Tests that the toolbar button of an {@code <upload-command>} a form lists opens the file
	 * chooser, and that a file uploaded through it runs the command's actions in the form, which
	 * holds the {@link FormModel}.
	 */
	public void testUploadCommandOfAForm() throws ConfigurationException {
		CommandModel command = formCommand(UPLOAD,
			"<upload-command name='" + UPLOAD + "'>"
				+ "<action class='" + ArgumentsProbe.class.getName() + "'/>"
				+ "</upload-command>");

		ReactButtonControl button = command.createButton(_panelContext);
		assertTrue("The toolbar button of an upload command opens the file chooser: " + button,
			button instanceof ReactUploadButtonControl);

		((UploadHandler) button).handleUpload(new DummyDisplayContext(),
			List.of(new TextPart(UploadSupport.FILE_PART, FILE_NAME, "content")));

		assertEquals("The actions run once for the uploaded file.", 1, ArgumentsProbe.SEEN.size());
		Object file = ArgumentsProbe.SEEN.get(0).value();
		assertTrue("The actions run with the uploaded file: " + file, file instanceof BinaryData);
		assertEquals(FILE_NAME, ((BinaryData) file).getName());
		assertFormContext(ArgumentsProbe.CONTEXTS.get(0));
	}

	/**
	 * Tests that the toolbar button of a command a form lists that runs on a click alone is a plain
	 * button, and that a click runs the command's actions in the form.
	 */
	public void testClickCommandOfAForm() throws ConfigurationException {
		CommandModel command = formCommand(APPLY,
			"<generic-command name='" + APPLY + "'>"
				+ "<action class='" + ArgumentsProbe.class.getName() + "'/>"
				+ "</generic-command>");

		ReactButtonControl button = command.createButton(_panelContext);
		assertFalse("The toolbar button of a click command accepts no uploads: " + button,
			button instanceof UploadHandler);

		command.executeCommand(_panelContext);

		assertEquals("A click runs the actions once.", 1, ArgumentsProbe.CONTEXTS.size());
		assertFormContext(ArgumentsProbe.CONTEXTS.get(0));
	}

	/** Asserts that the actions ran in a context holding the form's {@link FormModel}. */
	private static void assertFormContext(ReactContext context) {
		assertTrue("The actions run in a view context: " + context, context instanceof ViewContext);
		assertNotNull("The actions run in the form, which holds the form model.",
			((ViewContext) context).getFormModel());
	}

	/**
	 * The command of the given name a displayed {@code <form>} listing the given command offers to
	 * the enclosing element.
	 */
	private CommandModel formCommand(String name, String command) throws ConfigurationException {
		ViewContext panel = new DefaultViewContext(_panelContext).withScope(CommandScope.class, _scope);
		panel.registerChannel(INPUT, new DefaultViewChannel(INPUT));

		FormElement form = element("<form input='" + INPUT + "'><commands>" + command + "</commands></form>");
		IReactControl control = form.createControl(panel);
		assertNull("The form offers nothing while it is not displayed.", _scope.resolveCommand(name));
		((ReactControl) control).attach();

		CommandModel offered = _scope.resolveCommand(name);
		assertNotNull("The displayed form offers its command.", offered);
		return offered;
	}

	private static FormElement element(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestFormCommandContext.class);
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"form", TypedConfiguration.getConfigurationDescriptor(FormElement.Config.class));
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(CharacterContents.newContent(xml, "test-form.view.xml"));
		FormElement.Config config = (FormElement.Config) reader.read();
		context.checkErrors();

		FormElement result = (FormElement) context.getInstance(config);
		context.checkErrors();
		return result;
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module, which resolves the element tags, and the
	 * services a form and the models of its commands need.
	 */
	public static Test suite() throws ModuleException {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestFormCommandContext.class, TypeIndex.Module.INSTANCE,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE,
				CommandCliqueService.Module.INSTANCE, CommandGroupRegistry.Module.INSTANCE));
	}

}
